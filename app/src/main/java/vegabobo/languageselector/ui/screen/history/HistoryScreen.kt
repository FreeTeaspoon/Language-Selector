package vegabobo.languageselector.ui.screen.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import vegabobo.languageselector.R
import vegabobo.languageselector.ui.components.AppListItem
import vegabobo.languageselector.ui.components.BackButton

@Composable
fun HistoryScreen(
    navigateBack: () -> Unit,
    navigateToApp: (String) -> Unit,
    viewModel: HistoryScreenVm = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scrollBehavior = MiuixScrollBehavior()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
        WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()

    Scaffold(
        topBar = {
            TopAppBar(
                title = stringResource(R.string.history),
                navigationIcon = { BackButton(navigateBack) },
                actions = {
                    IconButton(
                        onClick = viewModel::clearHistory,
                        enabled = state.apps.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Delete,
                            contentDescription = stringResource(R.string.clear_history)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets.systemBars
            .add(WindowInsets.displayCutout)
            .only(WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { InfiniteProgressIndicator() }

            state.apps.isEmpty() -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.history_empty),
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
            }

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .scrollEndHaptic()
                    .overScrollVertical(),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 6.dp,
                    bottom = bottomInset
                ),
                overscrollEffect = null
            ) {
                items(state.apps, key = { it.pkg }) { app ->
                    AppListItem(app = app, onClickApp = navigateToApp)
                }
            }
        }
    }
}
