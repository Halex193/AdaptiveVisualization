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
import kotlinx.serialization.json.Json
import org.neo4j.driver.Driver
import org.neo4j.driver.Transaction

@Serializable
data class NestingRelationshipElement(val property: String, val value: String? = null, val sort: String)

typealias NestingRelationship = List<NestingRelationshipElement>

interface Node

@Serializable
data class InnerNode(val children: Map<String, Node>) : Node

@Serializable
data class LeafNode(val data: List<Map<String, String>>) : Node

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

    fun Transaction.getDataByRelationship(
        nestingRelationship: NestingRelationship,
        datasetName: String
    ): List<Map<String, String>>
    {
        val query = buildString {
            append("MATCH (d:Dataset)")
            for (i in nestingRelationship.indices)
                append("-[g$i:GROUPED]->()")

            append("-[:CONTAINS]-(i:Item) ")
            append("WHERE d.name=\$name ")
            for (i in nestingRelationship.indices)
                append("AND g$i=\$g$i ")
            append("RETURN i")
        }
        val parameters = buildMap<String, String> {
            put("name", datasetName)
            nestingRelationship.forEachIndexed { index, (property, value, _) ->
                put("g${index}.property", property)
                put("g${index}.value", value ?: error("Value was null"))
            }
        }
        return run(query, parameters).list().map { record ->
            record[0].asMap { it.asString() } - nestingRelationship.map { it.property }
        }
    }

    fun Transaction.getValuesByRelationship(nestingRelationship: NestingRelationship, datasetName: String): List<String>
    {
        val query = buildString {
            append("MATCH (d:Dataset)")
            for (i in nestingRelationship.indices)
                append("-[g$i:GROUPED]->()")

            append("WHERE d.name=\$name ")
            for (i in 0 until nestingRelationship.size - 1)
                append("AND g$i=\$g$i ")
            append("RETURN g${nestingRelationship.size - 1}.value")
        }
        val parameters = buildMap<String, String> {
            put("name", datasetName)
            nestingRelationship.forEachIndexed { index, (property, value, _) ->
                put("g${index}.property", property)
                put("g${index}.value", value ?: error("Value was null"))
            }
        }
        return run(query, parameters).list().map { record -> record[0].asString() }
    }

    fun Transaction.getValuesByRelationshipOrClassify(
        nestingRelationship: NestingRelationship,
        datasetName: String
    ): List<String>
    {
        return this.getValuesByRelationship(nestingRelationship, datasetName).ifEmpty {
            val parentNestingRelationship = nestingRelationship.slice(0 until nestingRelationship.size - 1)
            val data = getDataByRelationship(parentNestingRelationship, datasetName)
            if (data.isEmpty())
                error("Previous dataset was not classified")
            val property = nestingRelationship.last().property
            data.groupBy { it[property] ?: error("Null value found") }.onEach { (value, items) ->
                val query = buildString {
                    append("MATCH (d:Dataset)")
                    for (i in 0 until parentNestingRelationship.size - 1)
                        append("-[g$i:GROUPED]->()")
                    append("-[g${parentNestingRelationship.size - 1}:GROUPED]->(p:Itemset), (i:Item) ")
                    append("WHERE d.name=\$name AND i=\$item")
                    for (i in parentNestingRelationship.indices)
                        append("AND g$i=\$g$i ")
                    append("CREATE (p)-[g:GROUPED {property=\$property,value=\$value}]->(:Itemset)-[:CONTAINS]->(i)")
                    //TOD check if it gives an error because it already exists
                }
                val parameters = buildMap<String, Any> {
                    put("name", datasetName)
                    put("property", property)
                    put("value", value)
                    nestingRelationship.forEachIndexed { index, (property, value, _) ->
                        put("g${index}.property", property)
                        put("g${index}.value", value ?: error("Value was null"))
                    }
                }.toMutableMap()
                items.forEach { item ->
                    run(query, parameters.also { it["item"] = item })
                }
            }.keys.toList()
        }
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
            val relationship = call.receive<List<NestingRelationshipElement>>()
            /*val result = driver.session().run(
                "MATCH (d:Dataset) WHERE d.name=\$name RETURN d.properties",
                mapOf("name" to datasetName)
            )
            val properties = result.single()[0].asList { it.asString() }*/
            //TODO validate properties and continuity of valued properties

            fun createTree(nestingRelationship: NestingRelationship, transaction: Transaction): Node
            {
                val index = nestingRelationship.indexOfFirst { it.value == null } - 1
                return if (index == nestingRelationship.size)
                {
                    LeafNode(transaction.getDataByRelationship(nestingRelationship, datasetName))
                }
                else
                {
                    val valuesRelationship = nestingRelationship.slice(0..index)
                    val values =
                        transaction.getValuesByRelationship(valuesRelationship, datasetName)
                    values.associateWith { value ->
                        LeafNode(
                            transaction.getDataByRelationship(
                                valuesRelationship.toMutableList()
                                    .also { it[index] = it[index].copy(value = value) },
                                datasetName
                            )
                        )
                    }.let { InnerNode(it) }
                }
            }
            val transaction = driver.session().beginTransaction()
            call.respond(HttpStatusCode.OK, createTree(relationship, transaction))
            transaction.commit()
        }
        authenticate("administrator") {
            webSocket("/dataset") {
                datasetUpdateEvent.collect {
                    send(Json.encodeToString(getDatasets()))
                }
            }
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
