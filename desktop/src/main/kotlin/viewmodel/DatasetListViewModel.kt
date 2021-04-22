package viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import model.Dataset
import model.datasetFlow
import view.Message
import view.chooseFile

class DatasetListViewModel(private val mainViewModel: MainViewModel)
{
    fun datasetListFlow(): Flow<List<DatasetViewModel>>
    {
        val currentConfiguration = mainViewModel.configuration.value ?: return emptyFlow()
        return datasetFlow(mainViewModel.client, currentConfiguration).map {
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

        fun deleteDataset()
        {
            val currentConfiguration = mainViewModel.configuration.value ?: return
            var message by mainViewModel.message
            if (deleteLoading.value) return
            deleteLoading.value = true
            mainViewModel.coroutineScope.launch {
                when (model.deleteDataset(mainViewModel.client, currentConfiguration, dataset.name))
                {
                    HttpStatusCode.OK -> message = Message("Dataset deleted", true)
                    HttpStatusCode.Unauthorized ->
                    {
                        message =
                            Message(
                                "Invalid credentials",
                                false
                            )

                        mainViewModel.configuration.value = null
                    }
                    null -> message = Message("Connection to server failed", false)
                    else -> message = Message("Unknown error occurred", false)
                }
                deleteLoading.value = false
            }
        }

        fun updateDataset()
        {
            val currentConfiguration = mainViewModel.configuration.value ?: return
            var message by mainViewModel.message
            if (updateLoading.value) return
            updateLoading.value = true
            mainViewModel.coroutineScope.launch {

                val file = chooseFile() ?: return@launch
                when (model.updateDataset(mainViewModel.client, currentConfiguration, dataset.name, file))
                {
                    HttpStatusCode.OK -> message =
                        Message(
                            "Dataset updated",
                            true
                        )

                    HttpStatusCode.Unauthorized ->
                    {
                        message =
                            Message(
                                "Invalid credentials",
                                false
                            )
                        mainViewModel.configuration.value = null
                    }
                    HttpStatusCode.Conflict -> message =
                        Message(
                            "Invalid properties",
                            false
                        )

                    HttpStatusCode.BadRequest -> message =
                        Message(
                            "Invalid dataset",
                            false
                        )

                    null -> message =
                        Message(
                            "Connection to server failed",
                            false
                        )

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
