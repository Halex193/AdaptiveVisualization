package ro.halex.av.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import io.ktor.client.*
import kotlinx.coroutines.CoroutineScope
import ro.halex.av.model.Configuration
import ro.halex.av.model.createHttpClient
import ro.halex.av.view.Message

class MainViewModel(val coroutineScope: CoroutineScope)
{
    private val _configuration: MutableState<Configuration?> = mutableStateOf(null)
    val configuration: State<Configuration?> = _configuration
    val message: MutableState<Message?> = mutableStateOf(null)
    val loginViewModel = LoginViewModel(this)
    val mainScreenViewModels: MutableState<Pair<AddViewModel, DatasetListViewModel>?> = mutableStateOf(null)
    private var lastClient: HttpClient? = null

    fun createConfiguration(currentConfiguration: Configuration)
    {
        lastClient = createHttpClient(currentConfiguration).also {
            mainScreenViewModels.value = AddViewModel(this, it) to DatasetListViewModel(this, it)
        }
        _configuration.value = currentConfiguration
    }

    fun destroyConfiguration()
    {
        lastClient?.close()
        _configuration.value = null
    }
}


