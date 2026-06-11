package vegabobo.languageselector.ui.screen.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import vegabobo.languageselector.R
import vegabobo.languageselector.ui.components.BackButton
import vegabobo.languageselector.ui.components.Title
import vegabobo.languageselector.ui.screen.BaseScreen
import vegabobo.languageselector.ui.screen.main.getAppIcon
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.util.withContext
import vegabobo.languageselector.BuildConfig
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun AboutScreen(
    navigateBack: () -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val libraries = remember(context) {
        Libs.Builder().withContext(context).build().libraries
    }

    BaseScreen(
        title = stringResource(R.string.about),
        navIcon = { BackButton { navigateBack() } }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = it.calculateTopPadding())
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        modifier = Modifier.size(88.dp),
                        bitmap = context.packageManager
                            .getAppIcon(context.applicationInfo)
                            .toBitmap().asImageBitmap(),
                        contentDescription = "App icon"
                    )
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MiuixTheme.textStyles.title2
                    )
                    Text(
                        stringResource(R.string.version).format(
                            BuildConfig.VERSION_NAME,
                            BuildConfig.VERSION_CODE
                        )
                    )
                }
            }
            item {
                Title(stringResource(id = R.string.app))
                PreferenceItem(
                    title = stringResource(R.string.ghrepo),
                    description = stringResource(R.string.view_source)
                ) {
                    uriHandler.openUri("https://github.com/VegaBobo/Language-Selector")
                }
            }
            item { Title(stringResource(R.string.deps_libs)) }
            items(libraries.size) {
                val thisLibrary = libraries[it]
                val name = thisLibrary.name
                var licenses = ""
                for (license in thisLibrary.licenses) {
                    licenses += license.name
                }
                val urlToOpen = thisLibrary.website ?: ""
                PreferenceItem(
                    title = name,
                    description = licenses,
                    onClick = {
                        if (urlToOpen.isNotEmpty()) {
                            uriHandler.openUri(urlToOpen)
                        }
                    },
                )
            }
            item { Spacer(modifier = Modifier.padding(bottom = it.calculateBottomPadding())) }
        }
    }

}

@Composable
fun PreferenceItem(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    BasicComponent(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp),
        title = title,
        summary = description.ifBlank { null },
        holdDownState = true,
        onClick = onClick
    )
}
