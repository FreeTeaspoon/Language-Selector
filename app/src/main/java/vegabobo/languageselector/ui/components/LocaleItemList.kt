package vegabobo.languageselector.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.ArrowRight
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

@Composable
fun LocaleItemList(
    itemText: String,
    onLongClick: (() -> Unit)? = null,
    showArrow: Boolean = false,
    onClick: () -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp),
        insideMargin = PaddingValues(0.dp),
        pressFeedbackType = PressFeedbackType.Sink,
        showIndication = true,
        onClick = onClick,
        onLongPress = onLongClick
    ) {
        BasicComponent(
            title = itemText,
            insideMargin = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            endActions = if (showArrow) {
                {
                    Image(
                        imageVector = MiuixIcons.Basic.ArrowRight,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurfaceVariantActions),
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = if (layoutDirection == LayoutDirection.Rtl) -1f else 1f
                            }
                            .size(width = 10.dp, height = 16.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
            } else {
                null
            }
        )
    }
}

@Composable
fun LocaleChildItem(
    itemText: String,
    selected: Boolean = false,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .padding(start = 12.dp)
                .width(6.dp)
                .height(24.dp)
                .align(Alignment.CenterVertically)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (selected) MiuixTheme.colorScheme.primary
                    else MiuixTheme.colorScheme.primaryContainer
                )
        )
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(start = 6.dp, end = 12.dp, bottom = 6.dp),
            insideMargin = PaddingValues(0.dp),
            pressFeedbackType = PressFeedbackType.Sink,
            showIndication = true,
            onClick = onClick,
            onLongPress = onLongClick
        ) {
            BasicComponent(
                title = itemText,
                insideMargin = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
            )
        }
    }
}
