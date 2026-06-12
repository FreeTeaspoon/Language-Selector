package vegabobo.languageselector.ui.screen.about

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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.layerBackdrop
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
    val title: String,
    val appName: String,
    val versionName: String,
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
                    title = state.title,
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
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .overScrollVertical()
                    .scrollEndHaptic(),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = bottomInset + 12.dp
                ),
                overscrollEffect = null
            ) {
                item {
                    AboutHero(
                        appName = state.appName,
                        versionName = state.versionName,
                        scrollProgress = scrollProgress
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
}

@Composable
private fun AboutHero(
    appName: String,
    versionName: String,
    scrollProgress: Float
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
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = appName,
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(24.dp))
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
            text = versionName,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            fontSize = 14.sp,
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

fun staticDependencies(): List<AboutDependency> = listOf(
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
    ),
    AboutDependency(
        name = "AndroidX",
        summary = "Apache-2.0",
        url = "https://github.com/androidx/androidx"
    ),
    AboutDependency(
        name = "Kotlin",
        summary = "Apache-2.0",
        url = "https://github.com/JetBrains/kotlin"
    )
)
