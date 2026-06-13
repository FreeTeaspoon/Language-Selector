package vegabobo.languageselector.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import vegabobo.languageselector.domain.apps.AppListLogic
import vegabobo.languageselector.domain.apps.AppRowTag

@Composable
fun AppListItem(
    app: AppInfo,
    onClickApp: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tags = AppListLogic.rowTags(app)
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
            if (tags.isNotEmpty()) {
                Column(
                    modifier = Modifier.padding(start = 16.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tags.forEach { tag ->
                        when (tag) {
                            AppRowTag.Modified -> ModifiedStatusTag()
                            AppRowTag.System -> SystemStatusTag()
                        }
                    }
                }
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
    AppStatusTag(
        label = stringResource(R.string.modified_tag),
        backgroundColor = MiuixTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
        contentColor = MiuixTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
        modifier = modifier
    )
}

@Composable
fun SystemStatusTag(modifier: Modifier = Modifier) {
    AppStatusTag(
        label = stringResource(R.string.system_tag),
        backgroundColor = MiuixTheme.colorScheme.primary.copy(alpha = 0.8f),
        contentColor = MiuixTheme.colorScheme.onPrimary,
        modifier = modifier
    )
}
