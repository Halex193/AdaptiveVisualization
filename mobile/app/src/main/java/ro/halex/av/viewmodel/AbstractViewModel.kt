package ro.halex.av.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ro.halex.av.MainApplication

abstract class AbstractViewModel(application: Application) : AndroidViewModel(application)
{
    private val mainApplication get() = getApplication<MainApplication>()
    protected val httpClient = mainApplication.httpClient
    protected val connectionDataStore = mainApplication.connectionDataStore
    protected val datasetInfoDataStore = mainApplication.datasetInfoDataStore
    protected val datasetTreeDataStore = mainApplication.datasetTreeDataStore
    protected val nestingRelationshipDataStore = mainApplication.nestingRelationshipDataStore
}