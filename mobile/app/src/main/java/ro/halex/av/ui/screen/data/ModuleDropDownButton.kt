package ro.halex.av.ui.screen.data

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ro.halex.av.ui.screen.main.Module

@Composable
internal fun ModuleDropDownButton(
    modifier: Modifier = Modifier,
    onModuleSelect: (Module) -> Unit,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    enabled: Boolean
)
{
    Box(modifier) {
        Button(
            onClick = { onExpandChange(true) },
            colors = backgroundButtonColors(),
            enabled = enabled
        ) {
            Text("Choose module")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { onExpandChange(false) }) {
            Column {
                Module.values().forEach {
                    DropdownMenuItem(onClick = { onModuleSelect(it);onExpandChange(false) }) {
                        Text(it.label)
                    }
                }
            }
        }
    }
}