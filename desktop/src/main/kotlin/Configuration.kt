data class Configuration(val host: String, val username: String, val password: String)
{
    val URL: String = "http://$host"

    val webSocketURL = "ws://$host"
}
