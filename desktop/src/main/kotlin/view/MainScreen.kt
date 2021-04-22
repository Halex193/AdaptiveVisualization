package view

import model.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.ktor.client.*
import io.ktor.http.*
import viewmodel.AddViewModel
import viewmodel.DatasetListViewModel
import viewmodel.MainViewModel

@Composable
fun MainScreen(mainViewModel:MainViewModel)
{
    Row(
        Modifier.fillMaxWidth().padding(top = 10.dp),
        horizontalArrangement = Arrangement.Center
    )
    {
        AddDataset(AddViewModel(mainViewModel))
        DatasetList(DatasetListViewModel(mainViewModel))
    }

}

@Composable
fun ShowProperties(properties: List<String>)
{
    var expanded by remember(properties) { mutableStateOf(false) }
    Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
        OutlinedButton(
            onClick = { expanded = true },
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colors.background
            )
        )
        {
            Text(
                "${properties.size} Properties"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
        ) {
            properties.forEach { property ->
                DropdownMenuItem(onClick = {}, enabled = true) {
                    Text(property)
                }
            }
        }
    }
}
