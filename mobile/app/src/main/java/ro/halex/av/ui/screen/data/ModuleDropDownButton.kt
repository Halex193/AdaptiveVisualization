package ro.halex.av.ui.screen.data

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
        CustomButton(
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