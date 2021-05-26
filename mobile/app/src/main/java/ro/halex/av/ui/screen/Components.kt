package ro.halex.av.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DropdownButton(
    modifier: Modifier = Modifier,
    items: List<String>,
    selectedItem: String,
    onItemSelect: (String) -> Unit,
    handle: @Composable (onExpand: () -> Unit) -> Unit
)
{
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        handle(onExpand = { expanded = true })
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Column {
                items.forEach {
                    DropdownMenuItem(onClick = { onItemSelect(it);expanded = false }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .width(10.dp)
                                    .height(10.dp)
                                    .background(
                                        if (it == selectedItem) MaterialTheme.colors.onSurface
                                        else MaterialTheme.colors.surface,
                                        CircleShape
                                    )
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(it)
                        }
                    }
                }
            }

        }
    }
}