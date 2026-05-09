package vegabobo.languageselector.ui.screen.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
fun ShizukuRequiredWarning(
    onClickContinue: () -> Unit
) {
    WindowDialog(
        show = true,
        onDismissRequest = {},
        title = stringResource(id = R.string.permissions_required)
    ) {
        Column {
            Text(
                text = stringResource(id = R.string.shizuku_required),
                color = MiuixTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(20.dp))
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.proceed),
                colors = ButtonDefaults.textButtonColorsPrimary(),
                onClick = { onClickContinue() }
            )
        }
    }
}
