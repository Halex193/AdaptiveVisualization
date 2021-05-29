package ro.halex.av.ui.screen.data

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
fun GroupedPropertiesTab()
{
    val viewModel: DataViewModel = viewModel()
    val groupedProperties = viewModel.mutableGroupedProperties
    val (sortingOrder, onSortingOrderChange) = remember { mutableStateOf(SortingOrder.ASCENDING) }

    LazyColumn(Modifier.fillMaxSize())
    {
        item {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

                SortingOrderPicker(sortingOrder, onSortingOrderChange)

                PropertyDropdownButton(
                    Modifier.padding(10.dp),
                    property = null,
                    onPropertySelect = { property ->
                        viewModel.addGroupedProperty(property, sortingOrder)
                    })
                if (viewModel.availableProperties.isNotEmpty())
                {
                    Button(
                        onClick = { viewModel.fillGroupedProperties(sortingOrder) },
                        colors = backgroundButtonColors()
                    ) {
                        Text("Add all remaining properties")
                    }
                }
            }
        }

        item {
            Column(Modifier.padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                groupedProperties.forEachIndexed { index, groupedProperty ->
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
                            val (currentProperty, sort) = groupedProperty
                            Text(
                                currentProperty,
                                Modifier
                                    .padding(10.dp)
                                    .weight(1f),
                                textAlign = TextAlign.Center,
                                overflow = TextOverflow.Ellipsis
                            )
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
                                viewModel.deleteGroupedProperty(groupedProperty)
                            }) {
                                Icon(Icons.Filled.Delete, "Delete")
                            }
                        }
                    }
                    if (index != groupedProperties.size -1)
                    {
                        IconButton(onClick = { viewModel.swapGroupedProperties(index) }) {
                            Icon(Icons.Filled.SwapVert, "Swap properties")
                        }
                    }
                }
            }
        }
    }
}