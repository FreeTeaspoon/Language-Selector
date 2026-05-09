package vegabobo.languageselector.ui.screen.main

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog
import vegabobo.languageselector.R

@Composable
fun SystemDialogWarn(
    onClickContinue: () -> Unit,
    onClickCancel: () -> Unit,
) {
    WindowDialog(
        show = true,
        onDismissRequest = { onClickCancel() },
        title = stringResource(R.string.warning)
    ) {
        Text(
            text = stringResource(R.string.warning_system_apps),
            color = MiuixTheme.colorScheme.onSurface
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
        ) {
            TextButton(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.cancel),
                onClick = { onClickCancel() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.proceed),
                colors = ButtonDefaults.textButtonColorsPrimary(),
                onClick = { onClickContinue() }
            )
        }
    }
}
