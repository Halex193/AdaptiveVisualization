package ro.halex.av

import kotlin.test.*
import org.neo4j.driver.Driver
import org.neo4j.driver.Value
import org.neo4j.driver.Values
import ro.halex.av.plugins.createDriver

class Neo4JDriverTest()
{
    private val driver: Driver = createDriver()

    @Test
    fun printGreeting()
    {
        val message = "huehue"
        driver.session().use { session ->
            val greeting =
                session.writeTransaction { tx ->
                    val result = tx.run(
                        "CREATE (a:Greeting) " +
                                "SET a.message = \$message " +
                                "RETURN a.message + ', from node ' + id(a)",
                        Values.parameters("message", message)
                    )
                    result.single()[0].asString()
                }
            println(greeting)
        }
    }

    @Test
    fun login()
    {
        val result = driver.session().run(
            "MATCH (u:User {username:\$username, password:\$password}) RETURN (u)",
                mapOf("username" to "admin", "password" to "admin")
            )

        val value = result.list().firstOrNull()
        println(value)
    }

    @Test
    fun getDataset()
    {
        val result = driver.session().run("MATCH (d:DatasetLocation), (i:Item), (d)-[x:Contains]-(i) RETURN d.name AS name, d.color AS color, i ORDER BY d.name")
        /*val value = result.list().map { it.asMap() }.map { it["i"] as  }
        println(value)*/
    }
}
