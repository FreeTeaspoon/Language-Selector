package vegabobo.languageselector.ui.screen.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import vegabobo.languageselector.R
import vegabobo.languageselector.ui.components.AppListItem
import vegabobo.languageselector.ui.components.AppSearchBar
import vegabobo.languageselector.ui.screen.BaseScreen
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.SnackbarHostState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    mainScreenVm: MainScreenVm = hiltViewModel(),
    navigateToAppScreen: (String) -> Unit,
    navigateToAbout: () -> Unit,
    requestShizukuAccess: () -> Unit,
) {
    val uiState by mainScreenVm.uiState.collectAsState()
    val sb = remember { SnackbarHostState() }
    val lazyListState = rememberLazyListState()
    val density = LocalDensity.current
    var searchBarHeight by remember { mutableStateOf(64.dp) }

    LaunchedEffect(Unit) {
        mainScreenVm.reloadLastSelectedItem()
        mainScreenVm.uiState.collectLatest {
            when (it.snackBarDisplay) {
                SnackBarDisplay.MOVED_TO_TOP -> {
                    val i = mainScreenVm.getIndexFromAppInfoItem()
                    if (i >= 0) lazyListState.animateScrollToItem(i)
                    sb.showSnackbar("Modified app has been moved up")
                }

                SnackBarDisplay.MOVED_TO_BOTTOM -> {
                    val i = mainScreenVm.getIndexFromAppInfoItem()
                    if (i >= 0) lazyListState.animateScrollToItem(i)
                    sb.showSnackbar("Unmodified has been moved down")
                }

                else -> {}
            }
            mainScreenVm.resetSnackBarDisplay()
        }
    }
    BaseScreen(snackBarHost = sb) {
        if (uiState.isLoading)
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        else {
            Box(
                Modifier
                    .fillMaxSize()
                    .semantics { isTraversalGroup = true }) {
                LazyColumn(
                    state = lazyListState,
                    contentPadding = PaddingValues(
                        top = searchBarHeight + 8.dp,
                        bottom = 8.dp
                    ),
                    modifier = Modifier.semantics { traversalIndex = 1f }
                ) {
                    items(
                        items = uiState.visibleHomeApps,
                        key = { it.pkg }
                    ) { thisApp ->
                        AppListItem(
                            modifier = Modifier.padding(
                                start = 26.dp,
                                end = 26.dp,
                                top = 4.dp,
                                bottom = 4.dp
                            ),
                            app = thisApp,
                            onClickApp = {
                                mainScreenVm.onClickApp(thisApp)
                                navigateToAppScreen(it)
                            }
                        )
                    }
                }

                if (uiState.operationMode == OperationMode.NONE) {
                    ShizukuRequiredWarning {
                        requestShizukuAccess()
                    }
                }

                AppSearchBar(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .onSizeChanged { size ->
                            searchBarHeight = with(density) { size.height.toDp() }
                        }
                        .semantics { traversalIndex = 0f },
                    placeholder = stringResource(R.string.search),
                    onUpdatedValue = { mainScreenVm.onSearchTextFieldChange(it) },
                    query = uiState.searchTextFieldValue,
                    onClickApp = { mainScreenVm.onClickApp(it); navigateToAppScreen(it.pkg) },
                    history = uiState.history,
                    apps = uiState.searchResults,
                    isExpanded = uiState.isExpanded,
                    onExpandedChange = { mainScreenVm.onSearchExpandedChange(it) },
                    selectedLabels = uiState.selectLabels,
                    isRefreshingAppStates = uiState.isLocaleRefreshRunning,
                    onSelectedLabelsChange = { mainScreenVm.onSelectedLabelChange(it) },
                    onClickClear = { mainScreenVm.onClickClear() },
                    actions = {
                        if (!uiState.isExpanded)
                            SearchBarActions(
                                isDropdownVisible = uiState.isDropdownVisible,
                                isShowingSystemApps = uiState.isShowSystemAppsHome,
                                onClickToggleDropdown = { mainScreenVm.toggleDropdown() },
                                onToggleDropdown = { mainScreenVm.toggleDropdown() },
                                onClickToggleSystemApps = { mainScreenVm.toggleSystemAppsVisibility() },
                                onClickAbout = { navigateToAbout() }
                            )
                    })
            }
        }
    }
}
