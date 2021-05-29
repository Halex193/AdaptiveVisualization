package ro.halex.av.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import ro.halex.av.MainApplication
import ro.halex.av.backend.ClassificationProperty
import ro.halex.av.backend.NestingRelationship

abstract class AbstractViewModel(application: Application) : AndroidViewModel(application)
{
    private val mainApplication get() = getApplication<MainApplication>()
    protected val httpClient = mainApplication.httpClient
    protected val connectionDataStore = mainApplication.connectionDataStore
    protected val datasetInfoDataStore = mainApplication.datasetInfoDataStore
    protected val datasetTreeDataStore = mainApplication.datasetTreeDataStore
    protected val nestingRelationshipDataStore = mainApplication.nestingRelationshipDataStore

    protected fun showToast(message: String)
    {
        Toast.makeText(mainApplication, message, Toast.LENGTH_SHORT)
            .show()
    }

    protected fun helpNode(relationship: NestingRelationship): Node
    {
        fun convertRelationship(classificationProperties: List<ClassificationProperty>): Node
        {
            val classificationProperty = classificationProperties.firstOrNull() ?: run {
                val dummyItem = relationship.groupedProperties.associate { it.property to "•" }
                return LeafNode(listOf(dummyItem, dummyItem))
            }

            val subTree =
                convertRelationship(classificationProperties.slice(1 until classificationProperties.size))

            val property = classificationProperty.property
            return InnerNode(
                classificationProperty.module,
                property,
                mapOf("$property 1" to subTree, "$property 2" to subTree)
            )
        }
        return convertRelationship(relationship.classificationProperties)
    }
}