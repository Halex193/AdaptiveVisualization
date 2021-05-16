package ro.halex.av.viewmodel

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import ro.halex.av.backend.*
import ro.halex.av.backend.NestingElement.*

class DataViewModel(application: Application) : AbstractViewModel(application)
{
    val mutableConnectionURL = mutableStateOf("")
    private val mutableDatasets = mutableStateOf<List<DatasetDTO>?>(null)
    val message = mutableStateOf<String?>(null)
    private val mutableSelectedDataset = mutableStateOf<DatasetDTO?>(null)
    val mutableNestingElementList = mutableStateListOf<NestingElement>()

    val datasets = mutableDatasets as State<List<DatasetDTO>?>
    val selectedDataset = mutableSelectedDataset as State<DatasetDTO?>

    init
    {
        viewModelScope.launch {
            mutableConnectionURL.value = connectionDataStore.data.first() ?: ""
        }
    }

    fun getDatasets()
    {
        viewModelScope.launch {
            val connectionURL = mutableConnectionURL.value
            val datasets = httpClient.getDatasets(connectionURL) ?: run {
                message.value = "Could not connect to server"
                return@launch
            }
            mutableDatasets.value = datasets
        }
    }

    fun selectDataset(dataset: DatasetDTO)
    {
        mutableSelectedDataset.value = dataset
    }

    fun addNestingElement()
    {
        mutableNestingElementList.add(SimpleProperty(null))
    }

    fun removeNestingElement(index: Int)
    {
        mutableNestingElementList.removeAt(index)
    }

    fun getAvailableProperties(nestingElement: NestingElement): List<String>
    {
        val dataset = mutableSelectedDataset.value ?: error("Dataset not selected")
        return dataset.properties - mutableNestingElementList.mapNotNull { it.elementProperty }
            .filterNot { it == nestingElement.elementProperty }
    }

    suspend fun getAvailableValues(index: Int): List<String>?
    {
        val dataset = mutableSelectedDataset.value ?: error("Dataset not selected")
        val valuedProperties = mutableNestingElementList.subList(0, index)
            .filterIsInstance<ValuedProperty>()
        val currentElement = mutableNestingElementList[index]
        val property = currentElement.elementProperty ?: error("Property was not chosen")
        val groupedProperties = listOf(
            GroupedProperty(
                property,
                currentElement.elementSort
            )
        )
        val nestingRelationship =
            NestingRelationship(valuedProperties, emptyList(), groupedProperties)

        val jsonArray = httpClient.getDataset(
            mutableConnectionURL.value,
            dataset.name,
            nestingRelationship
        )?.jsonArray ?: return null
        return jsonArray.asSequence()
            .mapNotNull { it.jsonObject[property]?.jsonPrimitive?.contentOrNull }.toList()
    }

    fun save(onSaveFinished: () -> Unit)
    {
        val dataset = mutableSelectedDataset.value ?: error("Dataset not selected")
        viewModelScope.launch {
            connectionDataStore.updateData { mutableConnectionURL.value }
            datasetInfoDataStore.updateData { dataset }
            val elementList = mutableNestingElementList
            val valuedProperties = elementList.filterIsInstance<ValuedProperty>()
            val classificationProperties = elementList.filterIsInstance<ClassificationProperty>()
            val groupedProperties = elementList.filterIsInstance<GroupedProperty>()
            val nestingRelationship =
                NestingRelationship(valuedProperties, classificationProperties, groupedProperties)
            datasetTreeDataStore.updateData {
                httpClient.getDataset(
                    mutableConnectionURL.value,
                    dataset.name,
                    nestingRelationship
                ) ?: error("Response was not a json element")
            }
            nestingRelationshipDataStore.updateData { nestingRelationship }
            onSaveFinished()
        }
    }

    fun fill()
    {
        val dataset = mutableSelectedDataset.value ?: error("Dataset not selected")
        val remainingProperties =
            dataset.properties - mutableNestingElementList.mapNotNull { it.elementProperty }
        mutableNestingElementList.addAll(remainingProperties.map { GroupedProperty(it) })
    }
}
