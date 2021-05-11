package ro.halex.av.ui.screen

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
fun MainScreen(onDataPress: () -> Unit)
{
    Button(onClick = onDataPress) {
        Text("Go to data screen")
    }
}