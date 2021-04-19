import androidx.compose.desktop.Window
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
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
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.ktor.client.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.swing.JFileChooser

fun main()
{
    Window(size = IntSize(1000, 800)) {
//        var mutableConfiguration: Configuration? by remember { mutableStateOf(null) }
        var mutableConfiguration: Configuration? by remember {
            mutableStateOf(
                Configuration(
                    "localhost:8080",
                    "admin",
                    "admin"
                )
            )
        }
        MaterialTheme(
            colors = lightColors(
                background = Color(0xFF3C003C),
                onBackground = Color.White
            ),
            shapes = Shapes(large = RoundedCornerShape(20.dp))
        ) {
            Scaffold {
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = 30.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Dataset Configuration", style = MaterialTheme.typography.h4)
                    Spacer(Modifier.height(50.dp))
                    val configuration = mutableConfiguration
                    if (configuration == null)
                    {
                        LoginScreen(onConfigurationChange = { mutableConfiguration = it })
                    }
                    else
                    {
                        MainScreen(
                            configuration,
                            onConfigurationInvalidate = { mutableConfiguration = null })
                    }
                }
            }
        }
    }
}

@Composable
private fun MainScreen(configuration: Configuration, onConfigurationInvalidate: () -> Unit)
{
    val client: HttpClient = remember { createHttpClient(configuration) }
    Text("Logged in as ${configuration.username}")
    var message by remember { mutableStateOf("")}
    Text(message)
    Spacer(Modifier.height(50.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center)
    {
        AddDataset(client, configuration, onConfigurationInvalidate, onMessageChange = {message = it})
        DatasetList(client, configuration, onConfigurationInvalidate, onMessageChange = {message = it})
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DatasetList(
    client: HttpClient,
    configuration: Configuration,
    onConfigurationInvalidate: () -> Unit,
    onMessageChange: (String)->Unit = {}
)
{
    val coroutineScope = rememberCoroutineScope { Dispatchers.Default }
    //TODO add a loading bar
    val datasets by datasetFlow(client, configuration).collectAsState(emptyList(), context = coroutineScope.coroutineContext)
    Box(
        modifier = Modifier.width(600.dp)
    )
    {
        val state = rememberLazyListState()
        LazyColumn(state = state) {
            items(datasets) {
                Dataset(it, configuration.username == it.username,
                    onDeleteButtonPress = {
                    coroutineScope.launch {
                        when(deleteDataset(client, configuration, it.name))
                        {
                            HttpStatusCode.OK -> onMessageChange("Dataset deleted")
                            HttpStatusCode.Unauthorized -> onConfigurationInvalidate()
                            null -> onMessageChange("Connection to server failed")
                            else -> onMessageChange("Unknown error occurred")
                        }
                    }
                },
                    onUpdatePress = { file ->
                        coroutineScope.launch {
                            when (updateDataset(client, configuration, it.name, file))
                            {
                                HttpStatusCode.OK -> onMessageChange("Dataset updated")
                                HttpStatusCode.Unauthorized -> onConfigurationInvalidate()
                                HttpStatusCode.Conflict -> onMessageChange("Invalid properties")
                                HttpStatusCode.BadRequest -> onMessageChange("Invalid dataset")
                                null -> onMessageChange("Connection to server failed")
                                else -> onMessageChange("Unknown error occurred")
                            }
                        }
                    })
            }
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(
                scrollState = state,
                itemCount = datasets.size,
                averageItemSize = 20.dp // TextBox height + Spacer height
            )
        )
    }

}

@Composable
private fun Dataset(
    dataset: Dataset,
    createdByUser: Boolean = false,
    onDeleteButtonPress: () -> Unit = {},
    onUpdatePress: (File) -> Unit = {}
)
{
    Surface(Modifier.fillMaxSize().padding(15.dp).clip(MaterialTheme.shapes.large)) {
        Row {
            Column(Modifier.padding(15.dp).width(400.dp)) {
                with(dataset)
                {
                    Text("Name: $name")
                    Text("Color: ${color.toString(16)}")
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
                    IconButton(onClick = {
                        val fileChooser =
                            JFileChooser(File("I:\\Preparation of Bachelors Thesis\\Bachelor Project\\Adaptive-Visualizer\\desktop\\data"))
                        when (fileChooser.showOpenDialog(null))
                        {
                            JFileChooser.APPROVE_OPTION -> onUpdatePress(fileChooser.selectedFile)
                        }
                    })
                    {
                        Icon(Icons.Default.Edit, "Update")
                    }
                }
        }

    }
}

@Composable
private fun AddDataset(
    client: HttpClient,
    configuration: Configuration,
    onConfigurationInvalidate: () -> Unit,
    onMessageChange:(String) -> Unit = {}
)
{
    val coroutineScope = rememberCoroutineScope { Dispatchers.Default }
    Surface(Modifier.width(400.dp).clip(MaterialTheme.shapes.large)) {
        var name: String by remember { mutableStateOf("") }
        var color: Int by remember { mutableStateOf("FFFFFF".toInt(16)) }
        var file: File? by remember { mutableStateOf(null) }
        Column(
            Modifier.fillMaxWidth().padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Name:")
                TextField(name, onValueChange = { name = it })
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Color:")
                TextField(color.toString(16), onValueChange = { color = it.toIntOrNull(16) ?: 0 })
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("File:")

                Button(onClick = {
                    coroutineScope.chooseFile { file = it }
                })
                {
                    val text = file?.name ?: "Choose file..."
                    Text(text)
                }
            }
            var adding by remember { mutableStateOf(false) }
            Button(onClick = {
                if (!adding)
                {
                    adding = true
                    coroutineScope.launch {
                        val currentFile = file
                        if (currentFile == null)
                        {
                            onMessageChange("A file needs to be chosen")
                        }
                        else
                        {
                            when (addDataset(client, configuration, name, color, currentFile))
                            {
                                HttpStatusCode.OK -> onMessageChange("Dataset added")
                                HttpStatusCode.Unauthorized -> onConfigurationInvalidate()
                                HttpStatusCode.Conflict -> onMessageChange("Dataset already exists")
                                HttpStatusCode.BadRequest -> onMessageChange("Invalid dataset")
                                null -> onMessageChange("Connection to server failed")
                                else -> onMessageChange("Unknown error occured")
                            }
                        }
                        adding = false
                    }
                }
            }, enabled = !adding)
            {
                Text("Add dataset")
            }
        }

    }

}

@Composable
private fun LoginScreen(onConfigurationChange: (Configuration) -> Unit)
{
    val coroutineScope = rememberCoroutineScope { Dispatchers.Default }
    var host by remember { mutableStateOf("") }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Host: ")
        TextField(value = host, onValueChange = { host = it })
    }

    var username by remember { mutableStateOf("") }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Username: ")
        TextField(value = username, onValueChange = { username = it })
    }

    var password by remember { mutableStateOf("") }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Password: ")
        TextField(value = password, onValueChange = { password = it })
    }

    var connecting by remember { mutableStateOf(false) }
    var error: String? by remember { mutableStateOf(null) }
    Button(onClick = {
        if (!connecting)
        {
            connecting = true
            coroutineScope.launch {
                val currentConfiguration =
                    Configuration(host, username, password)
                when (login(currentConfiguration))
                {
                    HttpStatusCode.OK -> onConfigurationChange(currentConfiguration)
                    HttpStatusCode.Unauthorized -> error = "Invalid credentials"
                    null -> error = "Connection could not be established"
                    else -> error = "Unknown error occured"
                }
                connecting = false
            }
        }
    }, enabled = !connecting)
    {
        Text("Connect")
    }
    error?.let { Text(text = it) }
}

private fun CoroutineScope.chooseFile(onFileChoose: (File?) -> Unit)
{
    launch {
        val fileChooser =
            JFileChooser(File("I:\\Preparation of Bachelors Thesis\\Bachelor Project\\Adaptive-Visualizer\\desktop\\data"))
        when (fileChooser.showOpenDialog(null))
        {
            JFileChooser.APPROVE_OPTION -> onFileChoose(fileChooser.selectedFile)
            else -> onFileChoose(null)
        }
    }
}
