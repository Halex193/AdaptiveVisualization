package ro.halex.av.ui.screen.modules

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ro.halex.av.ui.screen.DropdownButton
import ro.halex.av.viewmodel.InnerNode

@Composable
fun DropDownModule(node: InnerNode)
{
    val firstValue = node.children.entries.firstOrNull()?.key ?: error("Entries were empty")
    Column(androidx.compose.ui.Modifier.padding(15.dp)) {
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