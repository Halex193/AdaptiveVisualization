package ro.halex.av

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigate
import androidx.navigation.compose.rememberNavController
import ro.halex.av.ui.screen.AdaptScreen
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
                    composable("main") { MainScreen(onAdaptPress = {navController.navigate("adapt")}) }
                    composable("adapt") { AdaptScreen(onDataPress = {navController.navigate("data")}) { navController.navigateUp() } }
                    composable("data") { DataScreen(onBackPress = {navController.navigateUp()}) }
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