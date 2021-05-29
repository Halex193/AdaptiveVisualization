package ro.halex.av.ui.screen.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ro.halex.av.ui.theme.AdaptiveVisualizationTheme
import ro.halex.av.viewmodel.LeafNode
import ro.halex.av.viewmodel.Node

@Composable
fun HelpCard(valuedProperties: LeafNode, helpTree: Node, modifier:Modifier = Modifier)
{
    Card(modifier) {
        run block@{
            AdaptiveVisualizationTheme(themeColor = Color.DarkGray) {
                Surface(
                    Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Column(Modifier.padding(10.dp)) {
                        if (valuedProperties.data.first().isNotEmpty())
                        {
                            GroupingModule(node = valuedProperties)
                        }
                        DynamicUserInterface(helpTree)
                    }
                }
            }
        }
    }
}