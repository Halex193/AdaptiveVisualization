@file:OptIn(KtorExperimentalLocationsAPI::class)

package ro.halex.av.plugins

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onSubscription
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.neo4j.driver.Driver
import org.neo4j.driver.Transaction

interface NestingRelationshipElement

@Serializable
data class ValuedProperty(val property: String, val value: String, val sort: String = "incr")

@Serializable
data class ClassificationProperty(val property: String, val sort: String = "incr")

@Serializable
data class GroupedProperty(val property: String, val sort: String = "incr")

@Serializable
data class NestingRelationship(
    val valuedProperties: List<ValuedProperty>,
    val classificationProperties: List<ClassificationProperty>,
    val groupedProperties: List<GroupedProperty>
)

@Serializable
data class Dataset(
    val name: String,
    val color: String,
    val properties: List<String>,
    val username: String,
    val items: Int
)

@OptIn(ExperimentalStdlibApi::class)
fun Application.configureRouting(driver: Driver)
{
    install(Locations) {}

    install(StatusPages) {
        exception<SerializationException> { cause ->
            call.respond(HttpStatusCode.BadRequest)
        }
        /*exception<AuthorizationException> { cause ->
            call.respond(HttpStatusCode.Forbidden)
        }*/

    }

    val mutableDatasetUpdateEvent =
        MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val datasetUpdateEvent: Flow<Unit> = mutableDatasetUpdateEvent.onSubscription { emit(Unit) }

    fun getDatasets(): List<Dataset>
    {
        val result = driver.session()
            .run("MATCH (u:User)-[:CREATED]->(d:Dataset)-[:CONTAINS]->(i:Item) RETURN d, u.username AS username, COUNT(i) AS items ORDER BY d.name")
        return result.list()?.map { record ->
            val node = record.get(0)?.asNode() ?: error("Data missing")
            val username = record.get(1)?.asString() ?: error("Data missing")
            val items = record.get(2)?.asInt() ?: error("Data missing")
            Dataset(
                node["name"].asString(),
                node["color"].asString(),
                node["properties"].asList { it.asString() },
                username,
                items
            )
        } ?: emptyList()
    }

    routing {
        get("/") {
            call.respondText("Server working!")
        }
        get<DatasetLocation> {
            call.respondText(Json.encodeToString(getDatasets()), ContentType.Application.Json, HttpStatusCode.OK)
        }
        get<DatasetLocation.Name> { location ->
            val datasetName = location.name
            val relationship = call.receive<NestingRelationship>()
            val result = driver.session().run(
                "MATCH (d:Dataset) WHERE d.name=\$name RETURN id(d), d.properties",
                mapOf("name" to datasetName)
            ).single()
            val datasetId = result[0].asInt()
            val properties = result[1].asList { it.asString() }
            if (
                !relationship.valuedProperties.all { it.property in properties } ||
                !relationship.classificationProperties.all { it.property in properties } ||
                !relationship.groupedProperties.all { it.property in properties }
            )
            {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            val transaction = driver.session().beginTransaction()
            val firstNonValuedItemsetId =
                relationship.valuedProperties
                    .fold(datasetId) { currentId, (property, value, _) ->
                        transaction.getGroupingItemsetId(currentId, property, value) ?: run {
                            transaction.classify(currentId, property)
                            transaction.getGroupingItemsetId(currentId, property, value)
                                ?: error("Classification failed")
                        }
                    }

            fun createTree(currentId: Int, classificationProperties: List<ClassificationProperty>): JsonElement
            {
                return if (classificationProperties.isEmpty())
                {
                    val items = transaction.getContentsOfItemset(
                        currentId,
                        projection = relationship.groupedProperties.map { it.property }).distinct()
                    JsonArray(items.map { JsonObject(it.mapValues { (_, value) -> JsonPrimitive(value) }) })
                }
                else
                {
                    val property = classificationProperties.first().property
                    val values = transaction.getValuesAndItemsets(currentId, property).ifEmpty {
                        transaction.classify(currentId, property)
                        transaction.getValuesAndItemsets(currentId, property)
                    }
                    values.mapValues { (_, itemsetId) ->
                        createTree(itemsetId, classificationProperties.subList(1, classificationProperties.size))
                    }.let { JsonObject(it) }
                }
            }

            val json = createTree(firstNonValuedItemsetId, relationship.classificationProperties)
            transaction.commit()
            call.respond(HttpStatusCode.OK, json)
        }
        webSocket("/dataset") {
            datasetUpdateEvent.collect {
                send(Json.encodeToString(getDatasets()))
            }
        }
        authenticate("administrator") {
            get("/login") {
                call.respondText("Credentials valid!")
            }
            post<DatasetLocation> {

                @Serializable
                data class Dataset(
                    val name: String,
                    val color: String,
                    val properties: List<String>,
                    val values: List<List<String>>
                )

                val dataset: Dataset = call.receive()
                dataset.values.find { it.size != dataset.properties.size }
                    ?.run {
                        call.respond(HttpStatusCode.BadRequest)
                        return@post
                    }
                val username = call.principal<UserIdPrincipal>()?.name ?: return@post

                val status = driver.session().writeTransaction { transaction ->
                    val result =
                        transaction.run("MATCH (d:Dataset) WHERE d.name=\$name RETURN d", mapOf("name" to dataset.name))
                    result.list().firstOrNull()?.run { return@writeTransaction HttpStatusCode.Conflict }
                    transaction.run(
                        "CREATE (d:Dataset \$properties)",
                        mapOf(
                            "properties" to mapOf(
                                "name" to dataset.name,
                                "color" to dataset.color,
                                "properties" to dataset.properties
                            )
                        )
                    )
                    transaction.run(
                        "MATCH (u:User), (d:Dataset) WHERE u.username=\$username AND d.name=\$name CREATE (u)-[:CREATED]->(d)",
                        mapOf(
                            "username" to username,
                            "name" to dataset.name
                        )
                    )
                    transaction.run("MATCH (d) WHERE d.name = \$name UNWIND \$values AS map CREATE (d)-[:CONTAINS]->(i:Item) SET i = map",
                        mapOf(
                            "name" to dataset.name,
                            "values" to dataset.values.map { dataset.properties.zip(it).toMap() }
                        ))
                    return@writeTransaction HttpStatusCode.OK
                }
                call.respond(status)
                mutableDatasetUpdateEvent.emit(Unit)
            }

            put<DatasetLocation.Name> {
                val datasetName = it.name

                @Serializable
                data class DatasetItems(val properties: List<String>, val values: List<List<String>>)

                val datasetItems: DatasetItems = call.receive()

                datasetItems.values.find { it.size != datasetItems.properties.size }
                    ?.run {
                        call.respond(HttpStatusCode.BadRequest)
                        return@put
                    }

                val status = driver.session().writeTransaction { transaction ->
                    val result = transaction.run(
                        "MATCH (u:User)-[:CREATED]->(d:Dataset) WHERE d.name=\$name RETURN u.username AS username, d.properties AS properties",
                        mapOf("name" to datasetName)
                    )
                    val existingDataset = result.list().firstOrNull() ?: return@writeTransaction HttpStatusCode.NotFound

                    if (call.principal<UserIdPrincipal>()?.name != existingDataset["username"].asString())
                        return@writeTransaction HttpStatusCode.Forbidden

                    val existingProperties = existingDataset["properties"].asList { it.asString() }
                    if (datasetItems.properties != existingProperties)
                        return@writeTransaction HttpStatusCode.Conflict

                    transaction.run(
                        "MATCH (d:Dataset)-[:CONTAINS]->(i:Item) WHERE d.name=\$name DETACH DELETE i",
                        mapOf("name" to datasetName)
                    )
                    transaction.run("MATCH (d) WHERE d.name = \$name UNWIND \$values AS map CREATE (d)-[:CONTAINS]->(i:Item) SET i = map",
                        mapOf(
                            "name" to datasetName,
                            "values" to datasetItems.values.map { datasetItems.properties.zip(it).toMap() }
                        ))
                    return@writeTransaction HttpStatusCode.OK
                }
                call.respond(status)
                mutableDatasetUpdateEvent.emit(Unit)
            }

            delete<DatasetLocation.Name> {
                val datasetName = it.name

                val status = driver.session().writeTransaction { transaction ->
                    val result = transaction.run(
                        "MATCH (u:User)-[:CREATED]->(d:Dataset) WHERE d.name=\$name RETURN u.username AS username",
                        mapOf("name" to datasetName)
                    )
                    val existingDataset = result.list().firstOrNull() ?: return@writeTransaction HttpStatusCode.NotFound

                    if (call.principal<UserIdPrincipal>()?.name != existingDataset["username"].asString())
                        return@writeTransaction HttpStatusCode.Forbidden

                    transaction.run(
                        "MATCH (d:Dataset)-[:CONTAINS]->(i:Item) WHERE d.name=\$name DETACH DELETE d, i",
                        mapOf("name" to datasetName)
                    )
                    return@writeTransaction HttpStatusCode.OK
                }
                call.respond(status)
                mutableDatasetUpdateEvent.emit(Unit)
            }
        }
    }
}

private fun Transaction.getValuesAndItemsets(currentId: Int, property: String): Map<String, Int>
{
    return run(
        "MATCH (i)-[g:GROUPED]->(is:Itemset) WHERE id(i)=\$id RETURN g.value, id(is)",
        mapOf("id" to currentId)
    ).list()
        .map { record -> record[0].asString() to record[1].asInt() }.toMap()
}

fun Transaction.getContentsOfItemset(
    id: Int,
    projection: List<String>
): List<Map<String, String>>
{
    return run("MATCH (i)-[:CONTAINS]->(item) WHERE id(i)=\$id RETURN item", mapOf("id" to id)).list().map { record ->
        record[0].asMap { it.asString() }.filterKeys { it in projection }
    }
}

private fun Transaction.classify(currentId: Int, property: String)
{
    val items = run("MATCH (s)-[:CONTAINS]-(i) WHERE id(s)=\$id RETURN id(i), i", mapOf("id" to currentId)).list()
        .map { record -> record[0].asInt() to record[1][property].asString() }


    items.groupBy { it.second }.mapValues { (_, value) -> value.map { it.first } }.onEach { (value, itemIds) ->
        val itemsetId = run(
            "MATCH (s) WHERE id(s)=\$id CREATE (s)-[g:GROUPED {property:\$property,value:\$value}]->(i:Itemset) RETURN id(i)",
            mapOf("id" to currentId, "property" to property, "value" to value)
        ).single()[0].asInt()

        run(
            "MATCH (i), (item) WHERE id(i)=\$id AND id(item) IN \$itemIds CREATE (i)-[:CONTAINS]->(item) ",
            mapOf("id" to itemsetId, "itemIds" to itemIds)
        )
    }
}

private fun Transaction.getGroupingItemsetId(currentId: Int, property: String, value: String): Int?
{
    return run(
        "MATCH (i)-[g:GROUPED]->(i2) WHERE id(i)=\$id AND g.property=\$property AND g.value=\$value RETURN id(i2)",
        mapOf("id" to currentId, "property" to property, "value" to value)
    ).list().firstOrNull()?.get(0)?.asInt()
}

@Location("/dataset")
class DatasetLocation
{
    @Location("/{name}")
    class Name(val name: String, val datasetLocation: DatasetLocation)
}

@Location("/location/{name}")
class MyLocation(val name: String, val arg1: Int = 42, val arg2: String = "default")

@Location("/type/{name}")
data class Type(val name: String)
{
    @Location("/edit")
    data class Edit(val type: Type)

    @Location("/list/{page}")
    data class List(val type: Type, val page: Int)
}

class AuthenticationException : RuntimeException()
class AuthorizationException : RuntimeException()
