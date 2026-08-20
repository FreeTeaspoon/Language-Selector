package vegabobo.languageselector.ui.screen.appinfo

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.LocaleList
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vegabobo.languageselector.BuildConfig
import vegabobo.languageselector.LocaleManager
import vegabobo.languageselector.data.apps.AppRepository
import vegabobo.languageselector.data.locales.LocaleRepository
import vegabobo.languageselector.domain.apps.ModifiedState
import java.util.Locale
import javax.inject.Inject

object PrefConstants {
    const val PINNED_LOCALES = "pinned_locales"
}

@HiltViewModel
class AppInfoVm @Inject constructor(
    private val app: Application,
    private val localeManager: LocaleManager,
    private val appRepository: AppRepository,
    private val localeRepository: LocaleRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AppInfoState())
    val uiState: StateFlow<AppInfoState> = _uiState.asStateFlow()

    private lateinit var appInfo: ApplicationInfo

    fun initFromAppId(appId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val loadedApp = runCatching { appRepository.loadApp(appId) }.getOrNull() ?: return@launch
            appInfo = loadedApp.applicationInfo
            _uiState.update {
                it.copy(
                    appName = loadedApp.name,
                    appPackage = loadedApp.pkg,
                    applicationInfo = loadedApp.applicationInfo,
                    modifiedState = loadedApp.modifiedState,
                    listOfAllLanguages = localeManager.localeList
                )
            }
            updateSuggestedLanguages()
            updateCurrentLanguageStateInternal()
        }
    }

    private suspend fun updateSuggestedLanguages() {
        val systemLocales = localeRepository.getSystemLocales() ?: return
        val suggestedLanguages = (0 until systemLocales.size()).map { index ->
            val locale = systemLocales[index]
            SingleLocale(locale.capDisplayName(), locale.toLanguageTag())
        }.toMutableList()
        _uiState.update { it.copy(listOfSuggestedLanguages = suggestedLanguages) }
    }

    fun updateCurrentLanguageState() {
        viewModelScope.launch(Dispatchers.IO) {
            updateCurrentLanguageStateInternal()
        }
    }

    private suspend fun updateCurrentLanguageStateInternal() {
        if (!::appInfo.isInitialized) return
        val currentLocale = localeRepository.getApplicationLocales(appInfo.packageName)
        _uiState.update {
            when {
                currentLocale == null -> it.copy(modifiedState = ModifiedState.Unavailable)
                currentLocale.isEmpty -> it.copy(
                    currentLanguage = "",
                    currentLanguageTag = "",
                    modifiedState = ModifiedState.Unmodified
                )
                else -> it.copy(
                    currentLanguage = currentLocale[0].capDisplayName(),
                    currentLanguageTag = currentLocale[0].toLanguageTag(),
                    modifiedState = ModifiedState.Modified
                )
            }
        }
    }

    fun onClickLocale(singleLocale: SingleLocale) {
        if (!::appInfo.isInitialized) return
        viewModelScope.launch(Dispatchers.IO) {
            localeRepository.setApplicationLocales(
                appInfo.packageName,
                LocaleList(singleLocale.toLocale())
            )
            updateCurrentLanguageStateInternal()
        }
    }

    fun onClickSettings() {
        if (!::appInfo.isInitialized) return
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", appInfo.packageName, null)
        intent.data = uri
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        app.startActivity(intent)
    }

    fun onClickOpen() {
        if (!::appInfo.isInitialized) return
        val launchIntent = app.packageManager.getLaunchIntentForPackage(appInfo.packageName)
        launchIntent?.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        launchIntent?.let(app::startActivity)
    }

    fun onClickResetLang() {
        if (!::appInfo.isInitialized) return
        viewModelScope.launch(Dispatchers.IO) {
            localeRepository.setApplicationLocales(appInfo.packageName, LocaleList())
            _uiState.update {
                it.copy(
                    currentLanguage = "",
                    currentLanguageTag = "",
                    modifiedState = ModifiedState.Unmodified
                )
            }
            updateCurrentLanguageStateInternal()
        }
    }

    fun onClickForceClose() {
        if (!::appInfo.isInitialized) return
        viewModelScope.launch(Dispatchers.IO) {
            localeRepository.forceStopPackage(appInfo.packageName)
        }
    }

    fun getSp(): SharedPreferences =
        app.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)

    fun onPinLang(singleLocale: SingleLocale) {
        val sp = getSp()
        val set = sp.getStringSet(PrefConstants.PINNED_LOCALES, emptySet()) ?: emptySet()
        val mset = set.toMutableSet()
        mset.add("${singleLocale.name},${singleLocale.languageTag}")
        sp.edit().putStringSet(PrefConstants.PINNED_LOCALES, mset).apply()
        updatePinnedLangsFromSP()
    }

    fun onRemovePin(singleLocale: SingleLocale) {
        val sp = getSp()
        val set = sp.getStringSet(PrefConstants.PINNED_LOCALES, emptySet()) ?: emptySet()
        val newSet = mutableSetOf<String>()
        set.forEach {
            if (!it.contains(singleLocale.languageTag)) {
                newSet.add(it)
            }
        }
        sp.edit().putStringSet(PrefConstants.PINNED_LOCALES, newSet).apply()
        updatePinnedLangsFromSP()
    }

    fun updatePinnedLangsFromSP() {
        val sp = getSp()
        val set = sp.getStringSet(PrefConstants.PINNED_LOCALES, emptySet()) ?: return
        val pinnedLocaleList = set.parseSetLangs()
        _uiState.update { it.copy(listOfPinnedLanguages = pinnedLocaleList) }
    }
}

fun Locale.capDisplayName(): String {
    return this.getDisplayName(this).replaceFirstChar { it.uppercaseChar() }
}

fun Set<String>.parseSetLangs(): MutableList<SingleLocale> {
    return this.mapNotNull {
        try {
            val stringLocale = it.split(",")
            val name = stringLocale[0]
            val tag = stringLocale[1]
            SingleLocale(name, tag)
        } catch (e: Exception) {
            Log.e(BuildConfig.APPLICATION_ID, e.stackTraceToString())
            null
        }
    }.toMutableList()
}
