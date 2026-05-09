package vegabobo.languageselector.ui.components

import androidx.compose.runtime.Composable
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back

@Composable
fun BackButton(
    onClick: () -> Unit
){
    IconButton(onClick = { onClick() }) {
        Icon(
            imageVector = MiuixIcons.Regular.Back,
            contentDescription = "Back arrow"
        )
    }
}
