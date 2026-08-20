package vegabobo.languageselector.ui.screen.main

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import vegabobo.languageselector.R

@Composable
fun ShizukuRequiredWarning(
    onClickContinue: () -> Unit
) {
    OverlayDialog(
        show = true,
        onDismissRequest = {},
        title = stringResource(id = R.string.permissions_required)
    ) {
        Text(
            text = stringResource(id = R.string.shizuku_required),
            color = MiuixTheme.colorScheme.onSurface
        )
        TextButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            text = stringResource(id = R.string.proceed),
            colors = ButtonDefaults.textButtonColorsPrimary(),
            onClick = { onClickContinue() }
        )
    }
}
