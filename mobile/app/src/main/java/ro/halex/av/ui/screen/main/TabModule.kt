package ro.halex.av.ui.screen.main

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ro.halex.av.viewmodel.InnerNode

@Composable
fun TabModule(node: InnerNode)
{
    Column(Modifier.padding(15.dp)) {
        val firstValue = node.children.entries.firstOrNull()?.key ?: error("Entries were empty")
        val (selectedValue, onValueSelect) = remember(node) { mutableStateOf(firstValue) }
        val childNode =
            node.children[selectedValue] ?: error("No value for selected key '$selectedValue'")

        val selectedTabIndex = node.children.keys.indexOf(selectedValue)
        if (selectedTabIndex == -1)
            error("Selected key '$selectedValue' is not from the list")
        ScrollableTabRow(selectedTabIndex = selectedTabIndex, edgePadding = 0.dp) {
            node.children.keys.forEach {
                Tab(selected = it == selectedValue, onClick = { onValueSelect(it) }) {
                    Box(modifier = Modifier.padding(15.dp)) {
                        Text(it)
                    }
                }
            }
        }
        Spacer(Modifier.height(5.dp))
        Crossfade(targetState = childNode) {
            DynamicUserInterface(it)
        }

    }
}