package ro.halex.av.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ro.halex.av.MainApplication
import ro.halex.av.backend.getDatasets

class MainViewModel(application: Application) : AndroidViewModel(application)
{
    private val httpClient = getApplication<MainApplication>().httpClient

    fun getAndLogDatasets()
    {
        viewModelScope.launch {
            val datasets = httpClient.getDatasets()
            Log.d("MainViewModel", datasets.toString())
        }
    }
}