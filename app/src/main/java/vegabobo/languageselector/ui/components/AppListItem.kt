package vegabobo.languageselector.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import vegabobo.languageselector.domain.apps.AppInfo

@Composable
fun AppListItem(
    modifier: Modifier = Modifier,
    app: AppInfo,
    onClickApp: (String) -> Unit
) {
    BasicComponent(
        modifier = modifier,
        startAction = {
            AppIconImage(
                modifier = Modifier.size(44.dp),
                applicationInfo = app.applicationInfo,
                label = app.name
            )
        },
        endActions = {
            Column(horizontalAlignment = Alignment.End) {
                if (app.isModified()) {
                    Text(
                        text = "Modified",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.primary
                    )
                }
                if (app.isSystemApp()) {
                    Text(
                        text = "System",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceSecondary
                    )
                }
            }
        },
        holdDownState = true,
        onClick = { onClickApp(app.pkg) }
    ) {
        Text(
            text = app.name,
            style = MiuixTheme.textStyles.headline1,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = app.pkg,
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurfaceSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
