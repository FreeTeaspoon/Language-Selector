package vegabobo.languageselector.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.BasicComponent

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LocaleItemList(
    itemText: String,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit
) {
    BasicComponent(
        title = itemText,
        modifier = Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        ),
        insideMargin = PaddingValues(horizontal = 28.dp, vertical = 12.dp)
    )
}
