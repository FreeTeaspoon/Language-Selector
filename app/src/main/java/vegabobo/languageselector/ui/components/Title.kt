package vegabobo.languageselector.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import top.yukonga.miuix.kmp.basic.SmallTitle

@Composable
fun Title(title: String, modifier: Modifier = Modifier) {
    SmallTitle(text = title, modifier = modifier)
}
