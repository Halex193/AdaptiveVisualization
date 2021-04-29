package ro.halex.av

import org.neo4j.driver.Driver
import org.neo4j.driver.Transaction

fun Transaction.getDatasetNode(datasetName: String): DatasetNode?
{
    val result = run(
        "MATCH (u:User)-[:CREATED]->(d:Dataset) WHERE d.name=\$name RETURN id(d) AS id, u.username AS username d.name AS name, d.color AS color d.properties AS properties",
        mapOf("name" to datasetName)
    ).list().firstOrNull() ?: return null

    return DatasetNode(
        result["id"].asInt(),
        result["username"].asString(),
        result["name"].asString(),
        result["color"].asString(),
        result["properties"].asList { it.toString() }
    )
}

fun retrieveDatasets(driver: Driver): List<DatasetDTO>
{
    val result = driver.session()
        .run("MATCH (u:User)-[:CREATED]->(d:Dataset)-[:CONTAINS]->(i:Item) RETURN d, u.username AS username, COUNT(i) AS items ORDER BY d.name")
    return result.list()?.map { record ->
        val node = record.get(0)?.asNode() ?: error("Data missing")
        val username = record.get(1)?.asString() ?: error("Data missing")
        val items = record.get(2)?.asInt() ?: error("Data missing")
        DatasetDTO(
            node["name"].asString(),
            node["color"].asString(),
            node["properties"].asList { it.asString() },
            username,
            items
        )
    } ?: emptyList()
}

fun Transaction.getValuesAndItemsets(currentId: Int, property: String): Map<String, Int>
{
    return run(
        "MATCH (i)-[g:GROUPED_BY]->(:Group)-[:GROUP_VALUE]->(is:Itemset) WHERE id(i)=\$id AND g.property=\$property RETURN is.value, id(is)",
        mapOf("id" to currentId, "property" to property)
    ).list().associate { record -> record[0].asString() to record[1].asInt() }
}

fun Transaction.getContentsOfItemset(
    id: Int,
    projection: List<String>
): List<Map<String, String>>
{
    return run("MATCH (i)-[:CONTAINS]->(item) WHERE id(i)=\$id RETURN item", mapOf("id" to id)).list().map { record ->
        record[0].asMap { it.asString() }.filterKeys { it in projection }
    }
}

fun Transaction.classify(currentId: Int, property: String)
{
    val items = run("MATCH (s)-[:CONTAINS]-(i) WHERE id(s)=\$id RETURN id(i), i", mapOf("id" to currentId)).list()
        .map { record -> record[0].asInt() to record[1][property].asString() }

    val groupId = run(
        "MATCH (s) WHERE id(s)=\$id CREATE (s)-[gb:GROUPED_BY {property:\$property}]->(g:Group {property:\$property}) RETURN id(g)",
        mapOf("id" to currentId, "property" to property)
    ).single()[0].asInt()

    items.groupBy { it.second }.mapValues { (_, value) -> value.map { it.first } }.onEach { (value, itemIds) ->
        val itemsetId = run(
            "MATCH (s) WHERE id(s)=\$id CREATE (s)-[g:GROUP_VALUE {value:\$value}]->(i:Itemset {value:\$value}) RETURN id(i)",
            mapOf("id" to groupId, "value" to value)
        ).single()[0].asInt()

        run(
            "MATCH (i), (item) WHERE id(i)=\$id AND id(item) IN \$itemIds CREATE (i)-[:CONTAINS]->(item) ",
            mapOf("id" to itemsetId, "itemIds" to itemIds)
        )
    }
}

fun Transaction.getGroupingItemsetId(currentId: Int, property: String, value: String): Int?
{
    return run(
        "MATCH (i)-[g:GROUPED_BY]->(:Group)-[:GROUP_VALUE]->(is:Itemset) WHERE id(i)=\$id AND g.property=\$property AND is.value=\$value RETURN id(is)",
        mapOf("id" to currentId, "property" to property, "value" to value)
    ).list().firstOrNull()?.get(0)?.asInt()
}

fun Transaction.addDataset(dataset: Dataset, username: String)
{
    run(
        "CREATE (d:Dataset \$properties)",
        mapOf(
            "properties" to mapOf(
                "name" to dataset.name,
                "color" to dataset.color,
                "properties" to dataset.properties
            )
        )
    )
    run(
        "MATCH (u:User), (d:Dataset) WHERE u.username=\$username AND d.name=\$name CREATE (u)-[:CREATED]->(d)",
        mapOf(
            "username" to username,
            "name" to dataset.name
        )
    )
    run("MATCH (d) WHERE d.name = \$name UNWIND \$values AS map CREATE (d)-[:CONTAINS]->(i:Item) SET i = map",
        mapOf(
            "name" to dataset.name,
            "values" to dataset.values.map { dataset.properties.zip(it).toMap() }
        ))
}

fun Transaction.deleteDataset(datasetName: String)
{
    run(
        "MATCH path = (:Dataset {name:\$name})-[*]->() FOREACH (node IN nodes(path) | DETACH DELETE node)",
        mapOf("name" to datasetName)
    )
}

fun Transaction.recreateDataset(
    existingDataset: DatasetNode,
    username: String?,
    datasetItems: DatasetItems
)
{
    val datasetId = run(
        "CREATE (d:Dataset) SET d=\$values RETURN id(d)",
        mapOf(
            "values" to mapOf(
                "name" to existingDataset.name,
                "color" to existingDataset.color,
                "properties" to existingDataset.properties,
            )
        )
    ).single()[0].asInt()
    run(
        "MATCH (u:User), (d:Dataset) WHERE u.username=\$username AND id(d)=\$id CREATE (u)-[:CREATED]->(d)",
        mapOf(
            "username" to username,
            "id" to datasetId
        )
    )
    run("MATCH (d:Dataset) WHERE id(d) = \$id UNWIND \$values AS map CREATE (d)-[:CONTAINS]->(i:Item) SET i = map",
        mapOf(
            "id" to datasetId,
            "values" to datasetItems.values.map { datasetItems.properties.zip(it).toMap() }
        ))
}
