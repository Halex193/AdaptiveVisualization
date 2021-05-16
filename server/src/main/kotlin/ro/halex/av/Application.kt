package ro.halex.av

import io.ktor.application.*
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Driver
import org.neo4j.driver.GraphDatabase
import ro.halex.av.plugins.*

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

/**
 * Please note that you can use any other name instead of *module*.
 * Also note that you can have more then one modules in your application.
 * */
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false)
{
    val database = environment.config.propertyOrNull("ktor.database")?.getString() ?: "localhost"
    val driver: Driver = createDriver(database)
    configureAuthentication(driver)
    configureSockets()
    configureRouting(driver)
    configureMonitoring()
    configureSerialization()

    environment.monitor.subscribe(ApplicationStopped) {
        driver.close()
    }
}
