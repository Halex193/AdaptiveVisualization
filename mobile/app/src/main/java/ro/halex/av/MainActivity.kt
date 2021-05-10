package ro.halex.av

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import ro.halex.av.ui.theme.AdaptiveVisualizationTheme
import ro.halex.av.viewmodel.MainViewModel

class MainActivity : ComponentActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContent {
            AdaptiveVisualizationTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Greeting("Android")
                    val viewModel = viewModel<MainViewModel>()
                    LaunchedEffect(null) {
                        viewModel.getAndLogDatasets()
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String)
{
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview()
{
    AdaptiveVisualizationTheme {
        Greeting("Android")
    }
}