package viewmodel

import androidx.compose.runtime.*
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import io.ktor.client.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import model.*
import view.Message
import view.chooseFile
import kotlin.properties.Delegates

class MainViewModel(val coroutineScope: CoroutineScope)
{
    var client: HttpClient by Delegates.notNull()
    val configuration: MutableState<Configuration?> = mutableStateOf(null)
    val message: MutableState<Message?> = mutableStateOf(null)
}


