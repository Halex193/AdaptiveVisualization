package ro.halex.av.ui.screen.data

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import ro.halex.av.R
import ro.halex.av.backend.SortingOrder
import ro.halex.av.viewmodel.DataViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ValuedPropertiesTab()
{
    val viewModel: DataViewModel = viewModel()
    val valuedProperties = viewModel.mutableValuedProperties
    val coroutineScope = rememberCoroutineScope()
    val (property, onPropertyChange) = remember(viewModel.selectedDataset.value) { mutableStateOf<String?>(null) }
    val (sortingOrder, onSortingOrderChange) = remember { mutableStateOf(SortingOrder.ASCENDING) }
    var values by remember { mutableStateOf<List<String>?>(null) }

    LazyColumn(Modifier.fillMaxSize())
    {
        item {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

                val (expanded, onExpandChange) = remember { mutableStateOf(false) }

                SortingOrderPicker(sortingOrder, onSortingOrderChange = { sortingOrder ->
                    onSortingOrderChange(sortingOrder)
                    property?.let { property ->
                        values = null
                        coroutineScope.launch {
                            values = viewModel.getAvailableValues(property, sortingOrder)
                        }
                    }
                })

                PropertyDropdownButton(
                    Modifier.padding(10.dp),
                    property = property,
                    onPropertySelect = { property ->
                        values = null
                        onPropertyChange(property)
                        coroutineScope.launch {
                            values = viewModel.getAvailableValues(property, sortingOrder)
                            onExpandChange(true)
                        }
                    })

                ValueDropDownButton(
                    Modifier.padding(10.dp),
                    values = values,
                    onValueSelect = { value ->
                        property?.let { property ->
                            viewModel.addValuedProperty(property, value)
                            onPropertyChange(null)
                            values = null
                        }
                    },
                    expanded = expanded,
                    onExpandChange = onExpandChange
                )
            }
        }

        items(valuedProperties) { valuedProperty ->
            Surface(
                Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .background(MaterialTheme.colors.surface, shape = RoundedCornerShape(5.dp))
                    .padding(10.dp)
            )
            {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {

                        val (currentProperty, _, value) = valuedProperty
                        Text(
                            currentProperty,
                            Modifier
                                .padding(10.dp)
                                .weight(0.5f),
                            textAlign = TextAlign.End,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(painterResource(R.drawable.arrow_right_alt_24), "Assigned to")
                        Text(
                            value,
                            Modifier
                                .padding(10.dp)
                                .weight(0.5f),
                            textAlign = TextAlign.Start,
                            overflow = TextOverflow.Ellipsis
                        )
                    }


                    IconButton(onClick = {
                        viewModel.deleteValuedProperty(valuedProperty)
                        onPropertyChange(null)
                        values = null
                    }) {
                        Icon(Icons.Filled.Delete, "Delete")
                    }
                }
            }
        }
    }
}

