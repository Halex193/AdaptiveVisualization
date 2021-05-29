package ro.halex.av.ui.screen.data

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ro.halex.av.R
import ro.halex.av.backend.SortingOrder

internal val icons = linkedMapOf(
    SortingOrder.ASCENDING to R.drawable.sort_asc,
    SortingOrder.DESCENDING to R.drawable.sort_desc,
    SortingOrder.INCREASING to R.drawable.sort_incr,
    SortingOrder.DECREASING to R.drawable.sort_decr
)

@Composable
internal fun SortingOrderPicker(sortingOrder: SortingOrder, onSortingOrderChange: (SortingOrder) -> Unit)
{
    Row(Modifier.padding(5.dp)) {
        for ((sort, icon) in icons)
        {
            IconButton(modifier = Modifier.padding(5.dp).size(35.dp).clip(CircleShape), onClick = { if (sort != sortingOrder) onSortingOrderChange(sort) }) {
                val transition = updateTransition(
                    targetState = sortingOrder == sort,
                    label = "Color change transition"
                )
                val backgroundColor by transition.animateColor(label = "Background color") {
                    if (it) MaterialTheme.colors.onPrimary else MaterialTheme.colors.primary
                }
                val tintColor by transition.animateColor(label = "Tint color") {
                    if (it) MaterialTheme.colors.primary else MaterialTheme.colors.onPrimary
                }
                Icon(
                    painterResource(icon), sort.toString(),
                    modifier = Modifier
                        .background(backgroundColor, CircleShape)
                        .padding(5.dp),
                    tint = tintColor
                )
            }
        }
    }
}