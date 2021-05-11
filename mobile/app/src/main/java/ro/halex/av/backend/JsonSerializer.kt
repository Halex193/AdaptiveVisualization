package ro.halex.av.backend

import androidx.datastore.core.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

inline fun <reified T> createJsonSerializer(defaultValue: T): Serializer<T>
{
    return object : Serializer<T>
    {
        override val defaultValue = defaultValue

        override suspend fun readFrom(input: InputStream): T
        {
            return Json.decodeFromString(input.readBytes().decodeToString())
        }

        override suspend fun writeTo(t: T, output: OutputStream)
        {
            runCatching { output.write(Json.encodeToString(t).encodeToByteArray()) }
                .getOrThrow()
        }
    }
}