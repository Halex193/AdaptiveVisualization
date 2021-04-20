import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.auth.providers.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.utils.io.errors.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

suspend fun login(configuration: Configuration): HttpStatusCode?
{
    return HttpClient(CIO) {
        expectSuccess = false

        install(Auth) {
            basic {
                this.sendWithoutRequest = true
                this.username = configuration.username
                this.password = configuration.password
            }
        }
    }.use { runCatching { it.get<HttpStatusCode>(configuration.URL + "/login") }.getOrNull() }
}

fun createHttpClient(configuration: Configuration) = HttpClient(CIO) {
    expectSuccess = false

    install(JsonFeature) {
        serializer = KotlinxSerializer()
    }

    install(WebSockets)

    install(Auth) {
        basic {
            this.sendWithoutRequest = true
            this.username = configuration.username
            this.password = configuration.password
        }
    }
}

suspend fun addDataset(
    client: HttpClient,
    configuration: Configuration,
    name: String,
    color: String,
    datasetFile: File
): HttpStatusCode?
{
    @Serializable
    data class Dataset(
        val name: String,
        val color: String,
        val properties: List<String>,
        val values: List<List<String>>
    )

    val rows: List<List<String>> = runCatching { csvReader().readAll(datasetFile) }.getOrNull() ?: return HttpStatusCode.BadRequest
    return runCatching {
        client.post<HttpStatusCode>(configuration.URL + "/dataset") {
            contentType(ContentType.Application.Json)
            body = Dataset(
                name,
                color,
                rows[0],
                rows.subList(1, rows.size)
            )
        }
    }.getOrNull()
}

suspend fun updateDataset(
    client: HttpClient,
    configuration: Configuration,
    name: String,
    datasetFile: File
): HttpStatusCode?
{
    @Serializable
    data class Dataset(
        val properties: List<String>,
        val values: List<List<String>>
    )

    val rows: List<List<String>> = runCatching { csvReader().readAll(datasetFile) }.getOrNull() ?: return HttpStatusCode.BadRequest
    return runCatching {
        client.put<HttpStatusCode>(configuration.URL + "/dataset/$name".encodeURLPath()) {
            contentType(ContentType.Application.Json)
            body = Dataset(
                rows[0],
                rows.subList(1, rows.size)
            )
        }
    }.getOrNull()
}

suspend fun deleteDataset(
    client: HttpClient,
    configuration: Configuration,
    name: String,
): HttpStatusCode?
{
    return runCatching {
        client.delete<HttpStatusCode>(configuration.URL + "/dataset/$name".encodeURLPath())
    }.getOrNull()
}

fun datasetFlow(client: HttpClient, configuration: Configuration): Flow<List<Dataset>>
{
    return flow {
        client.webSocket(configuration.webSocketURL + "/dataset")
        {
            incoming.consumeAsFlow()
                .mapNotNull { it as? Frame.Text }
                .map { Json.decodeFromString<List<Dataset>>(it.readText()) }
                .catch {
                    close()
                    throw it
                }
                .collect { emit(it) }
        }
        throw IOException("WebSocket disconnected")
    }.retryWhen { cause, _ ->
        println(cause)
        delay(5000)
        true
    }
}
