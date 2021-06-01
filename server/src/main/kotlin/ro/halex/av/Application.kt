package ro.halex.av

import io.ktor.application.*
import org.neo4j.driver.Driver
import ro.halex.av.plugins.*

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false)
{
    val databaseHost = environment.config.propertyOrNull("ktor.database_host")?.getString() ?: error("No database hostname provided")
    val databaseUsername = environment.config.propertyOrNull("ktor.database_username")?.getString() ?: error("No database username provided")
    val databasePassword = environment.config.propertyOrNull("ktor.database_password")?.getString() ?: error("No database password provided")
    val driver: Driver = createDriver(databaseHost, databaseUsername, databasePassword)

    configureAuthentication(driver)
    configureSockets()
    configureRouting(driver)
    configureMonitoring()
    configureSerialization()

    environment.monitor.subscribe(ApplicationStopped) {
        driver.close()
    }
}
