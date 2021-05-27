package ro.halex.av.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun AdaptiveVisualizationTheme(themeColor: Color, content: @Composable() () -> Unit)
{
    val darkenPercentage = 0.8f
    val secondary = Color(themeColor.red * darkenPercentage, themeColor.green * darkenPercentage, themeColor.blue * darkenPercentage)
    val colors = lightColors(
        primary = secondary,
        primaryVariant = secondary,
        secondary = themeColor,
        background = themeColor,
        onBackground = Color.White,
        surface = Color.White,
        onSurface = Color.Black,
        onPrimary = Color.White,
        onSecondary = Color.White,
    )

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}