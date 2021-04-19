import kotlinx.serialization.Serializable

@Serializable
data class Dataset(val name: String, val color: Int, val properties: List<String>, val username: String, val items: Int)
