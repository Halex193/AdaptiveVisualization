package ro.halex.av.viewmodel

import android.app.Application
import android.util.Log
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.*
import ro.halex.av.backend.ClassificationProperty
import ro.halex.av.backend.NestingRelationship
import ro.halex.av.ui.screen.Module

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
    val valuedProperties = nestingRelationship.map { it?.valuedProperties }
    val tree = nestingRelationship.combine(datasetTree) { nestingRelationShip, datasetTree ->
        nestingRelationShip ?: return@combine null

        fun convertTree(
            tree: JsonElement,
            classificationProperties: List<ClassificationProperty>
        ): Node
        {
            if (tree is JsonArray)
            {
                return LeafNode(tree.map { jsonElement ->
                    jsonElement.jsonObject.mapValues { it.value.jsonPrimitive.content }
                })
            }
            val classificationProperty = classificationProperties.first()
            val children = tree.jsonObject.mapValues {
                convertTree(
                    it.value,
                    classificationProperties.slice(1 until classificationProperties.size)
                )
            }
            return InnerNode(classificationProperty.module, classificationProperty.property, children)
        }
        convertTree(datasetTree, nestingRelationShip.classificationProperties)
    }.onEach {
        Log.i("MainViewModel", "Displaying: $it")
    }
}