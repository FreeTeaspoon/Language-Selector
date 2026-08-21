package vegabobo.languageselector.ui.screen.main

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import vegabobo.languageselector.R

@Composable
fun AppListPermissionWarning(
    show: Boolean,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val backState = rememberNavigationEventState(NavigationEventInfo.None)
    NavigationBackHandler(
        state = backState,
        isBackEnabled = show,
        onBackCompleted = onDismiss
    )

    OverlayDialog(
        show = show,
        onDismissRequest = onDismiss,
        title = stringResource(R.string.app_list_permission_required)
    ) {
        Text(
            text = stringResource(R.string.app_list_permission_message),
            color = MiuixTheme.colorScheme.onSurface
        )
        TextButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            text = stringResource(R.string.open_settings),
            onClick = onOpenSettings
        )
        TextButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            text = stringResource(R.string.retry),
            colors = ButtonDefaults.textButtonColorsPrimary(),
            onClick = onRetry
        )
    }
}
