package vegabobo.languageselector.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.shader.isRenderEffectSupported
import top.yukonga.miuix.kmp.theme.MiuixTheme
import vegabobo.languageselector.ui.components.hyperos.BgEffectConfig
import vegabobo.languageselector.ui.components.hyperos.BgEffectPainter
import vegabobo.languageselector.ui.components.hyperos.DeviceType
import vegabobo.languageselector.ui.components.hyperos.bgEffectDraw

@Composable
fun HyperOsAboutBackground(
    scrollProgress: Float,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val surface = MiuixTheme.colorScheme.surface
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(surface)
    ) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            isRuntimeShaderSupported() &&
            isRenderEffectSupported()
        ) {
            Os3ShaderBackground(scrollProgress = scrollProgress)
        }
        content()
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun Os3ShaderBackground(scrollProgress: Float) {
    val isDark = isSystemInDarkTheme()
    val surface = MiuixTheme.colorScheme.surface
    val painter = remember { BgEffectPainter(isOs3 = true) }
    val preset = remember(isDark) {
        BgEffectConfig.get(DeviceType.PHONE, isDark = isDark, isOs3 = true)
    }
    val transition = rememberInfiniteTransition(label = "AboutOs3ColorStage")
    val colorStage by transition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (preset.colorInterpPeriod * 1000).toInt(),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "AboutOs3ColorStage"
    )
    Spacer(
        modifier = Modifier
            .fillMaxSize()
            .bgEffectDraw(
                painter = painter,
                preset = preset,
                deviceType = DeviceType.PHONE,
                isDarkTheme = isDark,
                surface = surface,
                effectBackground = true,
                isFullSize = false,
                playing = true,
                colorStage = { colorStage },
                alpha = { (1f - scrollProgress).coerceIn(0f, 1f) }
            )
    )
}
