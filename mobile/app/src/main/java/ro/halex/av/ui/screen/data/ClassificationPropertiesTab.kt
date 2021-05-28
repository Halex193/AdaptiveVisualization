package ro.halex.av.ui.screen.data

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import ro.halex.av.viewmodel.DataViewModel

@Composable
fun ClassificationPropertiesTab()
{
    val viewModel: DataViewModel = viewModel()
    val classificationProperties = viewModel.mutableClassificationProperties
}