package ro.halex.av.viewmodel

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ro.halex.av.backend.DatasetDTO
import ro.halex.av.backend.NestingRelationship
import ro.halex.av.backend.getDatasets

class DataViewModel(application: Application) : AbstractViewModel(application)
{
    val mutableConnectionURL = mutableStateOf("")
    private val mutableDatasets = mutableStateOf<List<DatasetDTO>?>(null)
    val message = mutableStateOf<String?>(null)

    init
    {
        viewModelScope.launch {
            mutableConnectionURL.value = connectionDataStore.data.first() ?: ""
        }
    }

    fun getDatasets()
    {
        viewModelScope.launch {
            val connectionURL = mutableConnectionURL.value ?: return@launch
            val datasets = httpClient.getDatasets(connectionURL) ?: run {
                message.value = "Could not connect to server"
                return@launch
            }
            mutableDatasets.value = datasets
        }
    }

    private val mutableSelectedDataset = mutableStateOf<DatasetDTO?>(null)

    fun selectDataset(dataset: DatasetDTO)
    {
        mutableSelectedDataset.value = dataset
    }

    private val mutableNestingRelationship =
        mutableStateOf(NestingRelationship(emptyList(), emptyList(), emptyList()))

    val datasets = mutableDatasets as State<List<DatasetDTO>?>
    val selectedDataset = mutableSelectedDataset as State<DatasetDTO?>
    val nestingRelationship = mutableNestingRelationship as State<NestingRelationship>
}