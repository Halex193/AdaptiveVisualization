package ro.halex.av.viewmodel

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import ro.halex.av.backend.*
import ro.halex.av.ui.screen.main.Module

class DataViewModel(application: Application) : AbstractViewModel(application)
{
    val mutableConnectionURL = mutableStateOf<String?>(null)
    private val mutableDatasetsLoading = mutableStateOf(false)
    private val mutableDatasets = mutableStateOf<List<DatasetDTO>?>(null)
    private val mutableSelectedDataset = mutableStateOf<DatasetDTO?>(null)
    val mutableValuedProperties = mutableStateListOf<ValuedProperty>()
    val mutableClassificationProperties = mutableStateListOf<ClassificationProperty>()
    val mutableGroupedProperties = mutableStateListOf<GroupedProperty>()

    val datasetsLoading = mutableDatasetsLoading as State<Boolean>
    val datasets = mutableDatasets as State<List<DatasetDTO>?>
    val selectedDataset = mutableSelectedDataset as State<DatasetDTO?>

    val nestingRelationshipEmpty: Boolean
        get() = nestingRelationship.let {
            it.valuedProperties.isEmpty() &&
                    it.classificationProperties.isEmpty() &&
                    it.groupedProperties.isEmpty()
        }

    val valuedPropertiesLeafNode: LeafNode
        get() = LeafNode(listOf(mutableValuedProperties.associate { it.property to it.value }))

    val helpTree: Node get() = helpNode(nestingRelationship)

    val nestingRelationship
        get() = NestingRelationship(
            mutableValuedProperties.toList(),
            mutableClassificationProperties.toList(),
            mutableGroupedProperties.toList()
        )


    private suspend fun testingData()
    {
        //TODO remove this, testing only
        mutableConnectionURL.value = defaultConnectionURL
        getDatasets()
        val connectionURL = requireNotNull(mutableConnectionURL.value)
        mutableDatasetsLoading.value = true
        resetDatasets()
        httpClient.getDatasets(connectionURL)
            ?.let {
                mutableDatasets.value = it
                mutableSelectedDataset.value = it.first()
            }
            ?: run { showToast("Could not connect to server") }
        mutableDatasetsLoading.value = false
    }

    init
    {
        viewModelScope.launch {
            testingData()
            /*mutableConnectionURL.value = connectionDataStore.data.first() ?: ""
            val datasetInfo = datasetInfoDataStore.data.first() ?: return@launch
            getDatasets()
            mutableSelectedDataset.value = datasetInfo
            val relationship = nestingRelationshipDataStore.data.firstOrNull() ?: return@launch
            val elementList =
                relationship.valuedProperties + relationship.classificationProperties + relationship.groupedProperties
            mutableNestingElementList.addAll(elementList)*/
        }
    }

    fun resetDatasets()
    {
        resetNestingRelationShip()
        mutableDatasets.value = null
        mutableSelectedDataset.value = null
    }

    private fun resetNestingRelationShip()
    {
        mutableValuedProperties.clear()
        mutableClassificationProperties.clear()
        mutableGroupedProperties.clear()
    }

    fun getDatasets()
    {
        val connectionURL = requireNotNull(mutableConnectionURL.value)
        mutableDatasetsLoading.value = true
        resetDatasets()

        viewModelScope.launch()
        {
            httpClient.getDatasets(connectionURL)
                ?.let { mutableDatasets.value = it }
                ?: run { showToast("Could not connect to server") }
            mutableDatasetsLoading.value = false
        }
    }

    fun selectDataset(dataset: DatasetDTO)
    {
        resetNestingRelationShip()
        mutableSelectedDataset.value = dataset
    }

    fun getAvailableProperties(): List<String>
    {
        val dataset = requireNotNull(mutableSelectedDataset.value)
        val existingProperties = setOf<List<NestingElement>>(
            mutableValuedProperties,
            mutableClassificationProperties,
            mutableGroupedProperties
        ).flatMap { it.map { nestingElement -> nestingElement.elementProperty } }.toMutableSet()

        return dataset.properties - existingProperties
    }

    suspend fun getAvailableValues(property: String, sort: SortingOrder): List<String>
    {
        val dataset = requireNotNull(mutableSelectedDataset.value)
        val connectionURL = requireNotNull(mutableConnectionURL.value)

        val groupedProperties = listOf(
            GroupedProperty(
                property,
                sort
            )
        )
        val nestingRelationship =
            NestingRelationship(mutableValuedProperties.toList(), emptyList(), groupedProperties)

        val jsonArray = httpClient.getDataset(
            connectionURL,
            dataset.name,
            nestingRelationship
        )?.jsonArray ?: run {
            resetDatasets()
            return emptyList()
        }
        return jsonArray
            .asSequence()
            .mapNotNull { it.jsonObject[property]?.jsonPrimitive?.contentOrNull }
            .toList()
    }

    fun save(data: JsonElement, onSaveFinished: () -> Unit)
    {
        val dataset = requireNotNull(mutableSelectedDataset.value)
        val connectionURL = requireNotNull(mutableConnectionURL.value)

        viewModelScope.launch {
            connectionDataStore.updateData { connectionURL }
            datasetInfoDataStore.updateData { dataset }
            datasetTreeDataStore.updateData { data }
            nestingRelationshipDataStore.updateData { nestingRelationship }
            onSaveFinished()
        }
    }

    suspend fun getData(): JsonElement?
    {
        val dataset = requireNotNull(mutableSelectedDataset.value)
        val connectionURL = requireNotNull(mutableConnectionURL.value)
        return httpClient.getDataset(
            connectionURL,
            dataset.name,
            nestingRelationship
        ) ?: run {
            showToast("Server connection failed")
            resetDatasets()
            null
        }
    }

    fun addValuedProperty(property: String, value: String)
    {
        if (mutableValuedProperties.any { it.property == property })
            error("Value added for existing valued property")
        mutableValuedProperties.add(ValuedProperty(property = property, value = value))
    }

    fun deleteValuedProperty(valuedProperty: ValuedProperty)
    {
        mutableValuedProperties.remove(valuedProperty)
    }

    fun addClassificationProperty(property: String, module: Module, sortingOrder: SortingOrder)
    {
        if (mutableClassificationProperties.any { it.property == property })
            error("Value added for existing classification property")
        mutableClassificationProperties.add(ClassificationProperty(property, sortingOrder, module))
    }

    fun deleteClassificationProperty(classificationProperty: ClassificationProperty)
    {
        mutableClassificationProperties.remove(classificationProperty)
    }

    fun swapClassificationProperties(index: Int)
    {
        val list = mutableClassificationProperties
        list[index] = list[index + 1].also { list[index + 1] = list[index] }
    }

    fun addGroupedProperty(property: String, sortingOrder: SortingOrder)
    {
        mutableGroupedProperties.add(GroupedProperty(property, sortingOrder))
    }

    fun deleteGroupedProperty(groupedProperty: GroupedProperty)
    {
        mutableGroupedProperties.remove(groupedProperty)
    }

    fun swapGroupedProperties(index: Int)
    {
        val list = mutableGroupedProperties
        list[index] = list[index + 1].also { list[index + 1] = list[index] }
    }

    fun fillGroupedProperties(sortingOrder: SortingOrder)
    {
        getAvailableProperties().forEach {
            addGroupedProperty(it, sortingOrder)
        }
    }
}
