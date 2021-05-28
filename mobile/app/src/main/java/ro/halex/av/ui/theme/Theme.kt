package ro.halex.av.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

fun Color.blend(modifier: Float): Color
{
    return Color(this.red * modifier, this.green * modifier, this.blue * modifier)
}

@Composable
fun AdaptiveVisualizationTheme(themeColor: Color, content: @Composable() () -> Unit)
{
    val primary = themeColor.blend(0.8f)
    val primaryVariant = themeColor.blend(0.6f)
    val secondary = themeColor.blend(0.2f)

    val colors = lightColors(
        primary = primary,
        primaryVariant = primaryVariant,
        secondary = secondary,
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