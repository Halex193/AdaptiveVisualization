package ro.halex.av.view

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
import ro.halex.av.viewmodel.DatasetListViewModel

@Composable
fun DatasetList(datasetListViewModel: DatasetListViewModel)
{
    val mutableDatasets by datasetListViewModel.datasetListFlow().collectAsState(null)
    Box(
        Modifier.width(600.dp).fillMaxHeight().padding(horizontal = 15.dp),
        contentAlignment = Alignment.TopCenter
    )
    {
        @Suppress("UnnecessaryVariable")
        val datasetViewModels = mutableDatasets
        if (datasetViewModels == null)
        {
            Column {
                Spacer(Modifier.height(30.dp))
                LinearProgressIndicator(color = MaterialTheme.colors.onBackground)
            }
            return@Box
        }
        val state = rememberLazyListState()

        LazyColumn(Modifier.fillMaxSize().padding(end = 15.dp), state) {
            items(datasetViewModels) {
                Dataset(it)
            }
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(scrollState = state),
            style = ScrollbarStyle(
                0.dp,
                6.dp,
                RoundedCornerShape(2.dp),
                0,
                Color(0xFFFFFFFF),
                Color(0xFFFFFFFF)
            )
        )
    }
}


@Composable
fun Dataset(datasetViewModel: DatasetListViewModel.DatasetViewModel)
{
    Surface(
        Modifier.fillMaxWidth().height(100.dp).padding(bottom = 15.dp)
            .clip(MaterialTheme.shapes.large)
    ) {
        Row(Modifier.wrapContentHeight(), verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.width(5.dp))
            Box(
                Modifier.size(20.dp)
                    .background(Color(datasetViewModel.dataset.color.toLong(16)), shape = CircleShape)
            )
            Column(Modifier.padding(10.dp).wrapContentWidth()) {
                Text(datasetViewModel.dataset.name, style = MaterialTheme.typography.h6)
                Text("Creator: ${datasetViewModel.dataset.username}")
            }
            ShowProperties(datasetViewModel.dataset.properties)
            Spacer(Modifier.width(5.dp))
            Text("${datasetViewModel.dataset.items}\nItems", Modifier.weight(1f), textAlign = TextAlign.Center)

            if (datasetViewModel.createdByUser)
            {
                val deleteLoading by datasetViewModel.deleteLoading
                IconButton(onClick = datasetViewModel::delete, enabled = !deleteLoading)
                {
                    if (!deleteLoading)
                        Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colors.background)
                    else
                        CircularProgressIndicator(Modifier.size(30.dp))
                }

                val updateLoading by datasetViewModel.updateLoading
                IconButton(onClick = datasetViewModel::update, enabled = !updateLoading)
                {
                    if (!updateLoading)
                        Icon(Icons.Default.Edit, "Update", tint = MaterialTheme.colors.background)
                    else
                        CircularProgressIndicator(Modifier.size(30.dp))
                }
                Spacer(Modifier.width(5.dp))
            }
            else
            {
                Spacer(Modifier.width(80.dp))
            }
        }

    }
}
