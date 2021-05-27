package ro.halex.av.backend

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import ro.halex.av.ui.screen.modules.Module

sealed class NestingElement
{
    abstract val elementProperty: String?
    abstract val elementSort: SortingOrder

    abstract fun copyWithSort(sort: SortingOrder): NestingElement
}

data class SimpleProperty(val property: String?, val sort: SortingOrder = SortingOrder.ASCENDING): NestingElement()
{
    override val elementProperty: String?
        get() = property
    override val elementSort: SortingOrder
        get() = sort

    override fun copyWithSort(sort: SortingOrder): NestingElement
    {
        return copy(sort = sort)
    }
}

@Serializable
data class ValuedProperty(val property: String, @Transient val sort: SortingOrder = SortingOrder.ASCENDING, val value: String) :  NestingElement()
{
    override val elementProperty: String
        get() = property
    override val elementSort: SortingOrder
        get() = sort

    override fun copyWithSort(sort: SortingOrder): NestingElement
    {
        return copy(sort = sort)
    }
}

@Serializable
data class ClassificationProperty(
    val property: String,
    val sort: SortingOrder = SortingOrder.ASCENDING,
    val module: Module = Module.Separators
) :  NestingElement()
{
    override val elementProperty: String
        get() = property
    override val elementSort: SortingOrder
        get() = sort

    override fun copyWithSort(sort: SortingOrder): NestingElement
    {
        return copy(sort = sort)
    }
}

@Serializable
data class GroupedProperty(val property: String, val sort: SortingOrder = SortingOrder.ASCENDING) :  NestingElement()
{
    override val elementProperty: String
        get() = property
    override val elementSort: SortingOrder
        get() = sort

    override fun copyWithSort(sort: SortingOrder): NestingElement
    {
        return copy(sort = sort)
    }
}

@Serializable
data class NestingRelationship(
    val valuedProperties: List<ValuedProperty>,
    val classificationProperties: List<ClassificationProperty>,
    val groupedProperties: List<GroupedProperty>
)

@Serializable
enum class SortingOrder
{
    ASCENDING,
    DESCENDING,
    INCREASING,
    DECREASING
}

@Serializable
data class DatasetDTO(
    val name: String,
    val color: String,
    val properties: List<String>,
    val username: String,
    val items: Int
)
