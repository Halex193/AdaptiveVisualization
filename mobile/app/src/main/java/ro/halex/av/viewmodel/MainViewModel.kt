package ro.halex.av.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import ro.halex.av.backend.ClassificationProperty
import ro.halex.av.backend.NestingRelationship
import ro.halex.av.backend.getDataset
import ro.halex.av.ui.screen.main.Module

sealed class Node

data class InnerNode(
    val module: Module,
    val property: String,
    val children: Map<String, Node>
) : Node()

data class LeafNode(val data: List<Map<String, String>>) : Node()

class MainViewModel(application: Application) : AbstractViewModel(application)
{
    val datasetInfo = datasetInfoDataStore.data
    private val nestingRelationship = nestingRelationshipDataStore.data
    private val datasetTree = datasetTreeDataStore.data

    val nestingRelationshipEmpty: Flow<Boolean> =
        nestingRelationship.take(1).map { it == null }

    val tree: Flow<Node?> =
        nestingRelationship.combine(datasetTree) { nestingRelationShip, datasetTree ->
            nestingRelationShip ?: return@combine null

            fun convertTree(
                tree: JsonElement,
                classificationProperties: List<ClassificationProperty>
            ): Node
            {
                if (tree is JsonArray)
                {
                    return LeafNode(tree.map { jsonElement ->
                        jsonElement.jsonObject.mapValuesTo(LinkedHashMap()) { it.value.jsonPrimitive.content }
                    })
                }
                val classificationProperty = classificationProperties.first()
                val children = tree.jsonObject.mapValuesTo(LinkedHashMap()) {
                    convertTree(
                        it.value,
                        classificationProperties.slice(1 until classificationProperties.size)
                    )
                }
                return InnerNode(
                    classificationProperty.module,
                    classificationProperty.property,
                    children
                )
            }
            convertTree(datasetTree, nestingRelationShip.classificationProperties)
        }

    val helpTree: Flow<Node?> = nestingRelationship.map { relationship ->
        relationship ?: return@map null
        helpNode(relationship)
    }

    val valuedProperties: Flow<LeafNode?> =
        nestingRelationship.map { relationship ->
            relationship?.valuedProperties?.associate { it.property to it.value }
                ?.let { LeafNode(listOf(it)) }
        }

    fun refresh()
    {
        viewModelScope.launch {
            val datasetName = datasetInfo.first()?.name ?: return@launch
            val connectionURL = connectionDataStore.data.first()
            val nestingRelationship = nestingRelationship.first() ?: return@launch

            httpClient.getDataset(
                connectionURL,
                datasetName,
                nestingRelationship
            )
                ?.let { newData ->
                    datasetTreeDataStore.updateData { oldData ->
                        if (newData == oldData)
                            showToast("Data was not changed")
                        else
                            showToast("Data was updated")
                        newData
                    }
                }
                ?: run {
                    showToast("Server connection error")
                }

        }
    }
}