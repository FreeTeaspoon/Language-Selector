package vegabobo.languageselector.ui.screen.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurBlendMode
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.shader.isRenderEffectSupported
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import vegabobo.languageselector.BuildConfig
import vegabobo.languageselector.R
import vegabobo.languageselector.ui.components.BackButton
import vegabobo.languageselector.ui.components.BlurredTopBar
import vegabobo.languageselector.ui.components.HyperOsAboutBackground
import vegabobo.languageselector.ui.components.horizontalCutoutPadding
import vegabobo.languageselector.ui.components.rememberAppBlurBackdrop

data class AboutDependency(
    val name: String,
    val summary: String,
    val url: String?
)

data class AboutUiState(
    val title: String,
    val appName: String,
    val versionName: String,
    val versionCode: Int,
    val sourceUrl: String,
    val dependencies: List<AboutDependency>
)

data class AboutScreenActions(
    val onBack: () -> Unit,
    val onOpenUrl: (String) -> Unit
)

@Composable
fun AboutScreen(navigateBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val state = AboutUiState(
        title = stringResource(R.string.about),
        appName = stringResource(R.string.app_name),
        versionName = BuildConfig.VERSION_NAME,
        versionCode = BuildConfig.VERSION_CODE,
        sourceUrl = "https://github.com/FreeTeaspoon/Language-Selector",
        dependencies = remember { staticDependencies() }
    )

    AboutScreenContent(
        state = state,
        actions = AboutScreenActions(
            onBack = navigateBack,
            onOpenUrl = { url -> runCatching { uriHandler.openUri(url) } }
        )
    )
}

@Composable
private fun AboutScreenContent(
    state: AboutUiState,
    actions: AboutScreenActions
) {
    val scrollBehavior = MiuixScrollBehavior()
    val lazyListState = rememberLazyListState()
    val scrollProgressState = remember {
        derivedStateOf {
            when {
                lazyListState.firstVisibleItemIndex > 0 -> 1f
                else -> {
                    val spacer = lazyListState.layoutInfo.visibleItemsInfo
                        .firstOrNull { it.key == "logoSpacer" }
                    if (spacer != null && spacer.size > 0) {
                        (lazyListState.firstVisibleItemScrollOffset.toFloat() / spacer.size)
                            .coerceIn(0f, 1f)
                    } else {
                        0f
                    }
                }
            }
        }
    }
    val heroCollapsed by remember { derivedStateOf { scrollProgressState.value == 1f } }
    val titleAlpha by remember {
        derivedStateOf { ((scrollProgressState.value - 0.35f) / 0.65f).coerceIn(0f, 1f) }
    }
    val barBackdrop = rememberAppBlurBackdrop()
    val barColor = if (barBackdrop != null) {
        Color.Transparent
    } else if (heroCollapsed) {
        MiuixTheme.colorScheme.surface
    } else {
        Color.Transparent
    }

    Scaffold(
        topBar = {
            BlurredTopBar(
                backdrop = barBackdrop,
                active = barBackdrop != null,
                progressive = true,
                scrollBehavior = scrollBehavior
            ) {
                SmallTopAppBar(
                    modifier = Modifier.horizontalCutoutPadding(),
                    title = state.title,
                    color = barColor,
                    titleColor = MiuixTheme.colorScheme.onSurface.copy(alpha = titleAlpha),
                    navigationIcon = { BackButton(actions.onBack) },
                    scrollBehavior = scrollBehavior,
                    defaultWindowInsetsPadding = false
                )
            }
        },
        contentWindowInsets = WindowInsets.systemBars
            .add(WindowInsets.displayCutout)
            .only(WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        Box(modifier = if (barBackdrop != null) Modifier.layerBackdrop(barBackdrop) else Modifier) {
            AboutContent(
                state = state,
                actions = actions,
                innerPadding = innerPadding,
                scrollBehavior = scrollBehavior,
                lazyListState = lazyListState,
                scrollProgress = { scrollProgressState.value }
            )
        }
    }
}

@Composable
private fun AboutContent(
    state: AboutUiState,
    actions: AboutScreenActions,
    innerPadding: PaddingValues,
    scrollBehavior: ScrollBehavior,
    lazyListState: LazyListState,
    scrollProgress: () -> Float
) {
    val layoutDirection = LocalLayoutDirection.current
    val density = LocalDensity.current
    val contentBackdrop = rememberLayerBackdrop()
    val isDark = isSystemInDarkTheme()
    val blurEnabled = isRuntimeShaderSupported() && isRenderEffectSupported()
    val cardBlendColors = remember(isDark) {
        if (isDark) {
            listOf(
                BlendColorEntry(Color(0x4DA9A9A9), BlurBlendMode.Luminosity),
                BlendColorEntry(Color(0x1A9C9C9C), BlurBlendMode.PlusDarker)
            )
        } else {
            listOf(
                BlendColorEntry(Color(0x340034F9), BlurBlendMode.Overlay),
                BlendColorEntry(Color(0xB3FFFFFF.toInt()), BlurBlendMode.HardLight)
            )
        }
    }
    val logoBlendColors = remember(isDark) {
        if (isDark) {
            listOf(
                BlendColorEntry(Color(0xE6A1A1A1.toInt()), BlurBlendMode.ColorDodge),
                BlendColorEntry(Color(0x4DE6E6E6), BlurBlendMode.LinearLight),
                BlendColorEntry(Color(0xFF1AF500.toInt()), BlurBlendMode.Lab)
            )
        } else {
            listOf(
                BlendColorEntry(Color(0xCC4A4A4A.toInt()), BlurBlendMode.ColorBurn),
                BlendColorEntry(Color(0xFF4F4F4F.toInt()), BlurBlendMode.LinearLight),
                BlendColorEntry(Color(0xFF1AF200.toInt()), BlurBlendMode.Lab)
            )
        }
    }
    var logoHeight by remember { mutableStateOf(300.dp) }
    val versionProgress = { ((scrollProgress() - 0.05f) / 0.15f).coerceIn(0f, 1f) }
    val projectNameProgress = { ((scrollProgress() - 0.20f) / 0.15f).coerceIn(0f, 1f) }
    val iconProgress = { ((scrollProgress() - 0.35f) / 0.15f).coerceIn(0f, 1f) }
    val scrollPadding = PaddingValues(
        top = innerPadding.calculateTopPadding(),
        start = innerPadding.calculateStartPadding(layoutDirection),
        end = innerPadding.calculateEndPadding(layoutDirection)
    )
    val logoPadding = PaddingValues(
        top = innerPadding.calculateTopPadding() + 40.dp,
        start = innerPadding.calculateStartPadding(layoutDirection),
        end = innerPadding.calculateEndPadding(layoutDirection)
    )
    val projects = remember(state.appName, state.sourceUrl, state.dependencies) {
        listOf(
            AboutDependency(
                name = state.appName,
                summary = state.sourceUrl.removePrefix("https://"),
                url = state.sourceUrl
            )
        ) + state.dependencies
    }

    HyperOsAboutBackground(
        modifier = Modifier.fillMaxSize(),
        backdropModifier = Modifier.layerBackdrop(contentBackdrop),
        dynamicBackground = blurEnabled,
        alpha = { 1f - scrollProgress() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = logoPadding.calculateTopPadding() + 52.dp,
                    start = logoPadding.calculateStartPadding(layoutDirection),
                    end = logoPadding.calculateEndPadding(layoutDirection)
                )
                .onSizeChanged { size ->
                    with(density) { logoHeight = size.height.toDp() }
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .clipToBounds()
                    .graphicsLayer {
                        val progress = iconProgress()
                        alpha = 1f - progress
                        scaleX = 1f - progress * 0.05f
                        scaleY = 1f - progress * 0.05f
                    }
            ) {
                Image(
                    modifier = Modifier
                        .requiredSize(100.dp)
                        .then(
                            if (blurEnabled) {
                                Modifier.textureBlur(
                                    backdrop = contentBackdrop,
                                    shape = RoundedCornerShape(0.dp),
                                    blurRadius = 150f,
                                    colors = BlurColors(blendColors = logoBlendColors),
                                    contentBlendMode = BlendMode.DstIn,
                                    enabled = true
                                )
                            } else {
                                Modifier
                            }
                        ),
                    painter = painterResource(R.drawable.about_logo),
                    colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onBackground),
                    contentDescription = state.appName
                )
            }
            Text(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 5.dp)
                    .graphicsLayer {
                        val progress = projectNameProgress()
                        alpha = 1f - progress
                        scaleX = 1f - progress * 0.05f
                        scaleY = 1f - progress * 0.05f
                    }
                    .then(
                        if (blurEnabled) {
                            Modifier.textureBlur(
                                backdrop = contentBackdrop,
                                shape = RoundedCornerShape(16.dp),
                                blurRadius = 150f,
                                colors = BlurColors(blendColors = logoBlendColors),
                                contentBlendMode = BlendMode.DstIn,
                                enabled = true
                            )
                        } else {
                            Modifier
                        }
                    ),
                text = state.appName,
                color = MiuixTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 35.sp
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        val progress = versionProgress()
                        alpha = 1f - progress
                        scaleX = 1f - progress * 0.05f
                        scaleY = 1f - progress * 0.05f
                    },
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                text = "v${state.versionName} (${state.versionCode})",
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .scrollEndHaptic()
                .overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(
                top = scrollPadding.calculateTopPadding(),
                start = scrollPadding.calculateStartPadding(layoutDirection),
                end = scrollPadding.calculateEndPadding(layoutDirection)
            ),
            overscrollEffect = null
        ) {
            item(key = "logoSpacer") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            logoHeight + 52.dp + logoPadding.calculateTopPadding() -
                                scrollPadding.calculateTopPadding() + 126.dp
                        ),
                    contentAlignment = Alignment.TopCenter,
                    content = {}
                )
            }
            item(key = "about") {
                Box {
                    Spacer(Modifier.fillParentMaxHeight())
                    Column(modifier = Modifier.padding(bottom = 12.dp)) {
                        SmallTitle(text = stringResource(R.string.about_open_source))
                        AboutCard(
                            blurEnabled = blurEnabled,
                            backdrop = contentBackdrop,
                            blendColors = cardBlendColors
                        ) {
                            projects.forEach { project ->
                                ArrowPreference(
                                    title = project.name,
                                    summary = project.summary,
                                    enabled = project.url != null,
                                    onClick = { project.url?.let(actions.onOpenUrl) }
                                )
                            }
                        }
                        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutCard(
    blurEnabled: Boolean,
    backdrop: LayerBackdrop,
    blendColors: List<BlendColorEntry>,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(bottom = 12.dp)
            .then(
                if (blurEnabled) {
                    Modifier.textureBlur(
                        backdrop = backdrop,
                        shape = RoundedCornerShape(16.dp),
                        blurRadius = 60f,
                        colors = BlurColors(blendColors = blendColors),
                        enabled = true
                    )
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.defaultColors(
            color = if (blurEnabled) Color.Transparent else MiuixTheme.colorScheme.surfaceContainer,
            contentColor = Color.Transparent
        ),
        content = content
    )
}

fun staticDependencies(): List<AboutDependency> = listOf(
    AboutDependency(
        name = "Miuix",
        summary = "github.com/compose-miuix-ui/miuix",
        url = "https://github.com/compose-miuix-ui/miuix"
    ),
    AboutDependency(
        name = "Shizuku API",
        summary = "github.com/RikkaApps/Shizuku-API",
        url = "https://github.com/RikkaApps/Shizuku-API"
    ),
    AboutDependency(
        name = "libsu",
        summary = "github.com/topjohnwu/libsu",
        url = "https://github.com/topjohnwu/libsu"
    ),
    AboutDependency(
        name = "AboutLibraries",
        summary = "github.com/mikepenz/AboutLibraries",
        url = "https://github.com/mikepenz/AboutLibraries"
    ),
    AboutDependency(
        name = "AndroidX",
        summary = "github.com/androidx/androidx",
        url = "https://github.com/androidx/androidx"
    ),
    AboutDependency(
        name = "Kotlin",
        summary = "github.com/JetBrains/kotlin",
        url = "https://github.com/JetBrains/kotlin"
    )
)
