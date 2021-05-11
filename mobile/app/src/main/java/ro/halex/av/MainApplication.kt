package ro.halex.av

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import io.ktor.client.*
import kotlinx.serialization.json.JsonObject
import ro.halex.av.backend.*

const val APP_TAG = "AdaptiveVisualizationApplication"

class MainApplication : Application()
{
    lateinit var httpClient: HttpClient
    val connectionDataStore: DataStore<String?> by dataStore(
        "connection",
        createJsonSerializer(defaultConnectionURL)
    )
    val datasetInfoDataStore: DataStore<DatasetDTO?> by dataStore(
        "datasetInfo",
        createJsonSerializer(null)
    )
    val datasetTreeDataStore: DataStore<JsonObject> by dataStore(
        "datasetTree",
        createJsonSerializer(JsonObject(emptyMap()))
    )

    val nestingRelationshipDataStore: DataStore<NestingRelationship?> by dataStore(
        "datasetTree",
        createJsonSerializer(null)
    )

    override fun onCreate()
    {
        super.onCreate()
        httpClient = createHttpClient()
    }
}