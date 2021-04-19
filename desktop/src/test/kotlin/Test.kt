import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.auth.providers.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

internal class Test
{
    @Test
    private fun test() = runBlocking {
        HttpClient(CIO) {
            expectSuccess = false
            install(JsonFeature) {
                serializer = KotlinxSerializer()
            }

            install(Auth) {
                basic {
                    this.sendWithoutRequest = true
                    this.username = "admin"
                    this.password = "admin"
                }
            }

        }.use {
            runCatching {
                it.get<HttpStatusCode>("http://localhost:8080/login")
            }.onFailure { it.printStackTrace() }
                .getOrNull()
        }.let { println(it) }
    }
}
