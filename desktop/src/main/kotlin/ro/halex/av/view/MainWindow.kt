package ro.halex.av.view

import LoginScreen
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Logout
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.svgResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import ro.halex.av.viewmodel.MainViewModel
import java.io.File
import javax.swing.JFileChooser
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

data class Message(val text: String, val success: Boolean)

@OptIn(ExperimentalTime::class)
@Composable
fun MainWindow(mainViewModel: MainViewModel)
{
    MaterialTheme(
        colors = lightColors(
            background = Color(0xFF3C003C),
            onBackground = Color.White,
            surface = Color.White,
            onSurface = Color.Black
        ),
        shapes = Shapes(large = RoundedCornerShape(20.dp), medium = RoundedCornerShape(10.dp))
    ) {
        Scaffold {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 30.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                var mutableMessage by mainViewModel.message
                LaunchedEffect(mutableMessage)
                {
                    if (mutableMessage != null)
                    {
                        delay(5.seconds)
                        mutableMessage = null
                    }
                }
                val configuration = mainViewModel.configuration.value
                Box(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.align(Alignment.Center).wrapContentHeight(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            svgResource("images/icon.svg"),
                            "Logo",
                            modifier = Modifier.height(100.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(Modifier.width(30.dp))
                        Text(
                            "Adaptive Visualization\nDataset Configuration",
                            style = MaterialTheme.typography.h4
                        )
                    }


                    if (configuration != null)
                        Row(
                            Modifier.align(Alignment.CenterEnd),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(configuration.username)
                            IconButton(onClick = {
                                mainViewModel.destroyConfiguration()
                                mutableMessage = Message("Logged out", true)
                            })
                            {
                                Icon(Icons.Filled.Logout, "Logout")
                            }
                        }
                }
                val message = mutableMessage
                val messageDimensions = Modifier.height(80.dp)
                if (message == null)
                    Spacer(messageDimensions)
                else
                    Surface(
                        messageDimensions.padding(15.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(Color.White)

                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Row(
                                Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (message.success)
                                    Icon(Icons.Filled.Check, "Success")
                                else
                                    Icon(Icons.Filled.Error, "Error")
                                Spacer(Modifier.width(10.dp))
                                Text(message.text)
                                Spacer(Modifier.width(10.dp))
                            }
                            IconButton(
                                onClick = { mutableMessage = null },
                                Modifier.height(15.dp).width(30.dp)
                            )
                            {
                                Icon(Icons.Filled.Clear, "Clear")
                            }
                            Spacer(Modifier.width(15.dp))
                        }
                    }
                if (configuration == null)
                {
                    LoginScreen(mainViewModel.loginViewModel)
                }
                else
                {
                    MainScreen(mainViewModel)
                }
            }
        }
    }
}

fun chooseFile(): File?
{
    val fileChooser =
        JFileChooser(File("I:\\Preparation of Bachelors Thesis\\Bachelor Project\\Adaptive-Visualizer\\desktop\\data"))
    return when (fileChooser.showOpenDialog(null))
    {
        JFileChooser.APPROVE_OPTION -> fileChooser.selectedFile
        else -> null
    }
}
