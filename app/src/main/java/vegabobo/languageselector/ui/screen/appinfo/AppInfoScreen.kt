package vegabobo.languageselector.ui.screen.appinfo

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import vegabobo.languageselector.R
import vegabobo.languageselector.ui.components.AppIconImage
import vegabobo.languageselector.ui.components.BackButton
import vegabobo.languageselector.ui.components.LocaleItemList
import vegabobo.languageselector.ui.components.QuickTextButton
import vegabobo.languageselector.ui.components.Title
import vegabobo.languageselector.ui.screen.BaseScreen
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun AppInfoScreen(
    appId: String,
    navigateBack: () -> Unit,
    appInfoVm: AppInfoVm = hiltViewModel(),
) {
    val uiState by appInfoVm.uiState.collectAsState()
    val ctx = LocalContext.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val selectedLanguageBackState = rememberNavigationEventState(NavigationEventInfo.None)

    fun pinToast(locale: String) {
        val pinTxt =
            ctx.resources.getString(R.string.pinned_ok).format(locale)
        Toast.makeText(ctx, pinTxt, Toast.LENGTH_SHORT).show()
    }

    fun unpinToast(locale: String) {
        val pinTxt =
            ctx.resources.getString(R.string.unpinned).format(locale)
        Toast.makeText(ctx, pinTxt, Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(Unit) {
        appInfoVm.initFromAppId(appId)
        appInfoVm.updatePinnedLangsFromSP()
    }
    BaseScreen(
        title = stringResource(R.string.app_language),
        navIcon = {
            BackButton { navigateBack() }
        }
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .padding(top = it.calculateTopPadding())
                .animateContentSize(),
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 18.dp, end = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (uiState.applicationInfo != null) {
                        AppIconImage(
                            modifier = Modifier.size(84.dp),
                            applicationInfo = uiState.applicationInfo!!,
                            label = uiState.appName,
                            size = 84.dp
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(MiuixTheme.colorScheme.secondaryContainer)
                        )
                    }
                    Column(
                        modifier = Modifier
                            .padding(18.dp)
                            .weight(1f)
                    ) {
                        Text(text = uiState.appName, fontSize = 22.sp, maxLines = 1)
                        Text(text = uiState.appPackage, fontSize = 14.sp, maxLines = 1)
                        Text(
                            text = uiState.currentLanguage.ifEmpty { stringResource(R.string.system_default) },
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    QuickTextButton(
                        modifier = Modifier.weight(1f),
                        onClick = { appInfoVm.onClickOpen() },
                        icon = Icons.AutoMirrored.Outlined.OpenInNew,
                        text = stringResource(R.string.open)
                    )
                    QuickTextButton(
                        modifier = Modifier.weight(1f),
                        onClick = { appInfoVm.onClickForceClose() },
                        icon = Icons.Outlined.Close,
                        text = stringResource(R.string.close)
                    )
                    QuickTextButton(
                        modifier = Modifier.weight(1f),
                        onClick = { appInfoVm.onClickSettings() },
                        icon = Icons.Outlined.Settings,
                        text = stringResource(R.string.settings)
                    )
                }
            }

            if (uiState.selectedLanguage != -1) {
                item { Title(stringResource(R.string.region)) }
                items(uiState.listOfAllLanguages[uiState.selectedLanguage].locales.size) { index ->
                    val thisLangReg =
                        uiState.listOfAllLanguages[uiState.selectedLanguage].locales[index]
                    LocaleItemList(
                        itemText = thisLangReg.name,
                        onClick = {
                            appInfoVm.onClickLocale(thisLangReg)
                            appInfoVm.onBackWhenSelectedLang()
                            coroutineScope.launch { listState.scrollToItem(0) }
                        },
                        onLongClick = {
                            pinToast(thisLangReg.name)
                            appInfoVm.onPinLang(thisLangReg)
                        }
                    )
                }
            } else {
                if (uiState.listOfPinnedLanguages.size != 0) {
                    item { Title(stringResource(R.string.pinned)) }
                    items(uiState.listOfPinnedLanguages.size) { index ->
                        val thisLanguage = uiState.listOfPinnedLanguages[index]
                        LocaleItemList(
                            itemText = thisLanguage.name,
                            onClick = { appInfoVm.onClickLocale(thisLanguage) },
                            onLongClick = {
                                unpinToast(thisLanguage.name)
                                appInfoVm.onRemovePin(thisLanguage)
                            }
                        )
                    }
                }

                item { Title(stringResource(R.string.user_languages)) }
                item {
                    LocaleItemList(stringResource(R.string.system_default)) { appInfoVm.onClickResetLang() }
                }
                items(uiState.listOfSuggestedLanguages.size) { index ->
                    val thisLanguage = uiState.listOfSuggestedLanguages[index]
                    LocaleItemList(
                        itemText = thisLanguage.name,
                        onClick = { appInfoVm.onClickLocale(thisLanguage) },
                        onLongClick = {
                            pinToast(thisLanguage.name)
                            appInfoVm.onPinLang(thisLanguage)
                        }
                    )
                }

                item { Title(stringResource(R.string.all_languages)) }
                items(uiState.listOfAllLanguages.size) { index ->
                    val thisLanguage = uiState.listOfAllLanguages[index]
                    LocaleItemList(thisLanguage.language) {
                        appInfoVm.onClickSingleLanguage(index)
                        coroutineScope.launch { listState.scrollToItem(0) }
                    }
                }
            }
            item { Spacer(modifier = Modifier.padding(it.calculateBottomPadding())) }
        }
    }

    NavigationBackHandler(
        state = selectedLanguageBackState,
        isBackEnabled = uiState.selectedLanguage != -1,
        onBackCompleted = { appInfoVm.onBackWhenSelectedLang() }
    )

}
