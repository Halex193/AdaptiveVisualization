package ro.halex.av.ui.screen

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ro.halex.av.ui.screen.main.*
import ro.halex.av.ui.theme.AdaptiveVisualizationTheme
import ro.halex.av.viewmodel.MainViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen(onDataPress: () -> Unit)
{
    val viewModel = viewModel<MainViewModel>()
    Column {
        val datasetInfo = viewModel.datasetInfo.collectAsState(initial = null).value
        val color = datasetInfo
            ?.let { Color(it.color.toLong(16)) }
            ?: Color.Black
        AdaptiveVisualizationTheme(themeColor = color) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .padding(5.dp)
                ) {
                    item {
                        var showHelp by remember { mutableStateOf(false) }
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = datasetInfo?.name ?: "",
                                style = MaterialTheme.typography.h5,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(15.dp)
                            )
                            IconButton(onClick = onDataPress) {
                                Icon(
                                    Icons.Filled.Settings,
                                    "Settings"
                                )
                            }
                            IconButton(onClick = viewModel::refresh) {
                                Icon(
                                    Icons.Filled.Refresh,
                                    "Refresh"
                                )
                            }
                            IconButton(onClick = { showHelp = !showHelp }) {
                                Crossfade(showHelp) {
                                    if (it)
                                    {
                                        Icon(Icons.Filled.Help, "Show Help")
                                    } else
                                    {
                                        Icon(Icons.Filled.HelpOutline, "Hide Help")
                                    }
                                }
                            }
                        }
                        Crossfade(showHelp) {
                            if (it)
                            {
                                run block@{
                                    val valuedProperties =
                                        viewModel.valuedProperties.collectAsState(null).value
                                            ?: return@block
                                    val helpTree = viewModel.helpTree.collectAsState(null).value
                                        ?: return@block

                                    HelpCard(valuedProperties, helpTree)
                                }
                            } else
                            {
                                viewModel.tree.collectAsState(initial = null).value?.let { tree ->
                                    DynamicUserInterface(tree)
                                }
                                    ?: run {
                                        Text("Go to the settings menu and set up a dataset", Modifier.padding(20.dp).fillMaxSize(), textAlign = TextAlign.Center)
                                    }
                            }
                        }
                    }
                }
            }
        }
    }
}

