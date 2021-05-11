package ro.halex.av.ui.screen

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import ro.halex.av.Greeting
import ro.halex.av.viewmodel.AbstractViewModel

@Composable
fun DataScreen(onBackPress: () -> Boolean)
{
// A surface container using the 'background' color from the theme
    Surface(color = MaterialTheme.colors.background) {
        Greeting("Android")
        val viewModel = viewModel<AbstractViewModel>()
        val datasets = viewModel.getDatasets().value ?: return@Surface
        LazyColumn {
            items(datasets) {
                Text(text = it.toString())
            }
        }
    }
}