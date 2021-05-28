package ro.halex.av.ui.screen.data

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ro.halex.av.viewmodel.DataViewModel

@Composable
fun ConnectionRow()
{
    val viewModel: DataViewModel = viewModel()
    val (connectionURL, onConnectionURLChange) = viewModel.mutableConnectionURL
    connectionURL ?: return
    val datasets = viewModel.datasets.value
    Crossfade(targetState = datasets) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(70.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            if (it == null)
            {
                TextField(
                    value = connectionURL,
                    onValueChange = onConnectionURLChange,
                    singleLine = true,
                    label = { Text("URL", color = MaterialTheme.colors.onPrimary) },
                    placeholder = { Text("http://domain.com:8080") }
                )
                val datasetsLoading = viewModel.datasetsLoading.value
                Spacer(Modifier.width(10.dp))
                FloatingActionButton(onClick = { if (!datasetsLoading) viewModel.getDatasets() }) {
                    if (datasetsLoading)
                        CircularProgressIndicator(Modifier.size(30.dp))
                    else
                        Icon(Icons.Filled.KeyboardArrowRight, "Get datasets")
                }
            } else
            {
                Box(
                    Modifier
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                        .background(MaterialTheme.colors.onBackground, CircleShape)
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                )
                {
                    Text("URL", color = MaterialTheme.colors.background)
                }
                Spacer(Modifier.width(5.dp))
                Text("$connectionURL")
                Spacer(Modifier.width(15.dp))
                IconButton(
                    onClick = viewModel::resetDatasets,
                    Modifier.size(20.dp)
                ) {
                    Icon(Icons.Filled.Edit, "Edit URL")
                }
            }
        }
    }
}