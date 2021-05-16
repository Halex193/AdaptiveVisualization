package ro.halex.av.plugins

import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Driver
import org.neo4j.driver.GraphDatabase

fun createDriver(database: String): Driver = GraphDatabase.driver(
    "bolt://$database:7687",
    AuthTokens.basic("neo4j", "password")
)
