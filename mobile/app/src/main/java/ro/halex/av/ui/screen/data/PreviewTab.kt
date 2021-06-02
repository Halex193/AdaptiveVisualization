package ro.halex.av.ui.screen.data

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ro.halex.av.ui.screen.main.HelpCard
import ro.halex.av.viewmodel.DataViewModel

@Composable
fun PreviewTab(onBackPress: () -> Unit)
{
    val viewModel: DataViewModel = viewModel()
    if (!viewModel.nestingRelationshipEmpty)
    {
        val dataDownloading = viewModel.dataDownloading
        LazyColumn(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            item {
                Row {
                    Button(
                        modifier = Modifier.padding(10.dp),
                        onClick = {
                            if (!dataDownloading)
                                viewModel.save(onSaveFinished = onBackPress)
                        },
                        colors = backgroundButtonColors(),
                        enabled = !dataDownloading
                    ) {
                        Text(if (dataDownloading) "Downloading" else "Download data")
                        if (dataDownloading)
                        {
                            Spacer(Modifier.width(5.dp))
                            CircularProgressIndicator(Modifier.size(20.dp))
                        }
                    }
                }

                val valuedProperties = viewModel.valuedPropertiesLeafNode
                val helpTree = viewModel.helpTree

                HelpCard(valuedProperties, helpTree, Modifier.padding(10.dp))
            }
        }
    } else
    {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center)
        {
            Text(
                "Choose properties to show preview",
                style = MaterialTheme.typography.h6,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}