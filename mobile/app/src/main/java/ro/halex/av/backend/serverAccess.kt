package ro.halex.av.backend

import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.JsonObject

const val serverURL = "http://192.168.0.193:8080"

fun createHttpClient(): HttpClient = HttpClient(CIO) {
    // expectSuccess = false

    install(JsonFeature) {
        serializer = KotlinxSerializer()
    }
}

suspend fun HttpClient.getDatasets(): List<DatasetDTO>?
{
    return runCatching {
        get<List<DatasetDTO>>("$serverURL/dataset")
    }.onFailure { Log.e("ServerAccess", it.stackTraceToString()) }.getOrNull()
}

suspend fun HttpClient.getDataset(name: String, nestingRelationship: NestingRelationship): JsonObject?
{
    return runCatching {
        get<JsonObject>("$serverURL/$name".encodeURLPath())
    }.onFailure { Log.e("ServerAccess", it.stackTraceToString()) }.getOrNull()
}