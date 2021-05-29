package ro.halex.av.ui.screen.data

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
    val properties  = viewModel.availableProperties
    Box(modifier) {
        Button(
            onClick = {
                expanded = true
            },
            colors = backgroundButtonColors(),
            enabled = properties.isNotEmpty()
        ) {
            val unselectedText =
                if (properties.isEmpty()) "No properties left" else "Choose property"
            Text(property ?: unselectedText)
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