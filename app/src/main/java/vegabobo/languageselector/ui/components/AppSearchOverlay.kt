package vegabobo.languageselector.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.Search
import top.yukonga.miuix.kmp.icon.basic.SearchCleanup
import top.yukonga.miuix.kmp.theme.MiuixTheme
import vegabobo.languageselector.R
import vegabobo.languageselector.domain.apps.AppInfo
import vegabobo.languageselector.ui.screen.main.AppSearchStatus
import vegabobo.languageselector.ui.screen.main.SearchPhase
import vegabobo.languageselector.ui.screen.main.SearchResultState
import vegabobo.languageselector.ui.screen.main.animationFinished
import vegabobo.languageselector.ui.screen.main.cancelRequested
import vegabobo.languageselector.ui.screen.main.closeRequested
import vegabobo.languageselector.ui.theme.PageSpacing

@Composable
fun AppSearchStatus.TopAppBarAnim(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MiuixTheme.colorScheme.surface,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(backgroundColor)
        )
        Box(
            modifier = Modifier.graphicsLayer {
                alpha = if (shouldCollapsed()) 1f else 0f
            }
        ) {
            content()
        }
    }
}

@Composable
fun AppSearchStatus.SearchBox(
    content: @Composable () -> Unit
) {
    if (shouldCollapsed()) content()
}

@Composable
fun CollapsedAppSearch(
    status: AppSearchStatus,
    topPadding: Dp,
    onStatusChange: (AppSearchStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = if (status.isCollapsed()) 1f else 0f }
            .onGloballyPositioned { coordinates ->
                if (status.isCollapsed()) {
                    val newOffset = with(density) { coordinates.positionInWindow().y.toDp() }
                    if (status.offsetY != newOffset) {
                        onStatusChange(status.copy(offsetY = newOffset))
                    }
                }
            }
            .then(
                if (status.isCollapsed()) {
                    Modifier
                        .heightIn(min = 48.dp)
                        .clickable(
                            interactionSource = null,
                            indication = null,
                            onClick = {
                                onStatusChange(status.copy(current = SearchPhase.Expanding))
                            }
                        )
                        .semantics {
                            role = Role.Button
                            contentDescription = status.label
                        }
                } else {
                    Modifier
                }
            )
    ) {
        SearchBarFake(label = status.label, topPadding = topPadding)
    }
}

@Composable
fun AppSearchPager(
    status: AppSearchStatus,
    results: List<AppInfo>,
    bottomPadding: Dp,
    searchBarTopPadding: Dp,
    onStatusChange: (AppSearchStatus) -> Unit,
    onAppClick: (AppInfo) -> Unit
) {
    val systemTop = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
    val imeBottomPadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val emptyStateBottomPadding = max(bottomPadding, imeBottomPadding)
    val topPadding by animateDpAsState(
        targetValue = if (status.shouldExpand()) {
            systemTop + 5.dp
        } else {
            max(status.offsetY, 0.dp)
        },
        animationSpec = tween(300, easing = LinearOutSlowInEasing),
        label = "AppSearchTopPadding",
        finishedListener = {
            onStatusChange(status.animationFinished())
        }
    )
    val surfaceAlpha by animateFloatAsState(
        targetValue = if (status.shouldExpand()) 1f else 0f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "AppSearchSurfaceAlpha"
    )
    val surfaceColor = MiuixTheme.colorScheme.surface

    Column(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(20f)
            .drawBehind { drawRect(surfaceColor.copy(alpha = surfaceAlpha)) }
            .semantics { onClick { false } }
            .then(if (status.isVisible) Modifier.pointerInput(Unit) {} else Modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = topPadding)
                .then(if (!status.isCollapsed()) Modifier.background(surfaceColor) else Modifier),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!status.isCollapsed()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(surfaceColor)
                ) {
                    ExpandedSearchField(
                        status = status,
                        onStatusChange = onStatusChange,
                        topPadding = searchBarTopPadding
                    )
                }
            }
    AnimatedVisibility(
                visible = status.isExpand() || status.isAnimatingExpand(),
                enter = expandHorizontally() + slideInHorizontally(initialOffsetX = { it }),
                exit = shrinkHorizontally() + slideOutHorizontally(targetOffsetX = { it })
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    fontWeight = FontWeight.Bold,
                    color = MiuixTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(start = 4.dp, end = 16.dp, top = searchBarTopPadding, bottom = 6.dp)
                        .clickable(
                            interactionSource = null,
                            indication = null,
                            enabled = status.isExpand()
                        ) {
                            onStatusChange(status.cancelRequested())
                        }
                )
            }
        }

        AnimatedVisibility(
            visible = status.isExpand(),
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .zIndex(1f)
        ) {
            when (status.resultStatus) {
                SearchResultState.Default -> {}
                SearchResultState.Empty -> SearchMessage(
                    text = stringResource(R.string.no_search_results, status.searchText),
                    bottomPadding = emptyStateBottomPadding
                )

                SearchResultState.Results -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = PageSpacing.ExtraTop, bottom = bottomPadding)
                ) {
                    items(results, key = { it.pkg }) { app ->
                        AppListItem(
                            app = app,
                            onClickApp = { onAppClick(app) }
                        )
                    }
                }
            }
        }
    }

    val backState = rememberNavigationEventState(NavigationEventInfo.None)
    NavigationBackHandler(
        state = backState,
        isBackEnabled = status.isVisible,
        onBackCompleted = { onStatusChange(status.closeRequested()) }
    )
}

@Composable
private fun SearchMessage(text: String, bottomPadding: Dp) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = bottomPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
private fun ExpandedSearchField(
    status: AppSearchStatus,
    onStatusChange: (AppSearchStatus) -> Unit,
    topPadding: Dp,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val clearSearchDescription = stringResource(R.string.clear_search)
    var value by remember { mutableStateOf(TextFieldValue(status.searchText)) }

    LaunchedEffect(status.searchText) {
        if (value.text != status.searchText) value = TextFieldValue(status.searchText)
    }
    LaunchedEffect(status.isAnimatingExpand()) {
        if (status.isAnimatingExpand()) focusRequester.requestFocus()
    }

    BasicTextField(
        value = value,
        onValueChange = {
            value = it
            onStatusChange(status.copy(searchText = it.text))
        },
        singleLine = true,
        textStyle = TextStyle(
            fontWeight = FontWeight.Medium,
            fontSize = 17.sp,
            color = MiuixTheme.colorScheme.onSurface
        ),
        cursorBrush = SolidColor(MiuixTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(top = topPadding, bottom = 6.dp)
            .heightIn(min = 45.dp)
            .background(MiuixTheme.colorScheme.surfaceContainerHigh, CircleShape)
            .focusRequester(focusRequester),
        decorationBox = { field ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = MiuixIcons.Basic.Search,
                    contentDescription = null,
                    tint = MiuixTheme.colorScheme.onSurfaceContainerHigh,
                    modifier = Modifier
                        .size(44.dp)
                        .padding(start = 16.dp, end = 8.dp)
                )
                Box(modifier = Modifier.weight(1f)) { field() }
                AnimatedVisibility(
                    visible = status.searchText.isNotEmpty(),
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Icon(
                        imageVector = MiuixIcons.Basic.SearchCleanup,
                        contentDescription = null,
                        tint = MiuixTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .size(44.dp)
                            .padding(start = 8.dp, end = 16.dp)
                            .semantics { contentDescription = clearSearchDescription }
                            .clickable(interactionSource = null, indication = null) {
                                value = TextFieldValue("")
                                onStatusChange(status.copy(searchText = ""))
                            }
                    )
                }
            }
        }
    )
}

@Composable
private fun SearchBarFake(
    label: String,
    topPadding: Dp
) {
    InputField(
        query = "",
        onQueryChange = {},
        label = label,
        leadingIcon = {
            Icon(
                imageVector = MiuixIcons.Basic.Search,
                contentDescription = null,
                tint = MiuixTheme.colorScheme.onSurfaceContainerHigh,
                modifier = Modifier
                    .size(44.dp)
                    .padding(start = 16.dp, end = 8.dp)
            )
        },
        enabled = false,
        expanded = false,
        onExpandedChange = {},
        onSearch = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(top = topPadding, bottom = 6.dp)
    )
}
