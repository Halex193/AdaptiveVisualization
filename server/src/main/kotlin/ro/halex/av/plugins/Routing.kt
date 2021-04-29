@file:OptIn(KtorExperimentalLocationsAPI::class)

package ro.halex.av.plugins

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onSubscription
import kotlinx.serialization.SerializationException
import org.neo4j.driver.Driver
import ro.halex.av.*

@Location("/dataset")
class DatasetLocation
{
    @Location("/{name}")
    class Name(val name: String, val datasetLocation: DatasetLocation)
}

fun Application.configureRouting(driver: Driver)
{
    install(Locations) {}

    install(StatusPages) {
        exception<SerializationException> { cause ->
            call.respond(HttpStatusCode.BadRequest)
        }
    }

    val mutableDatasetUpdateEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val datasetUpdateEvent: Flow<Unit> = mutableDatasetUpdateEvent.onSubscription { emit(Unit) }

    routing {
        get("/") {
            call.respondText("Server working!")
        }
        get<DatasetLocation> {
            getDatasets(driver)
        }
        get<DatasetLocation.Name> { location ->
            val datasetName = location.name
            getClassifiedDataset(driver, datasetName)
        }
        webSocket("/dataset") {
            updateWebSocket(datasetUpdateEvent, driver)
        }

        authenticate("administrator") {
            get("/login") {
                call.respondText("Credentials valid!")
            }
            post<DatasetLocation> {
                addDataset(driver, mutableDatasetUpdateEvent)
            }

            put<DatasetLocation.Name> { location ->
                updateDataset(location, driver, mutableDatasetUpdateEvent)
            }

            delete<DatasetLocation.Name> {
                val datasetName = it.name
                deleteDataset(driver, datasetName, mutableDatasetUpdateEvent)
            }
        }
    }
}
