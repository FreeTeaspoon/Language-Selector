package vegabobo.languageselector.ui.screen.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.PullToRefresh
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.rememberPullToRefreshState
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.MoreCircle
import top.yukonga.miuix.kmp.icon.extended.Notes
import top.yukonga.miuix.kmp.icon.extended.Sort
import top.yukonga.miuix.kmp.overlay.OverlayListPopup
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import vegabobo.languageselector.R
import vegabobo.languageselector.domain.apps.AppInfo
import vegabobo.languageselector.domain.apps.AppSortField
import vegabobo.languageselector.ui.components.AppDropdownItem
import vegabobo.languageselector.ui.components.AppListItem
import vegabobo.languageselector.ui.components.AppPopupDefaults
import vegabobo.languageselector.ui.components.AppSearchOverlay
import vegabobo.languageselector.ui.components.BlurredTopBar
import vegabobo.languageselector.ui.components.CollapsedAppSearch
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
    val onSearchOpen: () -> Unit,
    val onSearchQueryChange: (String) -> Unit,
    val onSearchExpansionFinished: () -> Unit,
    val onSearchCloseRequested: () -> Unit,
    val onSearchCancelRequested: () -> Unit,
    val onSearchCollapseFinished: () -> Unit,
    val onSearchOffsetChanged: (Float) -> Unit,
    val onRequestShizuku: () -> Unit
)

@Composable
fun MainScreen(
    navigateToAppScreen: (String) -> Unit,
    navigateToHistory: () -> Unit,
    navigateToAbout: () -> Unit,
    requestShizukuAccess: () -> Unit,
    mainScreenVm: MainScreenVm = hiltViewModel()
) {
    val state by mainScreenVm.uiState.collectAsState()
    LaunchedEffect(Unit) { mainScreenVm.reloadLastSelectedItem() }

    MainScreenContent(
        state = state,
        actions = MainScreenActions(
            onAppClick = { app ->
                mainScreenVm.onClickApp(app)
                navigateToAppScreen(app.pkg)
            },
            onHistoryClick = navigateToHistory,
            onAboutClick = navigateToAbout,
            onRefresh = mainScreenVm::refresh,
            onSortFieldChange = mainScreenVm::updateSortField,
            onSortDirectionToggle = mainScreenVm::toggleSortDirection,
            onSystemAppsToggle = mainScreenVm::toggleSystemAppsVisibility,
            onModifiedOnlyToggle = mainScreenVm::toggleModifiedOnly,
            onSearchOpen = mainScreenVm::openSearch,
            onSearchQueryChange = mainScreenVm::onSearchQueryChange,
            onSearchExpansionFinished = mainScreenVm::finishSearchExpansion,
            onSearchCloseRequested = mainScreenVm::requestSearchClose,
            onSearchCancelRequested = mainScreenVm::cancelSearch,
            onSearchCollapseFinished = mainScreenVm::finishSearchCollapse,
            onSearchOffsetChanged = mainScreenVm::updateSearchOffset,
            onRequestShizuku = requestShizukuAccess
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
    val listState = rememberLazyListState()
    val pullState = rememberPullToRefreshState()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
        WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()
    var showSortPopup by remember { mutableStateOf(false) }
    var showMorePopup by remember { mutableStateOf(false) }
    val backdrop = rememberAppBlurBackdrop()
    val barColor = if (backdrop != null) Color.Transparent else MiuixTheme.colorScheme.surface

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                BlurredTopBar(backdrop = backdrop) {
                    TopAppBar(
                        title = stringResource(R.string.apps),
                        color = barColor,
                        scrollBehavior = scrollBehavior,
                        navigationIcon = {
                            IconButton(onClick = actions.onHistoryClick) {
                                Icon(
                                    imageVector = MiuixIcons.Notes,
                                    contentDescription = stringResource(R.string.history)
                                )
                            }
                        },
                        actions = {
                            Box {
                                OverlayListPopup(
                                    show = showSortPopup,
                                    popupPositionProvider = AppPopupDefaults.MenuPositionProvider,
                                    alignment = PopupPositionProvider.Align.TopEnd,
                                    onDismissRequest = { showSortPopup = false }
                                ) {
                                    SortPopupContent(
                                        selected = state.preferences.sortField,
                                        descending = state.preferences.descending,
                                        onSelect = {
                                            actions.onSortFieldChange(it)
                                            showSortPopup = false
                                        },
                                        onReverse = {
                                            actions.onSortDirectionToggle()
                                            showSortPopup = false
                                        }
                                    )
                                }
                                IconButton(
                                    onClick = { showSortPopup = true },
                                    holdDownState = showSortPopup
                                ) {
                                    Icon(
                                        imageVector = MiuixIcons.Sort,
                                        contentDescription = stringResource(R.string.sort)
                                    )
                                }
                            }
                            Box {
                                OverlayListPopup(
                                    show = showMorePopup,
                                    popupPositionProvider = AppPopupDefaults.MenuPositionProvider,
                                    alignment = PopupPositionProvider.Align.TopEnd,
                                    onDismissRequest = { showMorePopup = false }
                                ) {
                                    MorePopupContent(
                                        showSystemApps = state.preferences.showSystemApps,
                                        modifiedOnly = state.preferences.modifiedOnly,
                                        onSystemAppsToggle = {
                                            actions.onSystemAppsToggle()
                                            showMorePopup = false
                                        },
                                        onModifiedOnlyToggle = {
                                            actions.onModifiedOnlyToggle()
                                            showMorePopup = false
                                        },
                                        onAboutClick = {
                                            showMorePopup = false
                                            actions.onAboutClick()
                                        }
                                    )
                                }
                                IconButton(
                                    onClick = { showMorePopup = true },
                                    holdDownState = showMorePopup
                                ) {
                                    Icon(
                                        imageVector = MiuixIcons.MoreCircle,
                                        contentDescription = stringResource(R.string.more_options)
                                    )
                                }
                            }
                        },
                        bottomContent = {
                            CollapsedAppSearch(
                                label = stringResource(R.string.search),
                                topPadding = dynamicSearchPadding,
                                onClick = actions.onSearchOpen,
                                onOffsetChanged = actions.onSearchOffsetChanged
                            )
                        }
                    )
                }
            },
            popupHost = {
                AppSearchOverlay(
                    state = state.search,
                    results = state.searchResults,
                    bottomPadding = bottomInset,
                    onQueryChange = actions.onSearchQueryChange,
                    onAppClick = actions.onAppClick,
                    onExpansionFinished = actions.onSearchExpansionFinished,
                    onCloseRequested = actions.onSearchCloseRequested,
                    onCancelRequested = actions.onSearchCancelRequested,
                    onCollapseFinished = actions.onSearchCollapseFinished
                )
            },
            contentWindowInsets = WindowInsets.systemBars
                .add(WindowInsets.displayCutout)
                .only(WindowInsetsSides.Horizontal)
        ) { innerPadding ->
            Box(modifier = if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier) {
                if (state.isLoading && state.listOfApps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        InfiniteProgressIndicator()
                    }
                } else {
                    PullToRefresh(
                        isRefreshing = state.isRefreshing,
                        onRefresh = actions.onRefresh,
                        pullToRefreshState = pullState,
                        topAppBarScrollBehavior = scrollBehavior,
                        refreshTexts = listOf(
                            stringResource(R.string.refresh_pulling),
                            stringResource(R.string.refresh_release),
                            stringResource(R.string.refresh_refreshing),
                            stringResource(R.string.refresh_complete)
                        ),
                        contentPadding = PaddingValues(top = innerPadding.calculateTopPadding() + 6.dp)
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxHeight()
                                .scrollEndHaptic()
                                .overScrollVertical()
                                .nestedScroll(scrollBehavior.nestedScrollConnection),
                            contentPadding = PaddingValues(
                                top = innerPadding.calculateTopPadding() + 6.dp,
                                bottom = bottomInset
                            ),
                            overscrollEffect = null
                        ) {
                            if (state.preferences.modifiedOnly && state.isLocaleRefreshRunning) {
                                item(key = "modified-progress") {
                                    Box(
                                        modifier = Modifier
                                            .fillParentMaxWidth()
                                            .padding(bottom = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                            items(state.visibleHomeApps, key = { it.pkg }) { app ->
                                AppListItem(app = app, onClickApp = { actions.onAppClick(app) })
                            }
                        }
                    }
                }
            }
        }

        if (state.isOperationModeResolved && state.operationMode == OperationMode.NONE) {
            ShizukuRequiredWarning(onClickContinue = actions.onRequestShizuku)
        }
    }
}

@Composable
private fun SortPopupContent(
    selected: AppSortField,
    descending: Boolean,
    onSelect: (AppSortField) -> Unit,
    onReverse: () -> Unit
) {
    val options = listOf(
        AppSortField.Name to stringResource(R.string.sort_name),
        AppSortField.PackageName to stringResource(R.string.sort_package),
        AppSortField.InstallTime to stringResource(R.string.sort_install_time),
        AppSortField.UpdateTime to stringResource(R.string.sort_update_time)
    )
    ListPopupColumn {
        options.forEachIndexed { index, (field, label) ->
            DropdownImpl(
                text = label,
                optionSize = options.size + 1,
                isSelected = selected == field,
                index = index,
                onSelectedIndexChange = { onSelect(field) }
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            thickness = 1.5.dp
        )
        DropdownImpl(
            text = stringResource(R.string.sort_reverse),
            optionSize = options.size + 1,
            isSelected = descending,
            index = options.size,
            onSelectedIndexChange = { onReverse() }
        )
    }
}

@Composable
private fun MorePopupContent(
    showSystemApps: Boolean,
    modifiedOnly: Boolean,
    onSystemAppsToggle: () -> Unit,
    onModifiedOnlyToggle: () -> Unit,
    onAboutClick: () -> Unit
) {
    val labels = listOf(
        stringResource(R.string.show_system_apps),
        stringResource(R.string.modified_only),
        stringResource(R.string.about)
    )
    ListPopupColumn {
        DropdownImpl(
            text = labels[0],
            optionSize = labels.size,
            isSelected = showSystemApps,
            index = 0,
            onSelectedIndexChange = { onSystemAppsToggle() }
        )
        DropdownImpl(
            text = labels[1],
            optionSize = labels.size,
            isSelected = modifiedOnly,
            index = 1,
            onSelectedIndexChange = { onModifiedOnlyToggle() }
        )
        AppDropdownItem(
            text = labels[2],
            optionSize = labels.size,
            index = 2,
            onClick = onAboutClick
        )
    }
}
