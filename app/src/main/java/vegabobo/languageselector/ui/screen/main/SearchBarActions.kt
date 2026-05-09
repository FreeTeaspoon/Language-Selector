package vegabobo.languageselector.ui.screen.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.theme.MiuixTheme
import vegabobo.languageselector.R

@Composable
fun SearchBarActions(
    isDropdownVisible: Boolean = false,
    isShowingSystemApps: Boolean = false,
    onClickToggleDropdown: () -> Unit,
    onToggleDropdown: () -> Unit,
    onClickToggleSystemApps: () -> Unit,
    onClickAbout: () -> Unit
) {
    Box(
        modifier = Modifier.wrapContentSize(Alignment.Center)
    ) {
        ToolbarNormal(
            onToggleDropdown = { onToggleDropdown() }
        )

        if (isDropdownVisible) {
            Popup(
                alignment = Alignment.TopEnd,
                onDismissRequest = { onClickToggleDropdown() },
                properties = PopupProperties(focusable = true)
            ) {
                Card(
                    modifier = Modifier
                        .padding(top = 46.dp, end = 4.dp)
                        .width(238.dp),
                    colors = CardDefaults.defaultColors(
                        color = MiuixTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        PopupItem(
                            text = if (isShowingSystemApps)
                                stringResource(R.string.show_only_user_apps)
                            else
                                stringResource(R.string.show_system_apps),
                            onClick = {
                                onClickToggleSystemApps()
                                onClickToggleDropdown()
                            }
                        )
                        PopupItem(
                            text = stringResource(R.string.about),
                            onClick = {
                                onClickAbout()
                                onClickToggleDropdown()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ToolbarNormal(
    onToggleDropdown: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onToggleDropdown() }) {
            Icon(
                imageVector = MiuixIcons.Regular.More,
                contentDescription = "More icon"
            )
        }
    }
}

@Composable
private fun PopupItem(
    text: String,
    onClick: () -> Unit
) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        color = MiuixTheme.colorScheme.onSurface
    )
}
