package ro.halex.av.ui.screen.data

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ro.halex.av.viewmodel.DataViewModel

@Composable
fun RelationshipConfiguration(onBackPress:() -> Unit)
{
    viewModel<DataViewModel>().selectedDataset.value ?: return

    var selectedTabIndex by remember { mutableStateOf(0) }
    ScrollableTabRow(selectedTabIndex = selectedTabIndex) {
        Tab.values().forEachIndexed { index, tab ->
            Tab(selected = selectedTabIndex == index, onClick = { selectedTabIndex = index }) {
                Text(tab.label, Modifier.padding(10.dp))
            }
        }
    }
    Scaffold(Modifier.fillMaxSize(), backgroundColor = MaterialTheme.colors.primary) {
        Crossfade(targetState = selectedTabIndex) {
            when (Tab.values()[it])
            {
                Tab.VALUED -> ValuedPropertiesTab()
                Tab.CLASSIFICATION -> ClassificationPropertiesTab()
                Tab.GROUPED -> GroupedPropertiesTab()
                Tab.PREVIEW -> PreviewTab(onBackPress)
            }
        }
    }
}

@Composable
internal fun backgroundButtonColors() =
    ButtonDefaults.textButtonColors(
        backgroundColor = MaterialTheme.colors.background,
        contentColor = MaterialTheme.colors.onBackground,
        disabledContentColor = Color.LightGray
    )