package vegabobo.languageselector.ui.screen.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.PullToRefresh
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.rememberPullToRefreshState
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.MoreCircle
import top.yukonga.miuix.kmp.icon.extended.Notes
import top.yukonga.miuix.kmp.icon.extended.Sort
import top.yukonga.miuix.kmp.menu.OverlayIconDropdownMenu
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.MiuixPopupUtils.Companion.MiuixPopupHost
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import vegabobo.languageselector.R
import vegabobo.languageselector.domain.apps.AppInfo
import vegabobo.languageselector.domain.apps.AppSortField
import vegabobo.languageselector.ui.components.AppListItem
import vegabobo.languageselector.ui.components.AppSearchPager
import vegabobo.languageselector.ui.components.BlurredTopBar
import vegabobo.languageselector.ui.components.CollapsedAppSearch
import vegabobo.languageselector.ui.components.SearchBox
import vegabobo.languageselector.ui.components.TopAppBarAnim
import vegabobo.languageselector.ui.components.rememberAppBlurBackdrop

data class MainScreenActions(
    val onAppClick: (AppInfo) -> Unit,
    val onHistoryClick: () -> Unit,
    val onAboutClick: () -> Unit,
    val onRefresh: () -> Unit,
    val onSortFieldChange: (AppSortField) -> Unit,
    val onSortDirectionToggle: () -> Unit,
    val onSystemAppsToggle: () -> Unit,
    val onModifiedOnlyToggle: () -> Unit,
    val onSearchStatusChange: (AppSearchStatus) -> Unit,
    val onRequestShizuku: () -> Unit
)

@Composable
fun MainScreen(
    activityResumeCount: Int,
    navigateToAppScreen: (String) -> Unit,
    navigateToHistory: () -> Unit,
    navigateToAbout: () -> Unit,
    requestAppListAccess: (() -> Unit) -> Unit,
    requestShizukuAccess: (() -> Unit) -> Unit,
    mainScreenVm: MainScreenVm = hiltViewModel()
) {
    val state by mainScreenVm.uiState.collectAsState()
    LaunchedEffect(Unit) { mainScreenVm.reloadLastSelectedItem() }
    LaunchedEffect(activityResumeCount) {
        if (activityResumeCount > 0) {
            requestAppListAccess(mainScreenVm::onAppListPermissionGranted)
        }
    }

    MainScreenContent(
        state = state,
        actions = MainScreenActions(
            onAppClick = { app ->
                navigateToAppScreen(app.pkg)
                mainScreenVm.onClickApp(app)
            },
            onHistoryClick = navigateToHistory,
            onAboutClick = navigateToAbout,
            onRefresh = mainScreenVm::refresh,
            onSortFieldChange = mainScreenVm::updateSortField,
            onSortDirectionToggle = mainScreenVm::toggleSortDirection,
            onSystemAppsToggle = mainScreenVm::toggleSystemAppsVisibility,
            onModifiedOnlyToggle = mainScreenVm::toggleModifiedOnly,
            onSearchStatusChange = mainScreenVm::updateSearchStatus,
            onRequestShizuku = {
                requestShizukuAccess(mainScreenVm::onShizukuPermissionGranted)
            }
        )
    )
}

@Composable
fun MainScreenContent(
    state: MainScreenState,
    actions: MainScreenActions
) {
    val scrollBehavior = MiuixScrollBehavior()
    val dynamicSearchPadding by remember {
        derivedStateOf { 12.dp * (1f - scrollBehavior.state.collapsedFraction) }
    }
    val pullState = rememberPullToRefreshState()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
        WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()
    val sortEntries = rememberSortEntries(
        selected = state.preferences.sortField,
        descending = state.preferences.descending,
        onSelect = actions.onSortFieldChange,
        onReverse = actions.onSortDirectionToggle,
    )
    val moreEntries = rememberMoreEntries(
        showSystemApps = state.preferences.showSystemApps,
        modifiedOnly = state.preferences.modifiedOnly,
        onSystemAppsToggle = actions.onSystemAppsToggle,
        onModifiedOnlyToggle = actions.onModifiedOnlyToggle,
        onAboutClick = actions.onAboutClick,
    )
    val backdrop = rememberAppBlurBackdrop()
    val barColor = if (backdrop != null) Color.Transparent else MiuixTheme.colorScheme.surface
    val searchStatus = state.search.copy(label = stringResource(R.string.search))

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                BlurredTopBar(backdrop = backdrop) {
                    searchStatus.TopAppBarAnim(backgroundColor = barColor) {
                        TopAppBar(
                            title = stringResource(R.string.apps),
                            color = barColor,
                            scrollBehavior = scrollBehavior,
                            navigationIcon = {
                                IconButton(onClick = { actions.onHistoryClick() }) {
                                    Icon(
                                        imageVector = MiuixIcons.Notes,
                                        contentDescription = stringResource(R.string.history)
                                    )
                                }
                            },
                            actions = {
                                OverlayIconDropdownMenu(
                                    entries = sortEntries,
                                    collapseOnSelection = true,
                                ) {
                                    Icon(
                                        imageVector = MiuixIcons.Sort,
                                        contentDescription = stringResource(R.string.sort)
                                    )
                                }
                                OverlayIconDropdownMenu(
                                    entries = moreEntries,
                                    collapseOnSelection = true,
                                ) {
                                    Icon(
                                        imageVector = MiuixIcons.MoreCircle,
                                        contentDescription = stringResource(R.string.more_options)
                                    )
                                }
                            },
                            bottomContent = {
                                CollapsedAppSearch(
                                    status = searchStatus,
                                    topPadding = dynamicSearchPadding,
                                    onStatusChange = actions.onSearchStatusChange
                                )
                            }
                        )
                    }
                }
            },
            popupHost = {
                Box(Modifier.fillMaxSize()) {
                    AppSearchPager(
                        status = searchStatus,
                        results = state.searchResults,
                        bottomPadding = bottomInset,
                        searchBarTopPadding = dynamicSearchPadding,
                        onAppClick = actions.onAppClick,
                        onStatusChange = actions.onSearchStatusChange
                    )
                    if (state.isOperationModeResolved && state.operationMode == OperationMode.NONE) {
                        ShizukuRequiredWarning(onClickContinue = actions.onRequestShizuku)
                    }
                    MiuixPopupHost()
                }
            },
            contentWindowInsets = WindowInsets.systemBars
                .add(WindowInsets.displayCutout)
                .only(WindowInsetsSides.Horizontal)
        ) { innerPadding ->
            val layoutDirection = LocalLayoutDirection.current
            val listPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 6.dp,
                start = innerPadding.calculateStartPadding(layoutDirection),
                end = innerPadding.calculateEndPadding(layoutDirection),
                bottom = bottomInset
            )
            searchStatus.SearchBox {
                val listState = rememberLazyListState()
                val sortOrder = state.preferences.sortField to state.preferences.descending
                var appliedSortOrder by remember { mutableStateOf(sortOrder) }
                LaunchedEffect(sortOrder) {
                    if (appliedSortOrder == sortOrder) return@LaunchedEffect
                    appliedSortOrder = sortOrder
                    listState.scrollToItem(0)
                    scrollBehavior.state.heightOffset = 0f
                    scrollBehavior.state.contentOffset = 0f
                }
                if (state.isLoading && state.listOfApps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier)
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            InfiniteProgressIndicator(size = 20.dp)
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = stringResource(R.string.loading),
                                style = MiuixTheme.textStyles.body1,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                } else {
                    PullToRefresh(
                        isRefreshing = state.isRefreshing,
                        onRefresh = actions.onRefresh,
                        pullToRefreshState = pullState,
                        refreshTexts = listOf(
                            stringResource(R.string.refresh_pulling),
                            stringResource(R.string.refresh_release),
                            stringResource(R.string.refresh_refreshing),
                            stringResource(R.string.refresh_complete)
                        ),
                        contentPadding = PaddingValues(
                            top = listPadding.calculateTopPadding(),
                            start = listPadding.calculateStartPadding(layoutDirection),
                            end = listPadding.calculateEndPadding(layoutDirection)
                        )
                    ) {
                        Box(modifier = if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier) {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .scrollEndHaptic()
                                    .overScrollVertical()
                                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                                contentPadding = listPadding,
                                overscrollEffect = null
                            ) {
                                if (
                                    state.preferences.modifiedOnly &&
                                    state.isLocaleRefreshRunning &&
                                    !state.isRefreshing
                                ) {
                                    item(key = "modified-progress") {
                                        Box(
                                            modifier = Modifier
                                                .fillParentMaxWidth()
                                                .padding(bottom = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            InfiniteProgressIndicator(size = 20.dp)
                                        }
                                    }
                                }
                                items(state.visibleHomeApps, key = { it.pkg }) { app ->
                                    AppListItem(
                                        app = app,
                                        onClickApp = { actions.onAppClick(app) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}

@Composable
private fun rememberSortEntries(
    selected: AppSortField,
    descending: Boolean,
    onSelect: (AppSortField) -> Unit,
    onReverse: () -> Unit,
): List<DropdownEntry> {
    val name = stringResource(R.string.sort_name)
    val packageName = stringResource(R.string.sort_package)
    val installTime = stringResource(R.string.sort_install_time)
    val updateTime = stringResource(R.string.sort_update_time)
    val reverse = stringResource(R.string.sort_reverse)
    val currentOnSelect = rememberUpdatedState(onSelect)
    val currentOnReverse = rememberUpdatedState(onReverse)
    return remember(selected, descending, name, packageName, installTime, updateTime, reverse) {
        listOf(
            DropdownEntry(
                items = listOf(
                    DropdownItem(
                        text = name,
                        selected = selected == AppSortField.Name,
                        onClick = { currentOnSelect.value(AppSortField.Name) },
                    ),
                    DropdownItem(
                        text = packageName,
                        selected = selected == AppSortField.PackageName,
                        onClick = { currentOnSelect.value(AppSortField.PackageName) },
                    ),
                    DropdownItem(
                        text = installTime,
                        selected = selected == AppSortField.InstallTime,
                        onClick = { currentOnSelect.value(AppSortField.InstallTime) },
                    ),
                    DropdownItem(
                        text = updateTime,
                        selected = selected == AppSortField.UpdateTime,
                        onClick = { currentOnSelect.value(AppSortField.UpdateTime) },
                    ),
                ),
            ),
            DropdownEntry(
                items = listOf(
                    DropdownItem(
                        text = reverse,
                        selected = descending,
                        onClick = { currentOnReverse.value() },
                    ),
                ),
            ),
        )
    }
}

@Composable
private fun rememberMoreEntries(
    showSystemApps: Boolean,
    modifiedOnly: Boolean,
    onSystemAppsToggle: () -> Unit,
    onModifiedOnlyToggle: () -> Unit,
    onAboutClick: () -> Unit,
): List<DropdownEntry> {
    val showSystemAppsLabel = stringResource(R.string.show_system_apps)
    val modifiedOnlyLabel = stringResource(R.string.modified_only)
    val aboutLabel = stringResource(R.string.about)
    val currentOnSystemAppsToggle = rememberUpdatedState(onSystemAppsToggle)
    val currentOnModifiedOnlyToggle = rememberUpdatedState(onModifiedOnlyToggle)
    val currentOnAboutClick = rememberUpdatedState(onAboutClick)
    return remember(
        showSystemApps,
        modifiedOnly,
        showSystemAppsLabel,
        modifiedOnlyLabel,
        aboutLabel,
    ) {
        listOf(
            DropdownEntry(
                items = listOf(
                    DropdownItem(
                        text = showSystemAppsLabel,
                        selected = showSystemApps,
                        onClick = { currentOnSystemAppsToggle.value() },
                    ),
                    DropdownItem(
                        text = modifiedOnlyLabel,
                        selected = modifiedOnly,
                        onClick = { currentOnModifiedOnlyToggle.value() },
                    ),
                    DropdownItem(
                        text = aboutLabel,
                        onClick = { currentOnAboutClick.value() },
                    ),
                ),
            ),
        )
    }
}
