package ro.halex.av.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ro.halex.av.viewmodel.MainViewModel

@Composable
fun MainScreen(mainViewModel: MainViewModel)
{
    Row(
        Modifier.fillMaxWidth().padding(top = 10.dp),
        horizontalArrangement = Arrangement.Center
    )
    {
        val (addViewModel, datasetListViewModel) = mainViewModel.mainScreenViewModels.value ?: return@Row
        AddDataset(addViewModel)
        DatasetList(datasetListViewModel)
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
