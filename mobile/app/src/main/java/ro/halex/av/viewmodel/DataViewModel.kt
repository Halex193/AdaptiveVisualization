package ro.halex.av.viewmodel

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ro.halex.av.backend.DatasetDTO
import ro.halex.av.backend.getDatasets

class DataViewModel(application: Application) : AbstractViewModel(application)
{
    private val datasets = mutableStateOf<List<DatasetDTO>?>(null)

    fun getDatasets(): State<List<DatasetDTO>?>
    {
        viewModelScope.launch {
            datasets.value = httpClient.getDatasets()
        }
        return datasets
    }
}