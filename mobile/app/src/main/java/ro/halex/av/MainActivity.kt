package ro.halex.av

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigate
import androidx.navigation.compose.rememberNavController
import ro.halex.av.ui.screen.DataScreen
import ro.halex.av.ui.screen.MainScreen
import ro.halex.av.ui.theme.AdaptiveVisualizationTheme

class MainActivity : ComponentActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContent {
            AdaptiveVisualizationTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "data") {
                    composable("main") { MainScreen(onDataPress = { navController.navigate("data") }) }
                    composable("data") { DataScreen(onBackPress = { navController.navigateUp() }) }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview()
{
    AdaptiveVisualizationTheme {

    }
}