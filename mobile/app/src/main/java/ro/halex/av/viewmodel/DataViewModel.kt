package ro.halex.av.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import ro.halex.av.APP_TAG
import ro.halex.av.backend.*
import ro.halex.av.ui.screen.main.Module

class DataViewModel(application: Application) : AbstractViewModel(application)
{
    val mutableConnectionURL = mutableStateOf(defaultConnectionURL)
    private val mutableDatasetsLoading = mutableStateOf(false)
    private val mutableDatasets = mutableStateOf<List<DatasetDTO>?>(null)
    private val mutableSelectedDataset = mutableStateOf<DatasetDTO?>(null)
    val mutableValuedProperties = mutableStateListOf<ValuedProperty>()
    val mutableClassificationProperties = mutableStateListOf<ClassificationProperty>()
    val mutableGroupedProperties = mutableStateListOf<GroupedProperty>()

    val datasetsLoading = mutableDatasetsLoading as State<Boolean>
    val datasets = mutableDatasets as State<List<DatasetDTO>?>
    val selectedDataset = mutableSelectedDataset as State<DatasetDTO?>

    val availableProperties: List<String>
        get()
        {
            val dataset = mutableSelectedDataset.value ?: return emptyList()
            val existingProperties = setOf<List<NestingElement>>(
                mutableValuedProperties,
                mutableClassificationProperties,
                mutableGroupedProperties
            ).flatMap { it.map { nestingElement -> nestingElement.elementProperty } }.toMutableSet()

            return dataset.properties - existingProperties
        }

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

    init
    {
        viewModelScope.launch {
            resetNestingRelationShip()
            nestingRelationshipDataStore.data.firstOrNull()?.apply {
                mutableValuedProperties.addAll(valuedProperties)
                mutableClassificationProperties.addAll(classificationProperties)
                mutableGroupedProperties.addAll(groupedProperties)
            } ?: return@launch

            mutableDatasetsLoading.value = true
            val connectionURL =
                connectionDataStore.data.first().also { mutableConnectionURL.value = it }

            httpClient.getDatasets(connectionURL).also { datasets ->
                if (datasets == null)
                {
                    showToast("Could not connect to server")
                }
                else
                {
                    mutableDatasets.value = datasets

                    datasetInfoDataStore.data.first()?.let {
                        if (it in datasets)
                        {
                            mutableSelectedDataset.value = it
                        } else
                        {
                            showToast("Dataset was deleted")
                        }
                    }
                }
            }

            mutableDatasetsLoading.value = false
        }
    }

    fun resetDatasets()
    {
        mutableDatasets.value = null
        mutableSelectedDataset.value = null
        resetNestingRelationShip()
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
        if (mutableGroupedProperties.any { it.property == property })
            error("Value added for existing grouped property")
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
        availableProperties.forEach {
            addGroupedProperty(it, sortingOrder)
        }
    }
}
