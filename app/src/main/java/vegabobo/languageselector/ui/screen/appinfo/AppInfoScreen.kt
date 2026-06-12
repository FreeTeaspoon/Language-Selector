package vegabobo.languageselector.ui.screen.appinfo

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.MoreCircle
import top.yukonga.miuix.kmp.overlay.OverlayListPopup
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import vegabobo.languageselector.R
import vegabobo.languageselector.domain.apps.ModifiedState
import vegabobo.languageselector.ui.components.AppDropdownItem
import vegabobo.languageselector.ui.components.AppIconImage
import vegabobo.languageselector.ui.components.AppPopupDefaults
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
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val scrollBehavior = MiuixScrollBehavior()
    val selectedLanguageBackState = rememberNavigationEventState(NavigationEventInfo.None)
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
        WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()
    var showMorePopup by remember { mutableStateOf(false) }
    val backdrop = rememberAppBlurBackdrop()
    val barColor = if (backdrop != null) Color.Transparent else MiuixTheme.colorScheme.surface

    fun pinToast(locale: String) {
        Toast.makeText(
            context,
            context.getString(R.string.pinned_ok).format(locale),
            Toast.LENGTH_SHORT
        ).show()
    }

    fun unpinToast(locale: String) {
        Toast.makeText(
            context,
            context.getString(R.string.unpinned).format(locale),
            Toast.LENGTH_SHORT
        ).show()
    }

    LaunchedEffect(appId) {
        appInfoVm.initFromAppId(appId)
        appInfoVm.updatePinnedLangsFromSP()
    }

    Scaffold(
        topBar = {
            BlurredTopBar(backdrop = backdrop) {
                TopAppBar(
                    title = stringResource(R.string.app_language),
                    color = barColor,
                    navigationIcon = { BackButton(navigateBack) },
                    scrollBehavior = scrollBehavior,
                    actions = {
                        Box {
                            OverlayListPopup(
                                show = showMorePopup,
                                popupPositionProvider = AppPopupDefaults.MenuPositionProvider,
                                alignment = PopupPositionProvider.Align.TopEnd,
                                onDismissRequest = { showMorePopup = false }
                            ) {
                                val labels = listOf(
                                    stringResource(R.string.open),
                                    stringResource(R.string.close),
                                    stringResource(R.string.settings)
                                )
                                ListPopupColumn {
                                    labels.forEachIndexed { index, label ->
                                        AppDropdownItem(
                                            text = label,
                                            optionSize = labels.size,
                                            index = index,
                                            onClick = {
                                                showMorePopup = false
                                                when (index) {
                                                    0 -> appInfoVm.onClickOpen()
                                                    1 -> appInfoVm.onClickForceClose()
                                                    else -> appInfoVm.onClickSettings()
                                                }
                                            }
                                        )
                                    }
                                }
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
                    }
                )
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
                    top = contentPadding.calculateTopPadding(),
                    bottom = bottomInset + 12.dp
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp)
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
                        onLongClick = {
                            pinToast(locale.name)
                            appInfoVm.onPinLang(locale)
                        }
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
                            onLongClick = {
                                unpinToast(locale.name)
                                appInfoVm.onRemovePin(locale)
                            }
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
                        onLongClick = {
                            pinToast(locale.name)
                            appInfoVm.onPinLang(locale)
                        }
                    )
                }

                item { SmallTitle(text = stringResource(R.string.all_languages)) }
                items(uiState.listOfAllLanguages.size) { index ->
                    val locale = uiState.listOfAllLanguages[index]
                    LocaleItemList(locale.language) {
                        appInfoVm.onClickSingleLanguage(index)
                        coroutineScope.launch { listState.scrollToItem(0) }
                    }
                }
            }
            item { Spacer(Modifier.size(1.dp)) }
            }
        }
    }

    NavigationBackHandler(
        state = selectedLanguageBackState,
        isBackEnabled = uiState.selectedLanguage != -1,
        onBackCompleted = appInfoVm::onBackWhenSelectedLang
    )
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
