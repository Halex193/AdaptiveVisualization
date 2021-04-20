import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onConfigurationChange: (Configuration) -> Unit,
    onNewMessage: (Message) -> Unit = {}
)
{
    val coroutineScope = rememberCoroutineScope { Dispatchers.Default }
    var host by remember { mutableStateOf(defaultConfiguration.host) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Host: ")
        TextField(value = host, onValueChange = { host = it })
    }

    var username by remember { mutableStateOf(defaultConfiguration.username) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Username: ")
        TextField(value = username, onValueChange = { username = it })
    }

    var password by remember { mutableStateOf(defaultConfiguration.password) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Password: ")
        TextField(
            value = password,
            onValueChange = { password = it },
            visualTransformation = VisualTransformation {
                TransformedText(
                    AnnotatedString("•".repeat(it.text.length)),
                    OffsetMapping.Identity
                )
            })
    }

    var connecting by remember { mutableStateOf(false) }
    Button(onClick = {
        if (!connecting)
        {
            connecting = true
            coroutineScope.launch {
                val currentConfiguration =
                    Configuration(host, username, password)
                when (login(currentConfiguration))
                {
                    HttpStatusCode.OK -> {onConfigurationChange(currentConfiguration)
                    onNewMessage(Message("Logged in", true))}
                    HttpStatusCode.Unauthorized -> onNewMessage(
                        Message(
                            "Invalid credentials",
                            false
                        )
                    )
                    null -> onNewMessage(Message("Connection could not be established", false))
                    else -> onNewMessage(Message("Unknown error occured", false))
                }
                connecting = false
            }
        }
    }, enabled = !connecting)
    {
        Text("Connect")
    }
}
