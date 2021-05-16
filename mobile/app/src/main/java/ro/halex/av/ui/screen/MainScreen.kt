package ro.halex.av.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.map
import ro.halex.av.viewmodel.InnerNode
import ro.halex.av.viewmodel.LeafNode
import ro.halex.av.viewmodel.MainViewModel
import ro.halex.av.viewmodel.Node

@Composable
fun MainScreen(onDataPress: () -> Unit)
{
    val viewModel = viewModel<MainViewModel>()
    Column {
        run block@{
            Button(onClick = onDataPress) {
                Text("Go to data screen")
            }
            val tree = viewModel.tree.collectAsState(initial = null).value ?: return@block
            val color by viewModel.datasetInfo.map { datasetDTO ->
                datasetDTO?.color?.let {
                    Color(
                        it.toLong(16)
                    )
                } ?: Color.White
            }.collectAsState (initial = Color.White)

            Surface(Modifier.fillMaxSize(), color =color) {
                DynamicUserInterface(tree)
            }
        }
    }

}

@Composable
fun DynamicUserInterface(node: Node)
{
    when (node)
    {
        is InnerNode ->
        {
            when (node.module)
            {
                Module.MODULE1 -> Module1(node)
                Module.MODULE2 -> Module2(node)
            }
        }
        is LeafNode -> GroupingModule(node)
    }
}

@Composable
fun Module1(node: InnerNode)
{
    Column(Modifier.fillMaxWidth()) {
        node.children.forEach { (key, node) ->
            var expanded by remember(key) { mutableStateOf(true)}
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(key, color = Color.White)
                Spacer(Modifier.width(5.dp))
                Spacer(modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.LightGray))
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Filled.ArrowDropDown, "Expand/Collapse")
                }
            }
            if (expanded)
                DynamicUserInterface(node)
        }
    }
}

@Composable
fun Module2(node: InnerNode)
{
    Column(Modifier.padding(10.dp)) {
        node.children.toList().forEach { (key, node) ->
            Text(key)
            DynamicUserInterface(node)
        }
    }
}

@Composable
fun GroupingModule(node: LeafNode)
{
    val data = node.data
    Column(
        Modifier
            .fillMaxWidth()
            .padding(15.dp)) {
        for (item in data)
        {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(5.dp)
                    .background(Color.White, shape = RoundedCornerShape(5.dp))
                    .padding(10.dp)) {
                for ((key, value) in item)
                {
                    Row(Modifier.fillMaxWidth()) {
                        Text("$key: ")
                        Spacer(Modifier.width(5.dp))
                        Text(value)
                    }
                }
            }
        }
    }
}