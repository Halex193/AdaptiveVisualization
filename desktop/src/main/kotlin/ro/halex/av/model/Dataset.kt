package ro.halex.av.model

import kotlinx.serialization.Serializable

@Serializable
data class Dataset(val name: String, val color: String, val properties: List<String>, val username: String, val items: Int)
