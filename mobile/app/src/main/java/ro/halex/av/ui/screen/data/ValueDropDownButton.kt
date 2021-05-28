package ro.halex.av.ui.screen.data

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
internal fun ValueDropDownButton(
    modifier: Modifier = Modifier,
    values: List<String>?,
    onValueSelect: (String) -> Unit,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit
)
{
    Box(modifier) {
        Button(
            onClick = {
                if (values != null)
                {
                    onExpandChange(true)
                }
            },
            colors = backgroundButtonColors(),
            enabled = values != null
        ) {
            Text("Choose value")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { onExpandChange(false) }) {
            Column {
                values?.forEach {
                    DropdownMenuItem(onClick = { onValueSelect(it);onExpandChange(false) }) {
                        Text(it)
                    }
                }
            }
        }
    }
}