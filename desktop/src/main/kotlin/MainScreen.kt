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
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import io.ktor.client.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

val colors = listOf(0xFF880e4f, 0xFF1a237e, 0xff006064, 0xfff57f17, 0xff263238)

@Composable
fun MainScreen(
    configuration: Configuration,
    onConfigurationInvalidate: () -> Unit,
    onNewMessage: (Message) -> Unit = {}
)
{
    val client: HttpClient = remember { createHttpClient(configuration) }
    Row(
        Modifier.fillMaxWidth().padding(top = 10.dp),
        horizontalArrangement = Arrangement.Center
    )
    {
        AddDataset(
            client,
            configuration,
            onConfigurationInvalidate,
            onNewMessage = onNewMessage
        )
        DatasetList(
            client,
            configuration,
            onConfigurationInvalidate,
            onNewMessage = onNewMessage
        )
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DatasetList(
    client: HttpClient,
    configuration: Configuration,
    onConfigurationInvalidate: () -> Unit,
    onNewMessage: (Message) -> Unit = {}
)
{
    val coroutineScope = rememberCoroutineScope { Dispatchers.Default }
    val mutableDatasets by datasetFlow(client, configuration).collectAsState(
        null,
        context = coroutineScope.coroutineContext
    )
    Box(Modifier.width(600.dp).fillMaxHeight().padding(horizontal = 15.dp), contentAlignment = Alignment.TopCenter)
    {
        val datasets = mutableDatasets
        if (datasets == null)
        {
            Column {
                Spacer(Modifier.height(30.dp))
                LinearProgressIndicator(color = MaterialTheme.colors.onBackground)
            }

            return@Box
        }
        val state = rememberLazyListState()
        val itemCount = datasets.size

        LazyColumn(Modifier.fillMaxSize().padding(end = 15.dp), state) {
            items(datasets) {
                Dataset(
                    it, configuration.username == it.username,
                    onDeleteButtonPress = {
                        coroutineScope.launch {
                            when (deleteDataset(client, configuration, it.name))
                            {
                                HttpStatusCode.OK -> onNewMessage(Message("Dataset deleted", true))
                                HttpStatusCode.Unauthorized ->
                                {
                                    onNewMessage(
                                        Message(
                                            "Invalid credentials",
                                            false
                                        )
                                    )
                                    onConfigurationInvalidate()
                                }
                                null -> onNewMessage(Message("Connection to server failed", false))
                                else -> onNewMessage(Message("Unknown error occurred", false))
                            }
                        }
                    },
                    onUpdatePress = {
                        coroutineScope.launch {
                            val file = chooseFile() ?: return@launch
                            when (updateDataset(client, configuration, it.name, file))
                            {
                                HttpStatusCode.OK -> onNewMessage(
                                    Message(
                                        "Dataset updated",
                                        true
                                    )
                                )
                                HttpStatusCode.Unauthorized ->
                                {
                                    onNewMessage(
                                        Message(
                                            "Invalid credentials",
                                            false
                                        )
                                    );onConfigurationInvalidate()
                                }
                                HttpStatusCode.Conflict -> onNewMessage(
                                    Message(
                                        "Invalid properties",
                                        false
                                    )
                                )
                                HttpStatusCode.BadRequest -> onNewMessage(
                                    Message(
                                        "Invalid dataset",
                                        false
                                    )
                                )
                                null -> onNewMessage(
                                    Message(
                                        "Connection to server failed",
                                        false
                                    )
                                )
                                else -> onNewMessage(Message("Unknown error occurred", false))
                            }
                        }
                    })


            }
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(
                scrollState = state,
                itemCount = itemCount,
                averageItemSize = 100.dp // TextBox height + Spacer height
            ),
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
fun Dataset(
    dataset: Dataset,
    createdByUser: Boolean = false,
    onDeleteButtonPress: () -> Unit = {},
    onUpdatePress: () -> Unit = {}
)
{
    Surface(
        Modifier.fillMaxWidth().height(100.dp).padding(bottom = 15.dp)
            .clip(MaterialTheme.shapes.large)
    ) {
        Row(Modifier.wrapContentHeight(), verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.width(5.dp))
            Box(
                Modifier.size(20.dp)
                    .background(Color(dataset.color.toLong(16)), shape = CircleShape)
            )
            Column(Modifier.padding(10.dp).wrapContentWidth()) {
                with(dataset)
                {
                    Text(name, style = MaterialTheme.typography.h6)
                    Text("Creator: $username")
                }
            }
            ShowProperties(dataset.properties)
            Spacer(Modifier.width(5.dp))
            Text("${dataset.items}\nItems", Modifier.weight(1f), textAlign = TextAlign.Center)

            if (createdByUser)
            {
                IconButton(onClick = onDeleteButtonPress)
                {
                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colors.background)
                }
                IconButton(onClick = onUpdatePress)
                {
                    Icon(Icons.Default.Edit, "Update", tint = MaterialTheme.colors.background)
                }
                Spacer(Modifier.width(5.dp))
            }
        }

    }
}

data class FileDetails(val fileObject: File, val items: Int, val properties: List<String>)

private const val defaultName = ""

@OptIn(ExperimentalUnsignedTypes::class)
private val defaultColor = colors[0]

@OptIn(ExperimentalUnsignedTypes::class)
@Composable
fun AddDataset(
    client: HttpClient,
    configuration: Configuration,
    onConfigurationInvalidate: () -> Unit,
    onNewMessage: (Message) -> Unit = {}
)
{
    val coroutineScope = rememberCoroutineScope { Dispatchers.Default }
    Surface(Modifier.width(400.dp).clip(MaterialTheme.shapes.large)) {
        var name: String by remember { mutableStateOf(defaultName) }
        var selectedColor: Long by remember { mutableStateOf(defaultColor) }
        var file: FileDetails? by remember { mutableStateOf(null) }
        Column(
            Modifier.fillMaxWidth().padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Create dataset", style = MaterialTheme.typography.h6)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.width(5.dp))
                TextField(name, onValueChange = { name = it })
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                val colorObjects = remember { colors.associateWith { Color(it) } }
                colors.forEach { color ->
                    Box(
                        Modifier.padding(10.dp).size(40.dp).background(
                            colorObjects[color]
                                ?: error("Selected color is not in permitted values"),
                            shape = CircleShape
                        ).clip(CircleShape).clickable { selectedColor = color },
                        contentAlignment = Alignment.Center
                    )
                    {
                        if (selectedColor == color)
                            Box(
                                Modifier.size(30.dp).background(
                                    MaterialTheme.colors.surface, shape = CircleShape
                                ),
                            )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                val buttonColors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colors.surface,
                    backgroundColor = MaterialTheme.colors.background
                )
                TextButton(colors = buttonColors, onClick = {
                    coroutineScope.launch {
                        val fileObject = chooseFile() ?: return@launch
                        file = runCatching {
                            csvReader().openAsync(fileObject) {
                                val header = readNext() ?: return@openAsync null
                                val itemNumber = readAllAsSequence().count()
                                return@openAsync FileDetails(fileObject, itemNumber, header)
                            }
                        }.getOrNull() ?: return@launch
                    }
                })
                {
                    if (file == null)
                        Text("Choose file")
                    else
                        Text("Change file")
                }
                Spacer(Modifier.width(10.dp))
                var adding by remember { mutableStateOf(false) }
                TextButton(colors = buttonColors, onClick = {
                    if (!adding)
                    {
                        adding = true
                        coroutineScope.launch {
                            val currentFile = file
                            if (currentFile == null)
                            {
                                onNewMessage(
                                    Message(
                                        "You need to choose a file for the dataset",
                                        false
                                    )
                                )
                            }
                            else if (name == "")
                            {
                                onNewMessage(
                                    Message(
                                        "You need to choose a name for the dataset",
                                        false
                                    )
                                )
                            }
                            else
                            {
                                when (addDataset(
                                    client,
                                    configuration,
                                    name,
                                    selectedColor.toString(16),
                                    currentFile.fileObject
                                ))
                                {
                                    HttpStatusCode.OK -> onNewMessage(
                                        Message(
                                            "Dataset added",
                                            true
                                        )
                                    )
                                    HttpStatusCode.Unauthorized -> onConfigurationInvalidate()
                                    HttpStatusCode.Conflict -> onNewMessage(
                                        Message(
                                            "Dataset already exists",
                                            false
                                        )
                                    )
                                    HttpStatusCode.BadRequest -> onNewMessage(
                                        Message(
                                            "Invalid dataset",
                                            false
                                        )
                                    )
                                    null -> onNewMessage(
                                        Message(
                                            "Connection to server failed",
                                            false
                                        )
                                    )
                                    else -> onNewMessage(Message("Unknown error occured", false))
                                }
                            }
                            adding = false
                            name = defaultName
                            selectedColor = defaultColor
                            file = null
                        }
                    }
                }, enabled = !adding)
                {
                    Text("Create")
                }
            }
            file?.let { currentFile ->
                Spacer(Modifier.height(10.dp))
                Text("${currentFile.fileObject.name} - ${currentFile.items} items")
                Spacer(Modifier.height(10.dp))
                Spacer(Modifier.height(4.dp))
                ShowProperties(currentFile.properties)
            }

        }

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
