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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.delay
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.Search
import top.yukonga.miuix.kmp.icon.basic.SearchCleanup
import top.yukonga.miuix.kmp.theme.MiuixTheme
import vegabobo.languageselector.R
import vegabobo.languageselector.domain.apps.AppInfo
import vegabobo.languageselector.ui.screen.main.AppSearchState
import vegabobo.languageselector.ui.screen.main.SearchPhase

@Composable
fun CollapsedAppSearch(
    label: String,
    topPadding: Dp,
    onClick: () -> Unit,
    onOffsetChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(top = topPadding, bottom = 6.dp)
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
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .onGloballyPositioned {
                    onOffsetChanged(it.localToWindow(androidx.compose.ui.geometry.Offset.Zero).y)
                }
                .clickable(
                    interactionSource = null,
                    indication = null,
                    onClick = onClick
                )
        )
    }
}

@Composable
fun AppSearchOverlay(
    state: AppSearchState,
    results: List<AppInfo>,
    bottomPadding: Dp,
    onQueryChange: (String) -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onExpansionFinished: () -> Unit,
    onCloseRequested: () -> Unit,
    onCancelRequested: () -> Unit,
    onCollapseFinished: () -> Unit
) {
    if (!state.isVisible) return

    val density = LocalDensity.current
    val systemTop = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
    val collapsedTop = with(density) { state.collapsedOffsetY.toDp() }
    val expanding = state.phase == SearchPhase.Expanding || state.phase == SearchPhase.Expanded
    val topPadding by animateDpAsState(
        targetValue = if (expanding) systemTop + 5.dp else collapsedTop,
        animationSpec = tween(300, easing = LinearOutSlowInEasing),
        label = "AppSearchTop"
    )
    val surfaceAlpha by animateFloatAsState(
        targetValue = if (expanding) 1f else 0f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "AppSearchSurface"
    )

    LaunchedEffect(state.phase) {
        when (state.phase) {
            SearchPhase.Expanding -> {
                delay(300)
                onExpansionFinished()
            }
            SearchPhase.Collapsing -> {
                delay(300)
                onCollapseFinished()
            }
            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(20f)
            .background(MiuixTheme.colorScheme.surface.copy(alpha = surfaceAlpha))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = topPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExpandedSearchField(
                query = state.query,
                onQueryChange = onQueryChange,
                requestFocus = state.phase == SearchPhase.Expanding,
                modifier = Modifier.weight(1f)
            )
            AnimatedVisibility(
                visible = expanding,
                enter = expandHorizontally() + fadeIn(),
                exit = shrinkHorizontally() + fadeOut()
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    fontWeight = FontWeight.Bold,
                    color = MiuixTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(start = 4.dp, end = 16.dp, top = 12.dp, bottom = 6.dp)
                        .clickable(interactionSource = null, indication = null) { onCancelRequested() }
                )
            }
        }
        AnimatedVisibility(
            visible = state.phase == SearchPhase.Expanded,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    top = 6.dp,
                    bottom = bottomPadding
                )
            ) {
                items(results, key = { it.pkg }) { app ->
                    AppListItem(app = app, onClickApp = { onAppClick(app) })
                }
            }
        }
    }

    val backState = rememberNavigationEventState(NavigationEventInfo.None)
    NavigationBackHandler(
        state = backState,
        isBackEnabled = true,
        onBackCompleted = onCloseRequested
    )
}

@Composable
private fun ExpandedSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    requestFocus: Boolean,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    var value by remember { mutableStateOf(TextFieldValue(query)) }
    LaunchedEffect(query) {
        if (value.text != query) value = TextFieldValue(query)
    }
    LaunchedEffect(requestFocus) {
        if (requestFocus) focusRequester.requestFocus()
    }

    BasicTextField(
        value = value,
        onValueChange = {
            value = it
            onQueryChange(it.text)
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
            .padding(horizontal = 12.dp)
            .padding(top = 12.dp, bottom = 6.dp)
            .heightIn(min = 45.dp)
            .background(MiuixTheme.colorScheme.surfaceContainerHigh, CircleShape)
            .focusRequester(focusRequester),
        decorationBox = { field ->
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                    visible = query.isNotEmpty(),
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
                            .clickable(interactionSource = null, indication = null) {
                                value = TextFieldValue("")
                                onQueryChange("")
                            }
                    )
                }
            }
        }
    )
}
