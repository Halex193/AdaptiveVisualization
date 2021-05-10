package ro.halex.av.model

val defaultConfiguration = Configuration(
    "localhost:8080",
    "Halex",
    "admin"
)

const val defaultName = ""

@OptIn(ExperimentalUnsignedTypes::class)
val defaultColor = datasetColors[0]
