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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ro.halex.av.viewmodel.DataViewModel

@Composable
internal fun PropertyDropdownButton(
    modifier: Modifier = Modifier,
    property: String?,
    onPropertySelect: (String) -> Unit
)
{
    val viewModel: DataViewModel = viewModel()
    var expanded by remember { mutableStateOf(false) }
    var properties by remember { mutableStateOf(emptyList<String>()) }
    Box(modifier) {
        Button(
            onClick = {
                properties = viewModel.getAvailableProperties()
                expanded = true
            }, colors = backgroundButtonColors()
        ) {
            Text(property ?: "Choose property")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Column {
                properties.forEach {
                    DropdownMenuItem(onClick = {
                        expanded = false
                        onPropertySelect(it)
                    }, enabled = it != property) {
                        Text(it)
                    }
                }
            }

        }
    }
}