package ro.halex.av.ui.screen.data

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.serialization.json.JsonElement
import ro.halex.av.ui.screen.main.HelpCard
import ro.halex.av.viewmodel.DataViewModel
import ro.halex.av.viewmodel.LeafNode

@Composable
fun PreviewTab(onBackPress: () -> Unit)
{
    val viewModel: DataViewModel = viewModel()
    val nestingRelationship = viewModel.nestingRelationship
    if (!viewModel.nestingRelationshipEmpty)
    {
        var data by remember(nestingRelationship) { mutableStateOf<JsonElement?>(null) }
        LaunchedEffect(nestingRelationship)
        {
            data = viewModel.getData()
        }
        LazyColumn(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            item {
                Button(
                    modifier = Modifier.padding(10.dp),
                    onClick = {
                        data?.let {
                            viewModel.save(
                                it,
                                onSaveFinished = onBackPress
                            )
                        }
                    },
                    colors = backgroundButtonColors(),
                    enabled = data != null
                ) {
                    Text(data?.let { "Save" } ?: "Data downloading")
                    if (data == null)
                        CircularProgressIndicator(Modifier.size(20.dp))
                }

                val valuedProperties = viewModel.valuedPropertiesLeafNode
                val helpTree = viewModel.helpTree

                HelpCard(valuedProperties, helpTree, Modifier.padding(10.dp))
            }
        }
    } else
    {
        Text(
            "Choose properties to show preview",
            style = MaterialTheme.typography.h6,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(10.dp)
        )
    }
}