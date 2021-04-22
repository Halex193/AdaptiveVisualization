package model

data class Configuration(val host: String, val username: String, val password: String)
{
    val url: String = "http://$host"
    val webSocketUrl = "ws://$host"
}
