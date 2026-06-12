package vegabobo.languageselector.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.PopupPositionProvider

object AppPopupDefaults {
    val MenuPositionProvider: PopupPositionProvider = object : PopupPositionProvider {
        override fun calculatePosition(
            anchorBounds: IntRect,
            windowBounds: IntRect,
            layoutDirection: LayoutDirection,
            popupContentSize: IntSize,
            popupMargin: IntRect,
            alignment: PopupPositionProvider.Align
        ): IntOffset {
            val resolved = alignment.resolve(layoutDirection)
            val x = when (resolved) {
                PopupPositionProvider.Align.TopEnd,
                PopupPositionProvider.Align.BottomEnd,
                PopupPositionProvider.Align.End -> anchorBounds.right - popupContentSize.width - popupMargin.right

                else -> anchorBounds.left + popupMargin.left
            }
            val y = when (resolved) {
                PopupPositionProvider.Align.BottomStart,
                PopupPositionProvider.Align.BottomEnd -> anchorBounds.top - popupContentSize.height - popupMargin.bottom

                PopupPositionProvider.Align.TopStart,
                PopupPositionProvider.Align.TopEnd -> anchorBounds.bottom + popupMargin.top

                else -> {
                    val below = anchorBounds.bottom + popupMargin.top
                    val above = anchorBounds.top - popupContentSize.height - popupMargin.bottom
                    when {
                        below + popupContentSize.height <= windowBounds.bottom -> below
                        above >= windowBounds.top -> above
                        else -> anchorBounds.top + anchorBounds.height / 2 - popupContentSize.height / 2
                    }
                }
            }

            val minX = windowBounds.left + popupMargin.left
            val maxX = (windowBounds.right - popupContentSize.width - popupMargin.right).coerceAtLeast(minX)
            val minY = windowBounds.top + popupMargin.top
            val maxY = (windowBounds.bottom - popupContentSize.height - popupMargin.bottom).coerceAtLeast(minY)
            return IntOffset(
                x = x.coerceIn(minX, maxX),
                y = y.coerceIn(minY, maxY)
            )
        }

        override fun getMargins(): PaddingValues = PaddingValues(start = 20.dp, end = 20.dp)
    }
}

private fun PopupPositionProvider.Align.resolve(
    layoutDirection: LayoutDirection
): PopupPositionProvider.Align {
    if (layoutDirection == LayoutDirection.Ltr) return this
    return when (this) {
        PopupPositionProvider.Align.Start -> PopupPositionProvider.Align.End
        PopupPositionProvider.Align.End -> PopupPositionProvider.Align.Start
        PopupPositionProvider.Align.TopStart -> PopupPositionProvider.Align.TopEnd
        PopupPositionProvider.Align.TopEnd -> PopupPositionProvider.Align.TopStart
        PopupPositionProvider.Align.BottomStart -> PopupPositionProvider.Align.BottomEnd
        PopupPositionProvider.Align.BottomEnd -> PopupPositionProvider.Align.BottomStart
    }
}
