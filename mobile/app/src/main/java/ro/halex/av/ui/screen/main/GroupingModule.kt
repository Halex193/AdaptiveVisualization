package ro.halex.av.ui.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ro.halex.av.viewmodel.LeafNode

@Composable
fun GroupingModule(node: LeafNode)
{
    val data = node.data
    Column(
        Modifier.fillMaxWidth()
    ) {
        for (item in data)
        {
            Surface(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .background(MaterialTheme.colors.surface, shape = RoundedCornerShape(5.dp))
                    .padding(10.dp)
            ) {
                Column {
                    for ((key, value) in item)
                    {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                            Text("$key: ")
                            Spacer(Modifier.width(5.dp))
                            Text(value)
                        }
                    }
                    if (item.isEmpty())
                    {
                        Text(text = "No grouping properties selected")
                    }
                }
            }
        }
    }
}