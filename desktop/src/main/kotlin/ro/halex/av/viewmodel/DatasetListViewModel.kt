package ro.halex.av.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.ktor.client.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ro.halex.av.model.Dataset
import ro.halex.av.model.datasetFlow
import ro.halex.av.model.deleteDataset
import ro.halex.av.model.updateDataset
import ro.halex.av.view.Message
import ro.halex.av.view.chooseFile

class DatasetListViewModel(private val mainViewModel: MainViewModel, val client: HttpClient)
{
    fun datasetListFlow(): Flow<List<DatasetViewModel>>
    {
        val currentConfiguration = mainViewModel.configuration.value ?: return emptyFlow()
        return datasetFlow(client, currentConfiguration).map {
            it.map { dataset ->
                DatasetViewModel(
                    dataset,
                    currentConfiguration.username == dataset.username
                )
            }
        }
    }

    inner class DatasetViewModel(val dataset: Dataset, val createdByUser: Boolean)
    {
        val deleteLoading = mutableStateOf(false)
        val updateLoading = mutableStateOf(false)

        fun delete()
        {
            val currentConfiguration = mainViewModel.configuration.value ?: return
            var message by mainViewModel.message
            if (deleteLoading.value) return
            deleteLoading.value = true
            mainViewModel.coroutineScope.launch {
                when (deleteDataset(client, currentConfiguration, dataset.name))
                {
                    HttpStatusCode.OK -> message = Message("Dataset deleted", true)
                    HttpStatusCode.Unauthorized ->
                    {
                        message = Message("Invalid credentials", false)
                        mainViewModel.destroyConfiguration()
                    }
                    null -> message = Message("Connection to server failed", false)
                    else -> message = Message("Unknown error occurred", false)
                }
                deleteLoading.value = false
            }
        }

        fun update()
        {
            val currentConfiguration = mainViewModel.configuration.value ?: return
            var message by mainViewModel.message
            if (updateLoading.value) return
            updateLoading.value = true
            mainViewModel.coroutineScope.launch {

                val file = chooseFile() ?: run {
                    updateLoading.value = false
                    return@launch
                }
                when (updateDataset(
                    client,
                    currentConfiguration,
                    dataset.name,
                    file
                ))
                {
                    HttpStatusCode.OK -> message = Message("Dataset updated", true)

                    HttpStatusCode.Unauthorized ->
                    {
                        message = Message("Invalid credentials", false)
                        mainViewModel.destroyConfiguration()
                    }
                    HttpStatusCode.Conflict -> message = Message("Invalid properties", false)

                    HttpStatusCode.BadRequest -> message = Message("Invalid dataset", false)

                    null -> message = Message("Connection to server failed", false)

                    else -> message = Message("Unknown error occurred", false)
                }

                updateLoading.value = false
            }
        }

        override fun equals(other: Any?): Boolean
        {
            if (this === other) return true
            if (other !is DatasetViewModel) return false

            if (dataset != other.dataset) return false

            return true
        }

        override fun hashCode(): Int
        {
            return dataset.hashCode()
        }
    }
}
