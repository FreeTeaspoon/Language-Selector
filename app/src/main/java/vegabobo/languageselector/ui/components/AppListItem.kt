package vegabobo.languageselector.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.ArrowRight
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import vegabobo.languageselector.R
import vegabobo.languageselector.domain.apps.AppInfo

@Composable
fun AppListItem(
    app: AppInfo,
    onClickApp: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(horizontal = 12.dp)
            .padding(bottom = 12.dp),
        insideMargin = PaddingValues(start = 10.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        onClick = { onClickApp(app.pkg) },
        pressFeedbackType = PressFeedbackType.Sink,
        showIndication = true
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIconImage(
                applicationInfo = app.applicationInfo,
                label = app.name,
                size = 48.dp,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.size(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.name,
                    modifier = Modifier.basicMarquee(),
                    fontWeight = FontWeight(550),
                    color = MiuixTheme.colorScheme.onSurface,
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = app.pkg,
                    modifier = Modifier.basicMarquee(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight(550),
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                    softWrap = false
                )
            }
            if (app.isModified()) {
                ModifiedStatusTag(modifier = Modifier.padding(start = 16.dp))
            }
            val layoutDirection = LocalLayoutDirection.current
            Image(
                imageVector = MiuixIcons.Basic.ArrowRight,
                contentDescription = null,
                colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurfaceVariantActions),
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = if (layoutDirection == LayoutDirection.Rtl) -1f else 1f
                    }
                    .padding(start = 8.dp)
                    .size(width = 10.dp, height = 16.dp)
            )
        }
    }
}

@Composable
fun ModifiedStatusTag(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(
            color = MiuixTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
            shape = RoundedCornerShape(6.dp)
        )
    ) {
        Text(
            text = stringResource(R.string.modified_tag),
            color = MiuixTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
            fontSize = 9.sp,
            fontWeight = FontWeight(750),
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}
