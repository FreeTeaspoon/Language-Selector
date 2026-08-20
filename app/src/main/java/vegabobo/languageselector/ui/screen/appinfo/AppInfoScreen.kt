package vegabobo.languageselector.ui.screen.appinfo

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import vegabobo.languageselector.ui.components.LocaleItemList
import vegabobo.languageselector.ui.components.ModifiedStatusTag
import vegabobo.languageselector.ui.components.rememberAppBlurBackdrop

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
    val selectedLanguageBackState = rememberNavigationEventState(NavigationEventInfo.None)
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
    val pinnedMessage = stringResource(R.string.pinned_ok)
    val unpinnedMessage = stringResource(R.string.unpinned)

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
                    top = contentPadding.calculateTopPadding() + 6.dp,
                    bottom = bottomInset + 12.dp
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .overScrollVertical()
                    .scrollEndHaptic()
                    .animateContentSize()
            ) {
            item {
                AppHeader(uiState)
            }

            if (uiState.selectedLanguage != -1) {
                item { SmallTitle(text = stringResource(R.string.region)) }
                items(uiState.listOfAllLanguages[uiState.selectedLanguage].locales.size) { index ->
                    val locale = uiState.listOfAllLanguages[uiState.selectedLanguage].locales[index]
                    LocaleItemList(
                        itemText = locale.name,
                        onClick = {
                            appInfoVm.onClickLocale(locale)
                            appInfoVm.onBackWhenSelectedLang()
                            coroutineScope.launch { listState.scrollToItem(0) }
                        },
                        onLongClick = { onLocaleLongPress(locale) }
                    )
                }
            } else {
                if (uiState.listOfPinnedLanguages.isNotEmpty()) {
                    item { SmallTitle(text = stringResource(R.string.pinned)) }
                    items(uiState.listOfPinnedLanguages.size) { index ->
                        val locale = uiState.listOfPinnedLanguages[index]
                        LocaleItemList(
                            itemText = locale.name,
                            onClick = { appInfoVm.onClickLocale(locale) },
                            onLongClick = { localePendingUnpin = locale }
                        )
                    }
                }

                item { SmallTitle(text = stringResource(R.string.user_languages)) }
                item {
                    LocaleItemList(stringResource(R.string.system_default)) {
                        appInfoVm.onClickResetLang()
                    }
                }
                items(uiState.listOfSuggestedLanguages.size) { index ->
                    val locale = uiState.listOfSuggestedLanguages[index]
                    LocaleItemList(
                        itemText = locale.name,
                        onClick = { appInfoVm.onClickLocale(locale) },
                        onLongClick = { onLocaleLongPress(locale) }
                    )
                }

                item { SmallTitle(text = stringResource(R.string.all_languages)) }
                items(uiState.listOfAllLanguages.size) { index ->
                    val language = uiState.listOfAllLanguages[index]
                    LocaleItemList(
                        itemText = language.language,
                        onClick = {
                            appInfoVm.onClickSingleLanguage(index)
                            coroutineScope.launch { listState.scrollToItem(0) }
                        },
                        onLongClick = {
                            language.pinLocale()?.let(::onLocaleLongPress)
                        }
                    )
                }
            }
            item { Spacer(Modifier.size(1.dp)) }
            }
        }
    }

    NavigationBackHandler(
        state = selectedLanguageBackState,
        isBackEnabled = uiState.selectedLanguage != -1 && localePendingUnpin == null,
        onBackCompleted = appInfoVm::onBackWhenSelectedLang
    )
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
        title = locale?.let { stringResource(R.string.unpin_title, it.name) }
    ) {
        Text(
            text = stringResource(R.string.unpin_message),
            color = MiuixTheme.colorScheme.onSurface
        )
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
        insideMargin = PaddingValues(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (state.applicationInfo != null) {
                AppIconImage(
                    applicationInfo = state.applicationInfo,
                    label = state.appName,
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
                    .padding(start = 14.dp)
                    .weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = state.appName,
                        style = MiuixTheme.textStyles.title2,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (state.modifiedState == ModifiedState.Modified) {
                        ModifiedStatusTag(modifier = Modifier.padding(start = 8.dp))
                    }
                }
                Text(
                    text = state.currentLanguage.ifEmpty { stringResource(R.string.system_default) },
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = state.appPackage,
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
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
