package ro.halex.av

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.neo4j.driver.Driver
import org.neo4j.driver.TransactionConfig
import ro.halex.av.plugins.DatasetLocation
import java.time.Duration

suspend fun PipelineContext<Unit, ApplicationCall>.getDatasets(driver: Driver)
{
    call.respondText(Json.encodeToString(retrieveDatasets(driver)), ContentType.Application.Json, HttpStatusCode.OK)
}

suspend fun DefaultWebSocketServerSession.updateWebSocket(
    datasetUpdateEvent: Flow<Unit>,
    driver: Driver
)
{
    datasetUpdateEvent.collect {
        send(Json.encodeToString(retrieveDatasets(driver)))
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.getClassifiedDataset(driver: Driver, datasetName: String)
{
    val relationship = call.receive<NestingRelationship>()
    val datasetNode = driver.session().readTransaction { it.getDatasetNode(datasetName) } ?: run {
        call.respond(HttpStatusCode.NotFound)
        return
    }
    val datasetId = datasetNode.id
    val properties = datasetNode.properties
    if (
        !relationship.valuedProperties.all { it.property in properties } ||
        !relationship.classificationProperties.all { it.property in properties } ||
        !relationship.groupedProperties.all { it.property in properties }
    )
    {
        call.respond(HttpStatusCode.BadRequest, "The set of properties was invalid")
        return
    }

    application.log.info("Dataset '$datasetName' queried by $relationship")

    val transaction =
        driver.session().beginTransaction(TransactionConfig.builder().withTimeout(Duration.ofMinutes(1)).build())
    try
    {
        val firstNonValuedItemsetId =
            relationship.valuedProperties
                .fold(datasetId) { currentId, (property, value) ->
                    transaction.getGroupingItemsetId(currentId, property, value) ?: run {
                        transaction.classify(currentId, property)
                        transaction.getGroupingItemsetId(currentId, property, value)
                            ?: run {
                                call.respond(
                                    HttpStatusCode.BadRequest,
                                    "Value '$value' for property '$property' was invalid"
                                )
                                return
                            }
                    }
                }

        fun createTree(currentId: Int, classificationProperties: List<ClassificationProperty>): JsonElement
        {
            return if (classificationProperties.isEmpty())
            {
                val items = transaction.getContentsOfItemset(
                    currentId,
                    projection = relationship.groupedProperties.map { it.property })
                    .distinct()
                    .sortedWith(propertyComparator(relationship.groupedProperties.map { it.property to it.sort }))
                JsonArray(items.map { JsonObject(it.mapValues { (_, value) -> JsonPrimitive(value) }) })
            }
            else
            {
                val (property, sort) = classificationProperties.first()
                val values = transaction.getValuesAndItemsets(currentId, property).ifEmpty {
                    transaction.classify(currentId, property)
                    transaction.getValuesAndItemsets(currentId, property)
                }
                values.toSortedMap(sort.comparing(stringSelector = { it }, intSelector = { it.toIntOrNull() }))
                    .mapValues { (_, itemsetId) ->
                        createTree(itemsetId, classificationProperties.subList(1, classificationProperties.size))
                    }
                    .let { JsonObject(it) }
            }
        }

        val json = createTree(firstNonValuedItemsetId, relationship.classificationProperties)
        call.respond(HttpStatusCode.OK, json)
    }
    finally
    {
        transaction.commit()
    }
}

fun propertyComparator(properties: List<Pair<String, SortingOrder>>) =
    Comparator<Map<String, String>> { object1, object2 ->
        for ((property, sort) in properties)
        {
            val comparator: Comparator<Map<String, String>> =
                sort.comparing(stringSelector = { it[property] }, intSelector = { it[property]?.toIntOrNull() })
            val comparison = compareValuesBy(object1, object2, comparator) { it }
            if (comparison != 0) return@Comparator comparison
        }
        return@Comparator 0
    }

private inline fun <T> SortingOrder.comparing(
    crossinline stringSelector: (T) -> Comparable<*>?,
    crossinline intSelector: (T) -> Comparable<*>?
) = when (this)
{
    SortingOrder.ASCENDING -> compareBy(stringSelector)
    SortingOrder.DESCENDING -> compareByDescending(stringSelector)
    SortingOrder.INCREASING -> compareBy(intSelector)
    SortingOrder.DECREASING -> compareByDescending(intSelector)
}

suspend fun PipelineContext<Unit, ApplicationCall>.addDataset(
    driver: Driver,
    mutableDatasetUpdateEvent: MutableSharedFlow<Unit>
)
{

    val dataset: Dataset = call.receive()
    dataset.values.find { it.size != dataset.properties.size }
        ?.run {
            call.respond(HttpStatusCode.BadRequest)
            return
        }
    val username = call.principal<UserIdPrincipal>()?.name ?: return

    val status = driver.session().writeTransaction { transaction ->
        transaction.getDatasetNode(dataset.name)?.run { return@writeTransaction HttpStatusCode.Conflict }
        transaction.addDataset(dataset, username)
        return@writeTransaction HttpStatusCode.OK
    }
    call.respond(status)
    mutableDatasetUpdateEvent.emit(Unit)
}

suspend fun PipelineContext<Unit, ApplicationCall>.deleteDataset(
    driver: Driver,
    datasetName: String,
    mutableDatasetUpdateEvent: MutableSharedFlow<Unit>
)
{
    val status = driver.session().writeTransaction { transaction ->
        val datasetUser =
            transaction.getDatasetNode(datasetName)?.username ?: return@writeTransaction HttpStatusCode.NotFound

        if (call.principal<UserIdPrincipal>()?.name != datasetUser)
            return@writeTransaction HttpStatusCode.Forbidden

        transaction.deleteDataset(datasetName)
        return@writeTransaction HttpStatusCode.OK
    }
    call.respond(status)
    mutableDatasetUpdateEvent.emit(Unit)
}

suspend fun PipelineContext<Unit, ApplicationCall>.updateDataset(
    location: DatasetLocation.Name,
    driver: Driver,
    mutableDatasetUpdateEvent: MutableSharedFlow<Unit>
)
{
    val datasetName = location.name
    val datasetItems: DatasetItems = call.receive()

    datasetItems.values.find { it.size != datasetItems.properties.size }
        ?.run {
            call.respond(HttpStatusCode.BadRequest)
            return
        }

    val status = driver.session().writeTransaction { transaction ->
        val datasetNode = transaction.getDatasetNode(datasetName) ?: return@writeTransaction HttpStatusCode.NotFound

        val username = datasetNode.username
        if (call.principal<UserIdPrincipal>()?.name != username)
            return@writeTransaction HttpStatusCode.Forbidden
        if (datasetItems.properties != datasetNode.properties)
            return@writeTransaction HttpStatusCode.Conflict

        transaction.deleteDataset(datasetName)
        transaction.recreateDataset(datasetNode, username, datasetItems)
        return@writeTransaction HttpStatusCode.OK
    }
    call.respond(status)
    mutableDatasetUpdateEvent.emit(Unit)
}
