package ro.halex.av.ui.screen.data

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ro.halex.av.backend.SortingOrder
import ro.halex.av.viewmodel.DataViewModel

@Composable
fun ClassificationPropertiesTab()
{
    val viewModel: DataViewModel = viewModel()
    val classificationProperties = viewModel.mutableClassificationProperties
    val (property, onPropertyChange) = remember { mutableStateOf<String?>(null) }
    val (sortingOrder, onSortingOrderChange) = remember { mutableStateOf(SortingOrder.ASCENDING) }

    LazyColumn(Modifier.fillMaxSize())
    {
        item {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

                val (expanded, onExpandChange) = remember { mutableStateOf(false) }

                SortingOrderPicker(sortingOrder, onSortingOrderChange)

                PropertyDropdownButton(
                    Modifier.padding(10.dp),
                    property = property,
                    onPropertySelect = { property ->
                        onPropertyChange(property)
                        onExpandChange(true)
                    })

                ModuleDropDownButton(
                    Modifier.padding(10.dp),
                    onModuleSelect = { module ->
                        property?.let { property ->
                            viewModel.addClassificationProperty(property, module, sortingOrder)
                            onPropertyChange(null)
                        }
                    },
                    expanded = expanded,
                    onExpandChange = onExpandChange,
                    enabled = property != null
                )
            }
        }

        item {
            Column(
                Modifier.padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                classificationProperties.forEachIndexed { index, classificationProperty ->
                    Surface(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                            .background(
                                MaterialTheme.colors.surface,
                                shape = RoundedCornerShape(5.dp)
                            )
                            .padding(10.dp)
                    )
                    {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val (currentProperty, sort, module) = classificationProperty
                            Row(
                                Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    currentProperty,
                                    Modifier
                                        .padding(10.dp)
                                        .weight(0.5f),
                                    textAlign = TextAlign.End,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(Icons.Filled.ArrowRightAlt, "Assigned to")
                                Text(
                                    module.label,
                                    Modifier
                                        .padding(10.dp)
                                        .weight(0.5f),
                                    textAlign = TextAlign.Start,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(onClick = {}, enabled = false) {
                                Icon(
                                    painterResource(checkNotNull(icons[sort])),
                                    "Sorting order",
                                    modifier = Modifier
                                        .padding(5.dp)
                                        .size(25.dp),
                                )
                            }

                            IconButton(onClick = {
                                viewModel.deleteClassificationProperty(classificationProperty)
                                onPropertyChange(null)
                            }) {
                                Icon(Icons.Filled.Delete, "Delete")
                            }
                        }
                    }
                    if (index != classificationProperties.size - 1)
                    {
                        IconButton(onClick = { viewModel.swapClassificationProperties(index) }) {
                            Icon(Icons.Filled.SwapVert, "Swap properties")
                        }
                    }
                }
            }
        }
    }
}