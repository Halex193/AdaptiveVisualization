package ro.halex.av.backend

import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement

const val defaultConnectionURL = "http://halex193.go.ro"

fun createHttpClient(): HttpClient = HttpClient(CIO) {
    // expectSuccess = false

    install(JsonFeature) {
        serializer = KotlinxSerializer()
    }

    install(HttpTimeout) {
        requestTimeoutMillis = 60000
    }
}

suspend fun HttpClient.getDatasets(connectionURL: String): List<DatasetDTO>?
{
    return runCatching {
        get<List<DatasetDTO>>("$connectionURL/dataset")
    }.onFailure { Log.e("ServerAccess", it.stackTraceToString()) }.getOrNull()
}

suspend fun HttpClient.getDataset(connectionURL: String, name: String, nestingRelationship: NestingRelationship): JsonElement?
{
    return runCatching {
        get<JsonElement>("$connectionURL/dataset/$name".encodeURLPath()) {
            contentType(ContentType.Application.Json)
            body = nestingRelationship
        }
    }.onFailure { Log.e("ServerAccess", it.stackTraceToString()) }.getOrNull()
}