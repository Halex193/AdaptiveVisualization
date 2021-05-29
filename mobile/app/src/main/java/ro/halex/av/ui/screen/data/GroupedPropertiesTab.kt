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

                Button(
                    onClick = { viewModel.fillGroupedProperties(sortingOrder) },
                    colors = backgroundButtonColors()
                ) {
                    Text("Add all remaining properties")
                }
            }
        }

        items(groupedProperties) { groupedProperty ->
            Surface(
                Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .background(MaterialTheme.colors.surface, shape = RoundedCornerShape(5.dp))
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
                    val (canPushUp, canPushDown) = viewModel.canPushUpDown(groupedProperty)
                    Column {
                        IconButton(
                            onClick = { viewModel.pushUp(groupedProperty) },
                            enabled = canPushUp
                        ) {
                            Icon(Icons.Filled.ArrowUpward, "Push up")
                        }
                        IconButton(
                            onClick = { viewModel.pushDown(groupedProperty) },
                            enabled = canPushDown
                        ) {
                            Icon(Icons.Filled.ArrowDownward, "Push down")
                        }
                    }
                    Column {
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
            }
        }
    }
}