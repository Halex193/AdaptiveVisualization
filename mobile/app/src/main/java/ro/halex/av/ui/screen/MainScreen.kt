package ro.halex.av.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Settings
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

            val datasetInfo = viewModel.datasetInfo.collectAsState(initial = null).value
            val color = datasetInfo
                ?.let { Color(it.color.toLong(16)) }
                ?: Color.Black
            Surface(Modifier.fillMaxSize(), color = color) {
                LazyColumn(Modifier.fillMaxSize().padding(5.dp)) {
                    item {

                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = datasetInfo?.name ?: "", color = MaterialTheme.colors.onSurface)
                            IconButton(onClick = onDataPress) {
                                Icon(
                                    Icons.Filled.Settings,
                                    "Settings",
                                    tint = MaterialTheme.colors.onSurface
                                )
                            }
                        }

                        val tree = viewModel.tree.collectAsState(initial = null).value ?: return@item
                        DynamicUserInterface(tree)

                }
            }
        }
    }

}

@Composable
fun DynamicUserInterface(node: Node, modifier: Modifier = Modifier)
{
    when (node)
    {
        is InnerNode ->
        {
            when (node.module)
            {
                Module.MODULE1 -> Module1(node, modifier)
                Module.MODULE2 -> Module2(node, modifier)
                Module.MODULE3 -> Module3(node, modifier)
            }
        }
        is LeafNode -> GroupingModule(node)
    }
}

@Composable
fun Module1(node: InnerNode, modifier: Modifier = Modifier)
{
    Column(
        Modifier
            .fillMaxWidth()
            .padding(15.dp)
    ) {
        node.children.forEach { (key, node) ->
            var expanded by remember(key) { mutableStateOf(true) }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(key, color = Color.White)
                Spacer(Modifier.width(5.dp))
                Spacer(
                    modifier = Modifier
                        .weight(1F)
                        .height(1.dp)
                        .background(Color.LightGray)
                )
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Filled.ArrowDropDown else Icons.Filled.KeyboardArrowLeft,
                        "Expand/Collapse",
                        tint = MaterialTheme.colors.onSurface
                    )
                }
            }
            if (expanded)
                DynamicUserInterface(node, Modifier.padding(10.dp))
        }
    }
}

@Composable
fun Module2(node: InnerNode, modifier: Modifier = Modifier)
{
    Column(Modifier.padding(15.dp)) {
        var selectedKey by remember(node) { mutableStateOf(node.children.entries.first().key) }
        val childNode =
            node.children[selectedKey] ?: error("No value for selected key '$selectedKey'")
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(text = node.property, color = MaterialTheme.colors.onSurface)
            Spacer(Modifier.width(5.dp))
            var expanded by remember { mutableStateOf(false) }
            Box {
                Button(onClick = { expanded = true }) {
                    Text(selectedKey)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    node.children.keys.forEach {
                        DropdownMenuItem(onClick = { selectedKey = it;expanded = false }) {
                            Text(it)
                        }
                    }
                }
            }
            Spacer(Modifier.width(5.dp))
            Text(text = node.children.size.toString(), color = MaterialTheme.colors.onSurface)
        }
        Spacer(Modifier.height(5.dp))
        DynamicUserInterface(childNode, Modifier.padding(10.dp))
    }
}

@Composable
fun Module3(node: InnerNode, modifier: Modifier = Modifier)
{
    Column(
        Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        node.children.forEach { (key, node) ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .border(2.dp, MaterialTheme.colors.onSurface, shape = RoundedCornerShape(10.dp))
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colors.onSurface,
                                shape = RoundedCornerShape(10.dp, 0.dp, 10.dp, 0.dp)
                            )
                            .padding(5.dp)
                    )
                    {
                        Text(key, color = Color.Black)
                    }

                }
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp))
                {
                    DynamicUserInterface(node, Modifier.padding(10.dp))
                }

            }

        }
    }
}

@Composable
fun GroupingModule(node: LeafNode, modifier: Modifier = Modifier)
{
    val data = node.data
    Column(
        Modifier.fillMaxWidth()
    ) {
        for (item in data)
        {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .background(Color.White, shape = RoundedCornerShape(5.dp))
                    .padding(10.dp)
            ) {
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