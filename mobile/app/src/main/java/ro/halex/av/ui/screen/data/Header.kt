package ro.halex.av.ui.screen.data

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun Header(onBackPress: () -> Unit)
{
    Box(
        Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        IconButton(
            onClick = onBackPress,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(Icons.Default.ArrowBack, "Back")
        }
        Text(
            "Settings",
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            style = MaterialTheme.typography.h5,
            textAlign = TextAlign.Center
        )
    }
}