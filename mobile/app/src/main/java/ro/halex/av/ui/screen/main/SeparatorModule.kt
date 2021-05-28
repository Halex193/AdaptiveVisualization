package ro.halex.av.ui.screen.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import ro.halex.av.viewmodel.InnerNode

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SeparatorModule(node: InnerNode)
{
    Column(
        Modifier
            .fillMaxWidth()
            .padding(15.dp)
    ) {
        node.children.forEach { (value, childNode) ->
            var expanded by remember(value, childNode) { mutableStateOf(true) }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(value)
                Spacer(Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .weight(1F)
                        .height(1.dp)
                        .background(MaterialTheme.colors.onBackground)
                )
                IconButton(onClick = { expanded = !expanded }) {
                    val rotation by animateFloatAsState(targetValue = if (expanded) 0f else 90f)
                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        "Expand/Collapse",
                        modifier = Modifier.rotate(rotation)
                    )
                }
            }
            AnimatedVisibility(visible = expanded) {
                DynamicUserInterface(childNode)
            }
        }
    }
}