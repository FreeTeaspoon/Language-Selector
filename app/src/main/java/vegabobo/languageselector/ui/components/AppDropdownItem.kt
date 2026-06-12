package vegabobo.languageselector.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.DropdownColors
import top.yukonga.miuix.kmp.basic.DropdownDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun AppDropdownItem(
    text: String,
    optionSize: Int,
    index: Int,
    modifier: Modifier = Modifier,
    dropdownColors: DropdownColors = DropdownDefaults.dropdownColors(),
    onClick: () -> Unit
) {
    val currentOnClick = rememberUpdatedState(onClick)
    val topPadding = if (index == 0) 20.dp else 12.dp
    val bottomPadding = if (index == optionSize - 1) 20.dp else 12.dp

    Row(
        modifier = modifier
            .clickable { currentOnClick.value() }
            .background(dropdownColors.containerColor)
            .padding(horizontal = 20.dp)
            .padding(top = topPadding, bottom = bottomPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            fontSize = MiuixTheme.textStyles.body1.fontSize,
            fontWeight = FontWeight.Medium,
            color = dropdownColors.contentColor
        )
    }
}
