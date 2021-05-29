package ro.halex.av.ui.screen.main

import androidx.compose.runtime.Composable
import ro.halex.av.ui.screen.main.Module.*
import ro.halex.av.viewmodel.InnerNode
import ro.halex.av.viewmodel.LeafNode
import ro.halex.av.viewmodel.Node

@Composable
fun DynamicUserInterface(node: Node)
{
    when (node)
    {
        is InnerNode ->
        {
            when (node.module)
            {
                SEPARATORS -> SeparatorModule(node)
                DROPDOWN -> DropDownModule(node)
                BOXES -> BoxModule(node)
                TABS -> TabModule(node)
            }
        }
        is LeafNode -> GroupingModule(node)
    }
}