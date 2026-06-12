package vegabobo.languageselector.ui.screen.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.util.withContext
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import vegabobo.languageselector.BuildConfig
import vegabobo.languageselector.R
import vegabobo.languageselector.ui.components.BackButton
import vegabobo.languageselector.ui.screen.main.getAppIcon

@Composable
fun AboutScreen(navigateBack: () -> Unit) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val libraries = remember(context) { Libs.Builder().withContext(context).build().libraries }
    val appIcon = remember(context) {
        context.packageManager.getAppIcon(context.applicationInfo).toBitmap().asImageBitmap()
    }
    val listState = rememberLazyListState()
    val scrollBehavior = MiuixScrollBehavior()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
        WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = stringResource(R.string.about),
                navigationIcon = { BackButton(navigateBack) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { contentPadding ->
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding(),
                bottom = bottomInset + 12.dp
            ),
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .overScrollVertical()
                .scrollEndHaptic()
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        bitmap = appIcon,
                        contentDescription = stringResource(R.string.app_name),
                        modifier = Modifier.size(88.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MiuixTheme.textStyles.title1
                    )
                    Text(
                        text = stringResource(R.string.version).format(
                            BuildConfig.VERSION_NAME,
                            BuildConfig.VERSION_CODE
                        ),
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                }
            }

            item { SmallTitle(text = stringResource(R.string.app)) }
            item {
                AboutPreferenceCard(
                    title = stringResource(R.string.ghrepo),
                    summary = stringResource(R.string.view_source),
                    onClick = { uriHandler.openUri("https://github.com/FreeTeaspoon/Language-Selector") }
                )
            }

            item { SmallTitle(text = stringResource(R.string.deps_libs)) }
            items(libraries.size) { index ->
                val library = libraries[index]
                val website = library.website.orEmpty()
                AboutPreferenceCard(
                    title = library.name,
                    summary = library.licenses.joinToString { it.name },
                    enabled = website.isNotEmpty(),
                    onClick = { uriHandler.openUri(website) }
                )
            }
        }
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
