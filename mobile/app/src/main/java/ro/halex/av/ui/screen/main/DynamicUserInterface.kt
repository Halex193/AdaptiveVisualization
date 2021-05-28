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
                Separators -> SeparatorModule(node)
                DropDown -> DropDownModule(node)
                Boxes -> BoxModule(node)
                Tabs -> TabModule(node)
            }
        }
        is LeafNode -> GroupingModule(node)
    }
}