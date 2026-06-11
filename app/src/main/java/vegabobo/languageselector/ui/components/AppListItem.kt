package vegabobo.languageselector.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import vegabobo.languageselector.domain.apps.AppInfo

@Composable
fun AppListItem(
    modifier: Modifier = Modifier,
    app: AppInfo,
    onClickApp: (String) -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.defaultColors(
            color = MiuixTheme.colorScheme.surfaceContainer
        ),
        pressFeedbackType = PressFeedbackType.Sink,
        showIndication = true,
        onClick = { onClickApp(app.pkg) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIconImage(
                modifier = Modifier.size(36.dp),
                applicationInfo = app.applicationInfo,
                label = app.name
            )
            Spacer(modifier = Modifier.padding(8.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy((-4).dp)
            ) {
                Text(
                    text = app.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = app.pkg,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row {
                    TextLabel(text = if (app.isSystemApp()) "System App" else "User App")
                    if (app.isModified()) {
                        TextLabel(text = "Modified")
                    }
                }
            }
        }
    }
}

@Composable
fun TextLabel(text: String) {
    Box(Modifier.padding(top = 2.dp, end = 4.dp, bottom = 4.dp)) {
        Box(
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MiuixTheme.colorScheme.secondaryVariant)
        ) {
            Text(
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
                text = text,
                color = MiuixTheme.colorScheme.onSecondaryVariant,
                maxLines = 1,
                lineHeight = 16.sp,
                fontSize = 10.sp
            )
        }
    }
}
