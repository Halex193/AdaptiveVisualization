package ro.halex.av.backend

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class ValuedProperty(val property: String, val value: String, val sort: String = "incr")

@Serializable
data class ClassificationProperty(val property: String, val sort: String = "incr", @Transient val module: Int = 0)

@Serializable
data class GroupedProperty(val property: String, val sort: String = "incr")

@Serializable
data class NestingRelationship(
    val valuedProperties: List<ValuedProperty>,
    val classificationProperties: List<ClassificationProperty>,
    val groupedProperties: List<GroupedProperty>
)

@Serializable
data class DatasetDTO(
    val name: String,
    val color: String,
    val properties: List<String>,
    val username: String,
    val items: Int
)