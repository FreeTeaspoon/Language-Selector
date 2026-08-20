package vegabobo.languageselector.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme
import vegabobo.languageselector.R

@Composable
fun BackButton(
    onClick: () -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current
    IconButton(onClick = onClick) {
        Icon(
            imageVector = MiuixIcons.Regular.Back,
            contentDescription = stringResource(R.string.action_back),
            tint = MiuixTheme.colorScheme.onSurface,
            modifier = Modifier.graphicsLayer {
                scaleX = if (layoutDirection == LayoutDirection.Rtl) -1f else 1f
            }
        )
    }
}
