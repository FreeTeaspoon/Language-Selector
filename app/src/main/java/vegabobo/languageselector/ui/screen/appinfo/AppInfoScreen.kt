package vegabobo.languageselector.ui.screen.appinfo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.SnackbarHost
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.MoreCircle
import top.yukonga.miuix.kmp.menu.OverlayIconDropdownMenu
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.MiuixPopupUtils.Companion.MiuixPopupHost
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import vegabobo.languageselector.R
import vegabobo.languageselector.domain.apps.ModifiedState
import vegabobo.languageselector.ui.components.AppIconImage
import vegabobo.languageselector.ui.components.BackButton
import vegabobo.languageselector.ui.components.BlurredTopBar
import vegabobo.languageselector.ui.components.LocaleChildItem
import vegabobo.languageselector.ui.components.LocaleItemList
import vegabobo.languageselector.ui.components.ModifiedStatusTag
import vegabobo.languageselector.ui.components.rememberAppBlurBackdrop
import vegabobo.languageselector.ui.theme.PageSpacing

@Composable
fun AppInfoScreen(
    appId: String,
    navigateBack: () -> Unit,
    appInfoVm: AppInfoVm = hiltViewModel(),
) {
    val uiState by appInfoVm.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = MiuixScrollBehavior()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
        WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()
    val backdrop = rememberAppBlurBackdrop()
    val barColor = if (backdrop != null) Color.Transparent else MiuixTheme.colorScheme.surface
    val moreEntries = rememberAppActionsEntries(
        onOpen = appInfoVm::onClickOpen,
        onForceClose = appInfoVm::onClickForceClose,
        onSettings = appInfoVm::onClickSettings,
    )
    var localePendingUnpin by remember { mutableStateOf<SingleLocale?>(null) }
    var expandedLanguages by remember { mutableStateOf(setOf<String>()) }
    val pinnedMessage = stringResource(R.string.pinned_ok)
    val unpinnedMessage = stringResource(R.string.unpinned)
    val localeAppliedMessage = stringResource(R.string.locale_applied)
    val localeApplyFailedMessage = stringResource(R.string.locale_apply_failed)
    val appNotLaunchableMessage = stringResource(R.string.app_not_launchable)
    val forceStopCompletedMessage = stringResource(R.string.force_stop_completed)
    val forceStopFailedMessage = stringResource(R.string.force_stop_failed)
    val systemDefault = stringResource(R.string.system_default)

    fun showMessage(message: String) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    fun onLocaleLongPress(locale: SingleLocale) {
        if (uiState.listOfPinnedLanguages.containsLocale(locale)) {
            localePendingUnpin = locale
        } else {
            showMessage(pinnedMessage.format(locale.name))
            appInfoVm.onPinLang(locale)
        }
    }

    LaunchedEffect(appId) {
        appInfoVm.initFromAppId(appId)
        appInfoVm.updatePinnedLangsFromSP()
    }
    LaunchedEffect(appInfoVm) {
        appInfoVm.events.collect { event ->
            when (event) {
                is AppInfoEvent.LocaleApplied -> showMessage(
                    localeAppliedMessage.format(event.localeName ?: systemDefault)
                )
                AppInfoEvent.LocaleApplyFailed -> showMessage(localeApplyFailedMessage)
                AppInfoEvent.LaunchUnavailable -> showMessage(appNotLaunchableMessage)
                AppInfoEvent.ForceStopCompleted -> showMessage(forceStopCompletedMessage)
                AppInfoEvent.ForceStopFailed -> showMessage(forceStopFailedMessage)
            }
        }
    }

    Scaffold(
        topBar = {
            BlurredTopBar(
                backdrop = backdrop,
                progressive = true,
                scrollBehavior = scrollBehavior,
            ) {
                TopAppBar(
                    title = stringResource(R.string.app_language),
                    color = barColor,
                    navigationIcon = { BackButton(navigateBack) },
                    scrollBehavior = scrollBehavior,
                    actions = {
                        OverlayIconDropdownMenu(entry = moreEntries) {
                            Icon(
                                imageVector = MiuixIcons.MoreCircle,
                                contentDescription = stringResource(R.string.more_options)
                            )
                        }
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(state = snackbarHostState) },
        popupHost = {
            Box(Modifier.fillMaxSize()) {
                UnpinLanguageDialog(
                    locale = localePendingUnpin,
                    onDismiss = { localePendingUnpin = null },
                    onConfirm = {
                        val locale = localePendingUnpin
                        localePendingUnpin = null
                        if (locale != null) {
                            showMessage(unpinnedMessage.format(locale.name))
                            appInfoVm.onRemovePin(locale)
                        }
                    }
                )
                MiuixPopupHost()
            }
        },
        contentWindowInsets = WindowInsets.systemBars
            .add(WindowInsets.displayCutout)
            .only(WindowInsetsSides.Horizontal)
    ) { contentPadding ->
        Box(modifier = if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier) {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(
                    top = contentPadding.calculateTopPadding() + PageSpacing.ExtraTop,
                    bottom = bottomInset + 12.dp
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .overScrollVertical()
                    .scrollEndHaptic()
                    .animateContentSize()
            ) {
            item(key = "app-header") {
                AppHeader(uiState)
            }

            if (uiState.listOfPinnedLanguages.isNotEmpty()) {
                item(key = "pinned-title") { SmallTitle(text = stringResource(R.string.pinned)) }
                items(
                    count = uiState.listOfPinnedLanguages.size,
                    key = { index -> "pinned-${uiState.listOfPinnedLanguages[index].languageTag}" }
                ) { index ->
                    val locale = uiState.listOfPinnedLanguages[index]
                    LocaleItemList(
                        itemText = locale.name,
                        selected = locale.languageTag == uiState.currentLanguageTag,
                        enabled = !uiState.isLocaleOperationRunning,
                        onClick = { appInfoVm.onClickLocale(locale) },
                        onLongClick = { localePendingUnpin = locale }
                    )
                }
            }

            item(key = "user-languages-title") { SmallTitle(text = stringResource(R.string.user_languages)) }
            item(key = "system-default") {
                LocaleItemList(
                    itemText = stringResource(R.string.system_default),
                    selected = uiState.currentLanguageTag.isEmpty(),
                    enabled = !uiState.isLocaleOperationRunning,
                    onClick = { appInfoVm.onClickResetLang() }
                )
            }
            items(
                count = uiState.listOfSuggestedLanguages.size,
                key = { index -> "suggested-${uiState.listOfSuggestedLanguages[index].languageTag}" }
            ) { index ->
                val locale = uiState.listOfSuggestedLanguages[index]
                LocaleItemList(
                    itemText = locale.name,
                    selected = locale.languageTag == uiState.currentLanguageTag,
                    enabled = !uiState.isLocaleOperationRunning,
                    onClick = { appInfoVm.onClickLocale(locale) },
                    onLongClick = { onLocaleLongPress(locale) }
                )
            }

            item(key = "all-languages-title") { SmallTitle(text = stringResource(R.string.all_languages)) }
            items(
                count = uiState.listOfAllLanguages.size,
                key = { index -> "all-language-${uiState.listOfAllLanguages[index].language}" }
            ) { index ->
                val language = uiState.listOfAllLanguages[index]
                if (!language.hasMultipleSelections()) {
                    LocaleItemList(
                        itemText = language.language,
                        selected = language.locales.any {
                            it.languageTag == uiState.currentLanguageTag
                        },
                        enabled = !uiState.isLocaleOperationRunning,
                        onClick = {
                            language.locales.firstOrNull()?.let(appInfoVm::onClickLocale)
                        },
                        onLongClick = {
                            language.pinLocale()?.let(::onLocaleLongPress)
                        }
                    )
                } else {
                    val expanded = expandedLanguages.contains(language.language)
                    val expandedVisibilityState = remember {
                        MutableTransitionState(expanded)
                    }
                    expandedVisibilityState.targetState = expanded
                    Column(modifier = Modifier.fillMaxWidth()) {
                        LocaleItemList(
                            itemText = language.language,
                            selected = language.locales.any {
                                it.languageTag == uiState.currentLanguageTag
                            },
                            enabled = !uiState.isLocaleOperationRunning,
                            showArrow = true,
                            onClick = {
                                expandedLanguages = if (expanded) {
                                    expandedLanguages - language.language
                                } else {
                                    expandedLanguages + language.language
                                }
                            },
                            onLongClick = {
                                language.pinLocale()?.let(::onLocaleLongPress)
                            }
                        )
                        AnimatedVisibility(
                            visibleState = expandedVisibilityState,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                Spacer(Modifier.height(9.dp))
                                language.locales.forEach { locale ->
                                    LocaleChildItem(
                                        itemText = locale.name,
                                        selected = locale.languageTag == uiState.currentLanguageTag,
                                        enabled = !uiState.isLocaleOperationRunning,
                                        onClick = { appInfoVm.onClickLocale(locale) },
                                        onLongClick = { onLocaleLongPress(locale) }
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                            }
                        }
                    }
                }
            }
            item(key = "bottom-spacer") { Spacer(Modifier.size(1.dp)) }
            }
        }
    }
}

@Composable
private fun UnpinLanguageDialog(
    locale: SingleLocale?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val show = locale != null
    val backState = rememberNavigationEventState(NavigationEventInfo.None)
    NavigationBackHandler(
        state = backState,
        isBackEnabled = show,
        onBackCompleted = onDismiss
    )

    OverlayDialog(
        show = show,
        onDismissRequest = onDismiss,
        title = locale?.let { stringResource(R.string.unpin_title, it.name) },
        summary = stringResource(R.string.unpin_message)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.cancel),
                onClick = onDismiss
            )
            TextButton(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.unpin),
                colors = ButtonDefaults.textButtonColorsPrimary(),
                onClick = onConfirm
            )
        }
    }
}

@Composable
private fun AppHeader(state: AppInfoState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(bottom = 12.dp),
        insideMargin = PaddingValues(start = 12.dp, end = 16.dp, top = 10.dp, bottom = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (state.applicationInfo != null) {
                AppIconImage(
                    applicationInfo = state.applicationInfo,
                    size = 64.dp,
                    modifier = Modifier.size(64.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MiuixTheme.colorScheme.secondaryContainer)
                )
            }
            Column(
                modifier = Modifier
                    .padding(start = 12.dp, end = 8.dp)
                    .weight(1f)
            ) {
                Text(
                    text = state.appName,
                    color = MiuixTheme.colorScheme.onSurface,
                    fontWeight = FontWeight(550),
                    modifier = Modifier.basicMarquee(),
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = state.currentLanguage.ifEmpty { stringResource(R.string.system_default) },
                    fontSize = 12.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.basicMarquee(),
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = state.appPackage,
                    fontSize = 12.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.basicMarquee(),
                    maxLines = 1,
                    softWrap = false
                )
            }
            if (state.modifiedState == ModifiedState.Modified) {
                ModifiedStatusTag()
            }
        }
    }
}

@Composable
private fun rememberAppActionsEntries(
    onOpen: () -> Unit,
    onForceClose: () -> Unit,
    onSettings: () -> Unit,
): DropdownEntry {
    val launch = stringResource(R.string.launch)
    val forceStop = stringResource(R.string.force_stop)
    val settings = stringResource(R.string.settings)
    val currentOnOpen = rememberUpdatedState(onOpen)
    val currentOnForceClose = rememberUpdatedState(onForceClose)
    val currentOnSettings = rememberUpdatedState(onSettings)
    return remember(launch, forceStop, settings) {
        DropdownEntry(
            items = listOf(
                DropdownItem(text = launch, onClick = { currentOnOpen.value() }),
                DropdownItem(text = forceStop, onClick = { currentOnForceClose.value() }),
                DropdownItem(text = settings, onClick = { currentOnSettings.value() }),
            ),
        )
    }
}
