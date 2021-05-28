package ro.halex.av.ui.screen.main

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ro.halex.av.viewmodel.InnerNode

@Composable
fun DropDownModule(node: InnerNode)
{
    val firstValue = node.children.entries.firstOrNull()?.key ?: error("Entries were empty")
    Column(Modifier.padding(15.dp)) {
        val (selectedValue, onValueSelect) = remember(node) { mutableStateOf(firstValue) }
        val childNode =
            node.children[selectedValue] ?: error("No value for selected key '$selectedValue'")
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            DropdownButton(
                modifier = Modifier.fillMaxWidth(),
                items = node.children.keys.toList(),
                selectedItem = selectedValue,
                onItemSelect = onValueSelect
            ) { onExpand ->
                val index = node.children.keys.indexOf(selectedValue) + 1
                val size = node.children.size
                Button(modifier = Modifier.fillMaxWidth(), onClick = onExpand) {
                    Text(selectedValue, modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(20.dp)
                            .background(MaterialTheme.colors.onPrimary)
                    )
                    Text(
                        text = "$index/$size",
                        modifier = Modifier.padding(start = 10.dp, end = 5.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        Spacer(Modifier.height(5.dp))
        Crossfade(targetState = childNode) {
            DynamicUserInterface(it)
        }
    }
}

@Composable
private fun DropdownButton(
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
                                        if (it == selectedItem) MaterialTheme.colors.background
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