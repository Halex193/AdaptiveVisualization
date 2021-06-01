package ro.halex.av.plugins

import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Driver
import org.neo4j.driver.GraphDatabase

fun createDriver(databaseHost: String, databaseUsername: String, databasePassword: String): Driver = GraphDatabase.driver(
    "bolt://$databaseHost:7687",
    AuthTokens.basic(databaseUsername, databasePassword)
)
