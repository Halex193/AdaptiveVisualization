package ro.halex.av.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ro.halex.av.MainApplication
import ro.halex.av.backend.DatasetDTO
import ro.halex.av.backend.getDatasets

abstract class AbstractViewModel(application: Application) : AndroidViewModel(application)
{
    protected val httpClient = (application as MainApplication).httpClient
}