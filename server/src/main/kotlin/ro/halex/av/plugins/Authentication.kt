package ro.halex.av.plugins

import io.ktor.application.*
import io.ktor.auth.*
import org.neo4j.driver.Driver
import org.neo4j.driver.Query

fun Application.configureAuthentication(driver: Driver)
{
    install(Authentication) {
        basic(name = "administrator") {
            realm = "AV Server"
            validate { credentials ->
                //TODO password hashing
                val result = driver.session().run(
                    "MATCH (u:User {username:\$username, password:\$password}) RETURN (u)",
                    mapOf("username" to credentials.name, "password" to credentials.password)
                )
                result.list().firstOrNull()?.let { UserIdPrincipal(credentials.name) }
            }
        }
    }
}
