package ro.halex.av.ui.screen.data

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ro.halex.av.viewmodel.DataViewModel

@Composable
fun DatasetSelection()
{
    val viewModel: DataViewModel = viewModel()
    val datasets = viewModel.datasets.value ?: return

    var expanded by remember { mutableStateOf(false) }
    val selectedDataset by viewModel.selectedDataset
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Property configuration",
            Modifier
                .weight(1f)
                .padding(10.dp)
                .padding(start = 10.dp),
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.h6
        )

        Box()
        {
            Button(
                onClick = { expanded = true },
                modifier = Modifier.padding(horizontal = 10.dp),
                enabled = !viewModel.dataDownloading
            ) {
                Text(
                    text = selectedDataset?.name ?: "Select dataset",
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }) {
                datasets.forEach { dataset ->
                    DropdownMenuItem(onClick = {
                        viewModel.selectDataset(dataset)
                        expanded = false
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .width(10.dp)
                                    .height(10.dp)
                                    .background(
                                        Color(dataset.color.toLong(16)),
                                        CircleShape
                                    )
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(dataset.name)
                        }
                    }
                }

            }
        }

    }
}