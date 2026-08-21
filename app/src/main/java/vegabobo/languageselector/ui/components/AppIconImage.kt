package vegabobo.languageselector.ui.components

import android.content.pm.ApplicationInfo
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import vegabobo.languageselector.data.icons.AppIconCache
import top.yukonga.miuix.kmp.theme.MiuixTheme

private data class IconKey(
    val packageName: String,
    val uid: Int,
    val sourceDir: String?,
    val sizePx: Int
)

@Composable
fun AppIconImage(
    modifier: Modifier = Modifier,
    applicationInfo: ApplicationInfo,
    contentDescription: String? = null,
    size: Dp = 40.dp
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val sizePx = with(density) { size.roundToPx() }
    val iconKey = IconKey(
        packageName = applicationInfo.packageName,
        uid = applicationInfo.uid,
        sourceDir = applicationInfo.sourceDir,
        sizePx = sizePx
    )
    val cachedBitmap = remember(iconKey) {
        AppIconCache.getFromCache(applicationInfo, sizePx)
    }
    var appBitmap by remember(iconKey) { mutableStateOf(cachedBitmap) }

    LaunchedEffect(iconKey) {
        if (appBitmap == null) {
            appBitmap = AppIconCache.loadIcon(context, applicationInfo, sizePx)
        }
    }

    Box(modifier = modifier) {
        Crossfade(
            targetState = appBitmap,
            animationSpec = tween(durationMillis = 150),
            label = "AppIconFade"
        ) { icon ->
            if (icon == null) {
                AppIconPlaceholder(Modifier.fillMaxSize())
            } else {
                Image(
                    bitmap = icon.asImageBitmap(),
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun AppIconPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MiuixTheme.colorScheme.secondaryContainer)
    )
}
