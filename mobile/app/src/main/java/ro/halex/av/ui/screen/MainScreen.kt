package ro.halex.av.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ro.halex.av.ui.theme.AdaptiveVisualizationTheme
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
        AdaptiveVisualizationTheme(color) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .padding(5.dp)
                ) {
                    item {

                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = datasetInfo?.name ?: "")
                            IconButton(onClick = onDataPress) {
                                Icon(
                                    Icons.Filled.Settings,
                                    "Settings"
                                )
                            }
                            IconButton(onClick = onDataPress) {
                                Icon(
                                    Icons.Filled.Refresh,
                                    "Refresh"
                                )
                            }
                        }

                        val tree =
                            viewModel.tree.collectAsState(initial = null).value ?: return@item
                        DynamicUserInterface(tree)

                    }
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
                Module.DropDown -> DropDownModule(node, modifier)
                Module.MODULE3 -> Module3(node, modifier)
                Module.MODULE4 -> Module4(node, modifier)
            }
        }
        is LeafNode -> GroupingModule(node)
    }
}

@OptIn(ExperimentalAnimationApi::class)
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
                        "Expand/Collapse"
                    )
                }
            }
            AnimatedVisibility(visible = expanded) {
                DynamicUserInterface(node, Modifier.padding(10.dp))
            }
        }
    }
}

@Composable
fun DropDownModule(node: InnerNode, modifier: Modifier = Modifier)
{
    val firstValue = node.children.entries.firstOrNull()?.key ?: error("Entries were empty")
    Column(Modifier.padding(15.dp)) {
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
                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(MaterialTheme.colors.onPrimary))
                    Text(text = "$index/$size", modifier = Modifier.padding(start = 10.dp, end=5.dp), textAlign = TextAlign.Center)
                }
            }
        }
        Spacer(Modifier.height(5.dp))
        Crossfade(targetState = childNode) {
            DynamicUserInterface(it, Modifier.padding(10.dp))
        }
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
                    .border(
                        2.dp,
                        MaterialTheme.colors.onBackground,
                        shape = RoundedCornerShape(10.dp)
                    )
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colors.onBackground,
                                shape = RoundedCornerShape(10.dp, 0.dp, 10.dp, 0.dp)
                            )
                            .padding(5.dp)
                    )
                    {
                        Text(key, color = MaterialTheme.colors.background)
                    }

                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                )
                {
                    DynamicUserInterface(node, Modifier.padding(10.dp))
                }

            }

        }
    }
}

@Composable
fun Module4(node: InnerNode, modifier: Modifier = Modifier)
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
            DynamicUserInterface(it, Modifier.padding(10.dp))
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
}