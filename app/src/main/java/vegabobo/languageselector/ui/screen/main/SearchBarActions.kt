package vegabobo.languageselector.ui.screen.main

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.window.WindowListPopup
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
    val systemAppsText = if (isShowingSystemApps) {
        stringResource(R.string.show_only_user_apps)
    } else {
        stringResource(R.string.show_system_apps)
    }
    val aboutText = stringResource(R.string.about)

    Box {
        IconButton(onClick = onToggleDropdown) {
            Icon(
                imageVector = MiuixIcons.Regular.More,
                contentDescription = stringResource(R.string.more_options)
            )
        }
        WindowListPopup(
            show = isDropdownVisible,
            alignment = PopupPositionProvider.Align.BottomEnd,
            enableWindowDim = false,
            onDismissRequest = onClickToggleDropdown
        ) {
            ListPopupColumn {
                listOf(systemAppsText, aboutText).forEachIndexed { index, text ->
                    DropdownImpl(
                        text = text,
                        optionSize = 2,
                        isSelected = false,
                        index = index,
                        onSelectedIndexChange = {
                            if (index == 0) onClickToggleSystemApps() else onClickAbout()
                            onClickToggleDropdown()
                        }
                    )
                }
            }
        }
    }
}
