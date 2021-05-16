package ro.halex.av.backend

import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.JsonElement

const val defaultConnectionURL = "http://192.168.0.193:8080"

fun createHttpClient(): HttpClient = HttpClient(CIO) {
    // expectSuccess = false

    install(JsonFeature) {
        serializer = KotlinxSerializer()
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