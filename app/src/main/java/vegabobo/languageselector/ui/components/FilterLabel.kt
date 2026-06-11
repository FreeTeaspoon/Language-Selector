package vegabobo.languageselector.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

@Composable
fun FilterLabel(
    title: String,
    onClick: (Boolean) -> Unit,
    isSelected: Boolean
) {
    val background =
        if (isSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.surfaceContainerHigh
    val content =
        if (isSelected) MiuixTheme.colorScheme.onPrimary else MiuixTheme.colorScheme.onSurface

    Card(
        cornerRadius = 18.dp,
        insideMargin = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        colors = CardDefaults.defaultColors(color = background),
        pressFeedbackType = PressFeedbackType.Sink,
        showIndication = true,
        onClick = { onClick(isSelected) }
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Icon(
                    imageVector = MiuixIcons.Regular.Ok,
                    contentDescription = null,
                    tint = content,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(text = title, style = MiuixTheme.textStyles.body2, color = content)
        }
    }
}
