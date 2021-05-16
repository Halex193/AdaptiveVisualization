package ro.halex.av.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ro.halex.av.backend.*
import ro.halex.av.viewmodel.DataViewModel

@Composable
fun DataScreen(onBackPress: () -> Unit)
{
    Surface(color = MaterialTheme.colors.background) {
        val viewModel = viewModel<DataViewModel>()
        val (connectionURL, onConnectionURLChange) = viewModel.mutableConnectionURL
        LazyColumn {
            item {
                Row {
                    IconButton(onClick = onBackPress) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                    TextField(value = connectionURL, onValueChange = onConnectionURLChange)
                }

            }
            item {
                Button(onClick = viewModel::getDatasets) {
                    Text("Get datasets")
                }
            }
            val datasets = viewModel.datasets.value ?: return@LazyColumn
            item {
                var expanded by remember { mutableStateOf(false) }
                val selectedDataset by viewModel.selectedDataset
                Box(
                    Modifier
                        .clickable { expanded = true }
                        .background(
                            Color(
                                selectedDataset?.color?.toLongOrNull(16) ?: 0xFFFFFFFF
                            )
                        )) {
                    Text(text = selectedDataset?.name ?: "No dataset selected")
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        datasets.forEach { dataset ->
                            DropdownMenuItem(onClick = {
                                viewModel.selectDataset(dataset);expanded = false
                            }) {
                                Text(dataset.name)
                            }
                        }

                    }
                }
            }

            val selectedDataset = viewModel.selectedDataset.value ?: return@LazyColumn
            val nestingElementList = viewModel.mutableNestingElementList

            itemsIndexed(nestingElementList) { index, nestingElement ->

                Column {
                    run block@{
                        Row {
                            Box {
                                var expanded by remember { mutableStateOf(false) }
                                val availableProperties = remember {
                                    viewModel.getAvailableProperties(
                                        nestingElement
                                    )
                                }
                                Button(onClick = { expanded = true }) {
                                    Text(text = nestingElement.elementProperty ?: "Choose property")
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }) {
                                    availableProperties.forEach {
                                        DropdownMenuItem(onClick = {
                                            expanded = false
                                            nestingElementList[index] = SimpleProperty(it)
                                        }) {
                                            Text(it)
                                        }
                                    }
                                }
                            }
                            IconButton(onClick = { viewModel.removeNestingElement(index) }) {
                                Icon(Icons.Default.Delete, "Delete")
                            }

                            IconButton(onClick = {
                                nestingElementList[index] =
                                    nestingElementList[index].copyWithSort(SortingOrder.ASCENDING)
                            }) {
                                Icon(Icons.Default.KeyboardArrowUp, "Ascending")
                            }
                            IconButton(onClick = {
                                nestingElementList[index] =
                                    nestingElementList[index].copyWithSort(SortingOrder.DESCENDING)
                            }) {
                                Icon(Icons.Default.KeyboardArrowDown, "Descending")
                            }
                            IconButton(onClick = {
                                nestingElementList[index] =
                                    nestingElementList[index].copyWithSort(SortingOrder.INCREASING)
                            }) {
                                Icon(Icons.Default.PlayArrow, "Increasing")
                            }
                            IconButton(onClick = {
                                nestingElementList[index] =
                                    nestingElementList[index].copyWithSort(SortingOrder.DECREASING)
                            }) {
                                Icon(Icons.Default.ArrowDropDown, "Decreasing")
                            }
                        }
                        val property = nestingElement.elementProperty ?: return@block
                        val sort = nestingElement.elementSort
                        Row {
                            run block2@{
                                Box {
                                    var expanded by remember { mutableStateOf(false) }
                                    var availableValues by
                                    remember { mutableStateOf<List<String>?>(null) }
                                    LaunchedEffect(sort) {
                                        availableValues = viewModel.getAvailableValues(index)
                                    }
                                    Button(onClick = { expanded = true }) {
                                        Text(
                                            text = when (nestingElement)
                                            {
                                                is ClassificationProperty -> "Classification"
                                                is GroupedProperty -> "Grouped"
                                                is ValuedProperty -> nestingElement.value
                                                is SimpleProperty -> "Choose type"
                                            }
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }) {
                                        DropdownMenuItem(onClick = {
                                            expanded = false
                                            nestingElementList[index] = GroupedProperty(
                                                nestingElement.elementProperty
                                                    ?: error("Property was not selected"),
                                                nestingElement.elementSort
                                            )
                                        }) {
                                            Text("Grouped")
                                        }
                                        DropdownMenuItem(onClick = {
                                            expanded = false
                                            nestingElementList[index] = ClassificationProperty(
                                                property,
                                                nestingElement.elementSort
                                            )
                                        }) {
                                            Text("Classification")
                                        }
                                        Spacer(
                                            Modifier
                                                .fillMaxWidth()
                                                .height(1.dp)
                                                .background(Color.LightGray)
                                        )
                                        availableValues?.forEach {
                                            DropdownMenuItem(onClick = {
                                                expanded = false
                                                nestingElementList[index] = ValuedProperty(
                                                    property,
                                                    nestingElement.elementSort,
                                                    it
                                                )
                                            }) {
                                                Text(it)
                                            }
                                        } ?: run {
                                            CircularProgressIndicator()
                                        }
                                    }
                                }
                                val classificationElement =
                                    nestingElement as? ClassificationProperty ?: return@block2
                                Box {
                                    var expanded by remember { mutableStateOf(false) }
                                    Button(onClick = { expanded = true }) {
                                        Text(classificationElement.module.toString())
                                    }
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }) {
                                        Module.values().forEach { module ->
                                            DropdownMenuItem(onClick = {
                                                nestingElementList[index] =
                                                    classificationElement.copy(module = module)
                                                expanded = false
                                            }) {
                                                Text(module.toString())
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {

                if (nestingElementList.size != selectedDataset.properties.size)
                {
                    Row {
                        FloatingActionButton(onClick = { viewModel.addNestingElement() }) {
                            Icon(Icons.Default.Add, "Add")
                        }
                        Button(onClick = { viewModel.fill() }) {
                            Text("Fill")
                        }
                    }
                } else
                {
                    var loading by remember { mutableStateOf(false) }
                    Row {
                        Button(onClick = {
                            viewModel.save(onSaveFinished = onBackPress)
                            loading = true
                        }) {
                            Text("Save")
                        }
                        if (loading)
                            CircularProgressIndicator()
                    }
                }

            }
        }
    }
}