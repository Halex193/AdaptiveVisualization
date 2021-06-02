package ro.halex.av.ui.screen.data

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
internal fun CustomButton(
    onClick: () -> Unit,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
)
{
    Button(
        onClick = onClick,
        colors = colors,
        enabled = enabled,

        //TODO remove in later releases and look out for TRULY random crashes on button presses
        modifier = Modifier,
        interactionSource = remember { MutableInteractionSource() },
        shape = MaterialTheme.shapes.small,
        elevation = ButtonDefaults.elevation(),
        contentPadding = ButtonDefaults.ContentPadding,
        content = content
    )
}