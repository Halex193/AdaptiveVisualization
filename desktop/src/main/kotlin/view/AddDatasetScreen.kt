package view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import model.FileDetails
import model.datasetColors
import viewmodel.AddViewModel

@OptIn(ExperimentalUnsignedTypes::class)
@Composable
fun AddDataset(addViewModel: AddViewModel)
{
    Surface(Modifier.width(400.dp).clip(MaterialTheme.shapes.large)) {
        var name: String by addViewModel.name
        var selectedColor: Long by addViewModel.selectedColor
        val file: FileDetails? by addViewModel.file

        Column(
            Modifier.fillMaxWidth().padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Create dataset", style = MaterialTheme.typography.h6)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.width(5.dp))
                TextField(name, onValueChange = { name = it })
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                val colorObjects = remember { datasetColors.associateWith { Color(it) } }
                datasetColors.forEach { color ->
                    Box(
                        Modifier.padding(10.dp).size(40.dp).background(
                            colorObjects[color]
                                ?: error("Selected color is not in permitted values"),
                            shape = CircleShape
                        ).clip(CircleShape).clickable { selectedColor = color },
                        contentAlignment = Alignment.Center
                    )
                    {
                        if (selectedColor == color)
                            Box(
                                Modifier.size(30.dp).background(
                                    MaterialTheme.colors.surface, shape = CircleShape
                                ),
                            )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                val buttonColors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colors.surface,
                    backgroundColor = MaterialTheme.colors.background
                )
                TextButton(colors = buttonColors, onClick = addViewModel::fileSelected)
                {
                    if (file == null)
                        Text("Choose file")
                    else
                        Text("Change file")
                }
                Spacer(Modifier.width(10.dp))
                val adding by addViewModel.adding
                TextButton(modifier = Modifier.width(70.dp), colors = buttonColors, onClick = addViewModel::add, enabled = !adding)
                {
                    if (!adding)
                        Text("Create")
                    else
                        CircularProgressIndicator(Modifier.size(20.dp), color= MaterialTheme.colors.onBackground)
                }
            }
            file?.let { currentFile ->
                Spacer(Modifier.height(10.dp))
                Text("${currentFile.fileObject.name} - ${currentFile.items} items")
                Spacer(Modifier.height(10.dp))
                Spacer(Modifier.height(4.dp))
                ShowProperties(currentFile.properties)
            }

        }

    }

}
