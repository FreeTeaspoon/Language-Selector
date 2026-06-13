package vegabobo.languageselector.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import top.yukonga.miuix.kmp.shader.isRenderEffectSupported
import top.yukonga.miuix.kmp.shader.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HyperOsAboutBackground(
    scrollProgress: Float,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val surface = MiuixTheme.colorScheme.surface
    val isDark = isSystemInDarkTheme()
    val supportsEffect = remember { isRuntimeShaderSupported() && isRenderEffectSupported() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(surface)
    ) {
        if (supportsEffect) {
            val transition = rememberInfiniteTransition(label = "AboutOs3Background")
            val phase by transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 9000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "AboutOs3BackgroundPhase"
            )
            val alpha = (1f - scrollProgress).coerceIn(0f, 1f)
            val colors = if (isDark) {
                AboutGradientColors(
                    primary = Color(0xFF221A67),
                    secondary = Color(0xFF003E73),
                    tertiary = Color(0xFF57235F),
                    veil = surface.copy(alpha = 0.46f)
                )
            } else {
                AboutGradientColors(
                    primary = Color(0xFFFFDCE8),
                    secondary = Color(0xFFDCE7FF),
                    tertiary = Color(0xFFE8E0FF),
                    veil = surface.copy(alpha = 0.34f)
                )
            }
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer { this.alpha = alpha }
            ) {
                drawRect(surface)
                val t = phase * (PI.toFloat() * 2f)
                val width = size.width
                val height = size.height
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(colors.primary, Color.Transparent),
                        center = Offset(
                            x = width * (0.26f + 0.08f * cos(t)),
                            y = height * (0.20f + 0.06f * sin(t))
                        ),
                        radius = width * 0.76f
                    ),
                    radius = width * 0.76f,
                    center = Offset(width * (0.26f + 0.08f * cos(t)), height * (0.20f + 0.06f * sin(t)))
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(colors.secondary, Color.Transparent),
                        center = Offset(
                            x = width * (0.78f + 0.06f * sin(t * 0.8f)),
                            y = height * (0.18f + 0.05f * cos(t * 0.8f))
                        ),
                        radius = width * 0.7f
                    ),
                    radius = width * 0.7f,
                    center = Offset(width * (0.78f + 0.06f * sin(t * 0.8f)), height * (0.18f + 0.05f * cos(t * 0.8f)))
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(colors.tertiary, Color.Transparent),
                        center = Offset(
                            x = width * (0.52f + 0.05f * cos(t * 1.2f)),
                            y = height * (0.42f + 0.05f * sin(t * 1.2f))
                        ),
                        radius = width * 0.82f
                    ),
                    radius = width * 0.82f,
                    center = Offset(width * (0.52f + 0.05f * cos(t * 1.2f)), height * (0.42f + 0.05f * sin(t * 1.2f)))
                )
                drawRect(colors.veil)
            }
        }
        content()
    }
}

private data class AboutGradientColors(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val veil: Color
)
