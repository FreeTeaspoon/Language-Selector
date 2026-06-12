package vegabobo.languageselector.ui.screen.about

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.util.withContext
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurBlendMode
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
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
import vegabobo.languageselector.ui.components.rememberAppBlurBackdrop

data class AboutDependency(
    val name: String,
    val summary: String,
    val url: String?
)

data class AboutUiState(
    val appName: String,
    val versionText: String,
    val sourceUrl: String,
    val dependencies: List<AboutDependency>
)

data class AboutScreenActions(
    val onBack: () -> Unit,
    val onOpenUrl: (String) -> Unit
)

@Composable
fun AboutScreen(navigateBack: () -> Unit) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val appName = stringResource(R.string.app_name)
    val state = AboutUiState(
        appName = appName,
        versionText = stringResource(R.string.version).format(
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE
        ),
        sourceUrl = "https://github.com/FreeTeaspoon/Language-Selector",
        dependencies = remember(context) { loadAboutDependencies(context) }
    )
    val appIcon = remember(context) {
        context.packageManager.getApplicationIcon(context.applicationInfo)
            .toBitmap()
            .asImageBitmap()
    }
    AboutScreenContent(
        state = state,
        appIcon = appIcon,
        actions = AboutScreenActions(
            onBack = navigateBack,
            onOpenUrl = { url -> runCatching { uriHandler.openUri(url) } }
        )
    )
}

@Composable
private fun AboutScreenContent(
    state: AboutUiState,
    appIcon: androidx.compose.ui.graphics.ImageBitmap,
    actions: AboutScreenActions
) {
    val scrollBehavior = MiuixScrollBehavior()
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val collapseDistancePx = with(density) { 220.dp.toPx() }
    val scrollProgress by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) {
                1f
            } else {
                (listState.firstVisibleItemScrollOffset / collapseDistancePx).coerceIn(0f, 1f)
            }
        }
    }
    val barBackdrop = rememberAppBlurBackdrop()
    val blurActive = barBackdrop != null && scrollProgress > 0.7f
    val barColor = when {
        blurActive -> Color.Transparent
        scrollProgress > 0.7f -> MiuixTheme.colorScheme.surface
        else -> Color.Transparent
    }
    val titleAlpha = ((scrollProgress - 0.35f) / 0.65f).coerceIn(0f, 1f)
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
        WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()

    Scaffold(
        topBar = {
            BlurredTopBar(backdrop = barBackdrop, active = blurActive) {
                SmallTopAppBar(
                    title = state.appName,
                    color = barColor,
                    titleColor = MiuixTheme.colorScheme.onSurface.copy(alpha = titleAlpha),
                    navigationIcon = { BackButton(actions.onBack) },
                    scrollBehavior = scrollBehavior,
                    defaultWindowInsetsPadding = false
                )
            }
        },
        popupHost = {},
        contentWindowInsets = WindowInsets.systemBars
            .add(WindowInsets.displayCutout)
            .only(WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        Box(modifier = if (barBackdrop != null) Modifier.layerBackdrop(barBackdrop) else Modifier) {
            AboutContent(
                state = state,
                appIcon = appIcon,
                actions = actions,
                contentPadding = innerPadding,
                bottomPadding = bottomInset + 12.dp,
                scrollBehavior = scrollBehavior,
                scrollProgress = scrollProgress,
                listState = listState
            )
        }
    }
}

@Composable
private fun AboutContent(
    state: AboutUiState,
    appIcon: androidx.compose.ui.graphics.ImageBitmap,
    actions: AboutScreenActions,
    contentPadding: PaddingValues,
    bottomPadding: androidx.compose.ui.unit.Dp,
    scrollBehavior: top.yukonga.miuix.kmp.basic.ScrollBehavior,
    scrollProgress: Float,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    val surfaceColor = MiuixTheme.colorScheme.surface
    val heroBackdrop = rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }
    val heroBlurEnabled = remember { isRuntimeShaderSupported() }

    Box(modifier = if (heroBlurEnabled) Modifier.layerBackdrop(heroBackdrop) else Modifier) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .overScrollVertical()
                .scrollEndHaptic(),
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding(),
                bottom = bottomPadding
            ),
            overscrollEffect = null
        ) {
            item {
                AboutHero(
                    appName = state.appName,
                    versionText = state.versionText,
                    appIcon = appIcon,
                    scrollProgress = scrollProgress,
                    heroBackdrop = heroBackdrop,
                    heroBlurEnabled = heroBlurEnabled
                )
            }
            item { SmallTitle(text = stringResource(R.string.app)) }
            item {
                AboutPreferenceCard(
                    title = stringResource(R.string.ghrepo),
                    summary = stringResource(R.string.view_source),
                    onClick = { actions.onOpenUrl(state.sourceUrl) }
                )
            }
            item { SmallTitle(text = stringResource(R.string.deps_libs)) }
            items(state.dependencies.size) { index ->
                val dependency = state.dependencies[index]
                AboutPreferenceCard(
                    title = dependency.name,
                    summary = dependency.summary,
                    enabled = dependency.url != null,
                    onClick = { dependency.url?.let(actions.onOpenUrl) }
                )
            }
        }
    }
}

@Composable
private fun AboutHero(
    appName: String,
    versionText: String,
    appIcon: androidx.compose.ui.graphics.ImageBitmap,
    scrollProgress: Float,
    heroBackdrop: top.yukonga.miuix.kmp.blur.LayerBackdrop,
    heroBlurEnabled: Boolean
) {
    val heroAlpha = 1f - scrollProgress
    val heroScale = 1f - scrollProgress * 0.05f
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 92.dp, bottom = 44.dp)
            .graphicsLayer {
                alpha = heroAlpha
                scaleX = heroScale
                scaleY = heroScale
                translationY = -scrollProgress * 42f
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            bitmap = appIcon,
            contentDescription = appName,
            modifier = Modifier
                .size(104.dp)
                .clip(RoundedCornerShape(24.dp))
                .then(
                    if (heroBlurEnabled) {
                        Modifier.textureBlur(
                            backdrop = heroBackdrop,
                            shape = RoundedCornerShape(24.dp),
                            blurRadius = 80f,
                            colors = BlurColors(
                                blendColors = listOf(
                                    BlendColorEntry(
                                        MiuixTheme.colorScheme.primary.copy(alpha = 0.28f),
                                        BlurBlendMode.Screen
                                    )
                                )
                            )
                        )
                    } else {
                        Modifier
                    }
                )
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = appName,
            color = MiuixTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 35.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = versionText,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            style = MiuixTheme.textStyles.body2,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun AboutPreferenceCard(
    title: String,
    summary: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(bottom = 8.dp),
        insideMargin = PaddingValues(0.dp)
    ) {
        ArrowPreference(
            title = title,
            summary = summary.ifBlank { null },
            enabled = enabled,
            onClick = onClick
        )
    }
}

private fun loadAboutDependencies(context: Context): List<AboutDependency> {
    return runCatching {
        Libs.Builder()
            .withContext(context)
            .build()
            .libraries
            .map { library ->
                AboutDependency(
                    name = library.name,
                    summary = library.licenses.joinToString { it.name },
                    url = library.website.orEmpty().ifBlank { null }
                )
            }
            .sortedBy { it.name.lowercase() }
    }.onFailure { throwable ->
        if (BuildConfig.DEBUG) {
            Log.w("AboutScreen", "Unable to load generated dependency metadata", throwable)
        }
    }.getOrElse { fallbackDependencies() }
}

private fun fallbackDependencies(): List<AboutDependency> = listOf(
    AboutDependency(
        name = "Miuix",
        summary = "Apache-2.0",
        url = "https://github.com/compose-miuix/miuix"
    ),
    AboutDependency(
        name = "Shizuku API",
        summary = "Apache-2.0",
        url = "https://github.com/RikkaApps/Shizuku-API"
    ),
    AboutDependency(
        name = "libsu",
        summary = "Apache-2.0",
        url = "https://github.com/topjohnwu/libsu"
    ),
    AboutDependency(
        name = "AboutLibraries",
        summary = "Apache-2.0",
        url = "https://github.com/mikepenz/AboutLibraries"
    )
)
