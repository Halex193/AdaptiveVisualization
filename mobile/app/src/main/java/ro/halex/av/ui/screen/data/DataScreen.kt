package ro.halex.av.ui.screen.data

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import ro.halex.av.backend.*
import ro.halex.av.ui.theme.AdaptiveVisualizationTheme
import ro.halex.av.viewmodel.DataViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DataScreen(onBackPress: () -> Unit)
{
    val viewModel = viewModel<DataViewModel>()
    val selectedDataset = viewModel.selectedDataset.value
    val color = selectedDataset?.color?.let { Color(it.toLong(16)) } ?: Color.DarkGray
    AdaptiveVisualizationTheme(themeColor = color) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
            Column(Modifier.fillMaxSize()) {
                Header(onBackPress)
                if (viewModel.mutableConnectionURL.value != null)
                {
                    ConnectionRow()
                    val datasets = viewModel.datasets.value
                    AnimatedVisibility(visible = datasets != null) {
                        DatasetSelection()
                    }
                    RelationshipConfiguration()
                }
            }
        }
    }
}