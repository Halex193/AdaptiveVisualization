package ro.halex.av

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ro.halex.av.ui.screen.MainScreen
import ro.halex.av.ui.screen.data.DataScreen

class MainActivity : ComponentActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContent {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "main") {
                    composable("main") { MainScreen(onDataPress = { navController.navigate("data") }) }
                    composable("data") { DataScreen(onBackPress = { navController.navigateUp() }) }
                }
        }
    }
}