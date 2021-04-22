package viewmodel

import androidx.compose.runtime.mutableStateOf
import io.ktor.http.*
import kotlinx.coroutines.launch
import model.Configuration
import model.createHttpClient
import model.defaultConfiguration
import view.Message

class LoginViewModel(private val mainViewModel: MainViewModel)
{
    val host = mutableStateOf(defaultConfiguration.host.substringBefore(':'))
    val port = mutableStateOf(defaultConfiguration.host.substringAfter(':'))
    val username = mutableStateOf(defaultConfiguration.username)
    val password = mutableStateOf(defaultConfiguration.password)
    val connecting = mutableStateOf(false)

    fun login()
    {
        if (connecting.value) return
        connecting.value = true
        mainViewModel.coroutineScope.launch {
            val currentConfiguration =
                Configuration("${host.value}:${port.value}", username.value, password.value)
            when (model.login(currentConfiguration))
            {
                HttpStatusCode.OK ->
                {
                    mainViewModel.client = createHttpClient(currentConfiguration)
                    mainViewModel.configuration.value = currentConfiguration
                    mainViewModel.message.value = Message("Logged in", true)
                }
                HttpStatusCode.Unauthorized -> mainViewModel.message.value =
                    Message(
                        "Invalid credentials",
                        false
                    )

                null -> mainViewModel.message.value =
                    Message(
                        "Connection could not be established",
                        false
                    )

                else -> mainViewModel.message.value = Message("Unknown error occured", false)
            }
            connecting.value = false
        }

    }
}
