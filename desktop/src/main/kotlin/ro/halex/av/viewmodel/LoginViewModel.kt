package ro.halex.av.viewmodel

import androidx.compose.runtime.mutableStateOf
import io.ktor.http.*
import kotlinx.coroutines.launch
import ro.halex.av.model.Configuration
import ro.halex.av.model.defaultConfiguration
import ro.halex.av.view.Message

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
            when (ro.halex.av.model.login(currentConfiguration))
            {
                HttpStatusCode.OK ->
                {
                    mainViewModel.createConfiguration(currentConfiguration)
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
