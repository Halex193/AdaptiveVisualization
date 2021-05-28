package ro.halex.av.ui.screen.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ro.halex.av.viewmodel.InnerNode

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BoxModule(node: InnerNode)
{
    Column(
        Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        node.children.forEach { (value, childNode) ->
            var expanded by remember(value, childNode) { mutableStateOf(false) }
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .border(
                        2.dp,
                        MaterialTheme.colors.onBackground,
                        shape = RoundedCornerShape(10.dp)
                    )
            ) {
                val transition = updateTransition(targetState = expanded, label = "Expand/Collapse")
                val bottomRound by transition.animateDp(label = "Corner radius") { if (it) 0.dp else 10.dp }

                val shape = RoundedCornerShape(
                    topStart = 10.dp,
                    topEnd = 10.dp,
                    bottomStart = bottomRound,
                    bottomEnd = bottomRound
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colors.surface, shape = shape)
                        .clip(shape)
                        .clickable { expanded = !expanded }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = CenterVertically
                )
                {
                    Text(
                        value,
                        Modifier.weight(1f),
                        color = MaterialTheme.colors.onSurface,
                        textAlign = TextAlign.Start
                    )
                    val rotation by transition.animateFloat(label = "Button rotation") { if (it) 0f else 90f }
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        "Expand/Collapse",
                        modifier = Modifier.rotate(rotation),
                        tint = MaterialTheme.colors.background
                    )
                }

                AnimatedVisibility(visible = expanded) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    )
                    {

                        DynamicUserInterface(childNode)
                    }
                }

            }

        }
    }
}