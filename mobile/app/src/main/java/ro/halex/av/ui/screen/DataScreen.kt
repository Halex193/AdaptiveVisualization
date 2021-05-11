package ro.halex.av.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import ro.halex.av.viewmodel.DataViewModel

@Composable
fun DataScreen(onBackPress: () -> Boolean)
{
    Surface(color = MaterialTheme.colors.background) {
        val viewModel = viewModel<DataViewModel>()
        val (connectionURL, onConnectionURLChange) = viewModel.mutableConnectionURL
        LazyColumn {
            item {
                TextField(value = connectionURL, onValueChange = onConnectionURLChange)
            }
            item {
                Button(onClick = viewModel::getDatasets) {
                    Text("Get datasets")
                }
            }
            val datasets = viewModel.datasets.value ?: return@LazyColumn
            item {
                var expanded by remember {mutableStateOf(false)}
                val selectedDataset by viewModel.selectedDataset
                Box(Modifier.clickable { expanded = true }.background(Color(selectedDataset?.color?.toLongOrNull(16) ?: 0xFFFFFFFF))) {
                    Text(text = selectedDataset?.name ?: "No dataset selected")
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        datasets.forEach { dataset ->
                            DropdownMenuItem(onClick = { viewModel.selectDataset(dataset);expanded = false }) {
                                Text(dataset.name)
                            }
                        }

                    }
                }

            }

        }
    }
}