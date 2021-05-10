import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.unit.dp
import ro.halex.av.viewmodel.LoginViewModel

@Composable
fun LoginScreen(loginViewModel: LoginViewModel)
{
    Surface(shape = MaterialTheme.shapes.large, modifier = Modifier.width(500.dp)) {
        Column(Modifier.fillMaxWidth().padding(15.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            var host by loginViewModel.host
            var port by loginViewModel.port
            var username by loginViewModel.username
            var password by loginViewModel.password

            val columnSpacing = Modifier.padding(10.dp)
            val rowSpacing = Modifier.width(5.dp)
            Text("Server connection details", style = MaterialTheme.typography.h6)
            Spacer(columnSpacing)
            Column(Modifier, horizontalAlignment = Alignment.End) {
                Row(columnSpacing, verticalAlignment = Alignment.CenterVertically) {
                    Text("Host")
                    Spacer(rowSpacing)
                    TextField(value = host, onValueChange = { host = it })
                }


                Row(columnSpacing, verticalAlignment = Alignment.CenterVertically) {
                    Text("Port")
                    Spacer(rowSpacing)
                    TextField(value = port, onValueChange = { port = it })
                }

                Row(columnSpacing, verticalAlignment = Alignment.CenterVertically) {
                    Text("Username")
                    Spacer(rowSpacing)
                    TextField(value = username, onValueChange = { username = it })
                }

                Row(columnSpacing, verticalAlignment = Alignment.CenterVertically) {
                    Text("Password")
                    Spacer(rowSpacing)
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        visualTransformation = {
                            TransformedText(
                                AnnotatedString("•".repeat(it.text.length)),
                                OffsetMapping.Identity
                            )
                        })
                }
            }

            val connecting by loginViewModel.connecting
            Spacer(columnSpacing)
            Button(
                modifier = Modifier.width(100.dp),
                colors  =ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colors.surface,
                    backgroundColor = MaterialTheme.colors.background
                ),
                onClick = loginViewModel::login,
                enabled = !connecting
            )
            {
                if (!connecting)
                    Text("Connect")
                else
                    CircularProgressIndicator(
                        Modifier.size(20.dp),
                        color = MaterialTheme.colors.onBackground
                    )
            }
        }
    }
}
