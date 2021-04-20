import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import io.ktor.client.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

val colors = listOf(0xFF880e4f, 0xFF1a237e, 0xff006064, 0xfff57f17, 0xff263238).map { Color(it) }

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
    //TODO add a loading bar
    val datasets by datasetFlow(client, configuration).collectAsState(
        emptyList(),
        context = coroutineScope.coroutineContext
    )
    Box(Modifier.width(600.dp).padding(horizontal = 15.dp))
    {
        val state = rememberLazyListState()
        LazyColumn(state = state) {
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
        modifier = Modifier.align(Alignment.CenterEnd).height(100.dp),
        adapter = rememberScrollbarAdapter(
            scrollState = state,
            itemCount = 6,
            averageItemSize = 10.dp // TextBox height + Spacer height
        ),
        style = ScrollbarStyle(100.dp, 5.dp, RectangleShape, 100, Color(0xFF000000), Color(0xFF3c003c))
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
    Surface(Modifier.fillMaxSize().padding(bottom = 15.dp).clip(MaterialTheme.shapes.large)) {
        Row {
            Column(Modifier.padding(15.dp).width(400.dp)) {
                with(dataset)
                {
                    Text("Name: $name")
                    Text("Color: $color")
                    Text("Properties: $properties")
                    Text("Creator: $username")
                    Text("Items: $items")
                }
            }
            if (createdByUser)
                Column(Modifier.width(40.dp)) {
                    IconButton(onClick = onDeleteButtonPress)
                    {
                        Icon(Icons.Default.Delete, "Delete")
                    }
                    IconButton(onClick = onUpdatePress)
                    {
                        Icon(Icons.Default.Edit, "Update")
                    }
                }
        }

    }
}

data class FileDetails(val fileObject: File, val items: Int, val properties: List<String>)

private const val defaultName = ""

@OptIn(ExperimentalUnsignedTypes::class)
private val defaultColor = colors[0].value.toString(16)

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
        var selectedColor: String by remember { mutableStateOf(defaultColor) }
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
                @Composable
                fun Modifier.customBorder(addBorder: Boolean): Modifier =
                    if (addBorder)
                        border(3.dp, Color(0xFFE0E0E0), shape = CircleShape)
                    else this
                colors.forEach { color ->
                    Box(
                        Modifier.padding(10.dp).width(40.dp).height(40.dp).clip(CircleShape)
                            .customBorder(selectedColor == color.value.toString(16))
                            .background(color)
                            .clickable { selectedColor = color.value.toString(16) }
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = {
                    coroutineScope.launch {
                        val fileObject = chooseFile() ?: return@launch
                        file = runCatching { csvReader().openAsync(fileObject){
                            val header = readNext() ?: return@openAsync null
                            val itemNumber = readAllAsSequence().count()
                            return@openAsync FileDetails(fileObject, itemNumber,header)
                        } }.getOrNull() ?: return@launch
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
                Button(onClick = {
                    if (!adding)
                    {
                        adding = true
                        coroutineScope.launch {
                            val currentFile = file
                            if (currentFile == null)
                            {
                                onNewMessage(Message("A file needs to be chosen", false))
                            }
                            else
                            {
                                when (addDataset(
                                    client,
                                    configuration,
                                    name,
                                    selectedColor,
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
                Text("Properties")
                Spacer(Modifier.height(4.dp))
                ShowProperties(currentFile.properties)
            }

        }

    }

}

@Composable
fun ShowProperties(properties:List<String>)
{
    Column(Modifier.background(Color(0xFFE0E0E0), shape = MaterialTheme.shapes.medium).padding(5.dp),horizontalAlignment = Alignment.CenterHorizontally) {
        properties.forEach { property ->
            Text(property)
        }
    }
}
