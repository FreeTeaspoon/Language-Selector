package vegabobo.languageselector.ui.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SnackbarHost
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.basic.TopAppBar

@Composable
fun BaseScreen(
    modifier: Modifier = Modifier,
    title: String? = null,
    snackBarHost: SnackbarHostState = SnackbarHostState(),
    navIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    val scrollBehavior = MiuixScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
        snackbarHost = { SnackbarHost(state = snackBarHost) },
        topBar = {
            if (title?.isNotEmpty() == true) {
                TopAppBar(
                    title = title,
                    navigationIcon = navIcon ?: {},
                    actions = actions,
                    scrollBehavior = scrollBehavior
                )
            }
        },
        content = content
    )
}
