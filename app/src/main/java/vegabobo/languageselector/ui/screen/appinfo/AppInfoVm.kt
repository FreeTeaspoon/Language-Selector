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
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    // Kept as a compatibility mirror for versions that predate ordered storage.
    const val PINNED_LOCALES = "pinned_locales"
    const val PINNED_LOCALES_ORDERED = "pinned_locales_ordered"
}

private val pinnedLocalesJson = Json

sealed interface AppInfoEvent {
    data class LocaleApplied(val localeName: String?) : AppInfoEvent
    data object LocaleApplyFailed : AppInfoEvent
    data object LaunchUnavailable : AppInfoEvent
    data object ForceStopCompleted : AppInfoEvent
    data object ForceStopFailed : AppInfoEvent
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
    private val _events = MutableSharedFlow<AppInfoEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

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
        applyApplicationLocales(
            locales = LocaleList(singleLocale.toLocale()),
            localeName = singleLocale.name
        )
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
        val launchIntent = runCatching {
            app.packageManager.getLaunchIntentForPackage(appInfo.packageName)
        }.getOrNull()
        if (launchIntent == null) {
            emitEvent(AppInfoEvent.LaunchUnavailable)
            return
        }
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { app.startActivity(launchIntent) }
            .onFailure { error ->
                Log.w(
                    BuildConfig.APPLICATION_ID,
                    "Unable to launch ${appInfo.packageName}",
                    error
                )
                emitEvent(AppInfoEvent.LaunchUnavailable)
            }
    }

    fun onClickResetLang() {
        applyApplicationLocales(locales = LocaleList(), localeName = null)
    }

    private fun applyApplicationLocales(locales: LocaleList, localeName: String?) {
        if (!::appInfo.isInitialized || _uiState.value.isLocaleOperationRunning) return
        val previousModifiedState = _uiState.value.modifiedState
        val optimisticModifiedState = if (locales.isEmpty) {
            ModifiedState.Unmodified
        } else {
            ModifiedState.Modified
        }
        _uiState.update {
            it.copy(
                isLocaleOperationRunning = true,
                modifiedState = optimisticModifiedState
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val succeeded = runCatching {
                    localeRepository.setApplicationLocales(appInfo.packageName, locales)
                        .also { if (it) updateCurrentLanguageStateInternal() }
                }.getOrDefault(false)
                if (succeeded) {
                    _events.emit(AppInfoEvent.LocaleApplied(localeName))
                } else {
                    _uiState.update { it.copy(modifiedState = previousModifiedState) }
                    _events.emit(AppInfoEvent.LocaleApplyFailed)
                }
            } finally {
                _uiState.update { it.copy(isLocaleOperationRunning = false) }
            }
        }
    }

    fun onClickForceClose() {
        if (!::appInfo.isInitialized) return
        viewModelScope.launch(Dispatchers.IO) {
            val completed = runCatching {
                localeRepository.forceStopPackage(appInfo.packageName)
            }.onFailure { error ->
                Log.w(
                    BuildConfig.APPLICATION_ID,
                    "Unable to force stop ${appInfo.packageName}",
                    error
                )
            }.getOrDefault(false)
            _events.emit(
                if (completed) {
                    AppInfoEvent.ForceStopCompleted
                } else {
                    AppInfoEvent.ForceStopFailed
                }
            )
        }
    }

    private fun emitEvent(event: AppInfoEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }

    fun getSp(): SharedPreferences =
        app.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)

    fun onPinLang(singleLocale: SingleLocale) {
        val sp = getSp()
        val pinnedLocales = sp.getPinnedLocales()
        if (pinnedLocales.none { it.languageTag == singleLocale.languageTag }) {
            pinnedLocales.add(singleLocale)
            sp.setPinnedLocales(pinnedLocales)
        }
        updatePinnedLangsFromSP()
    }

    fun onRemovePin(singleLocale: SingleLocale) {
        val sp = getSp()
        val remainingLocales = sp.getPinnedLocales()
            .filterNot { it.languageTag == singleLocale.languageTag }
        sp.setPinnedLocales(remainingLocales)
        updatePinnedLangsFromSP()
    }

    fun updatePinnedLangsFromSP() {
        val sp = getSp()
        val pinnedLocaleList = sp.getPinnedLocales()
        _uiState.update { it.copy(listOfPinnedLanguages = pinnedLocaleList) }
    }
}

fun SharedPreferences.getPinnedLocales(): MutableList<SingleLocale> {
    val storedLocales = getString(PrefConstants.PINNED_LOCALES_ORDERED, null)
        ?.let { serialized ->
            runCatching {
                pinnedLocalesJson.decodeFromString<List<SingleLocale>>(serialized)
            }.getOrNull()
        }
    if (storedLocales != null) {
        return storedLocales.toMutableList()
    }

    val legacyLocales = getStringSet(PrefConstants.PINNED_LOCALES, emptySet()).orEmpty()
    val migratedLocales = legacyLocales.parseSetLangs().sortPinnedLocalesForMigration()
    setPinnedLocales(migratedLocales)
    return migratedLocales
}

fun SharedPreferences.setPinnedLocales(locales: List<SingleLocale>) {
    val legacyValues = locales.map { "${it.name},${it.languageTag}" }.toSet()
    edit()
        .putString(PrefConstants.PINNED_LOCALES_ORDERED, pinnedLocalesJson.encodeToString(locales))
        .putStringSet(PrefConstants.PINNED_LOCALES, legacyValues)
        .apply()
}

fun Locale.capDisplayName(): String {
    return this.getDisplayName(this)
        .normalizeLocaleDisplayName()
        .replaceFirstChar { it.uppercaseChar() }
}

private val missingDisplayNameCommaSpace = Regex(",(?=\\S)")

private fun String.normalizeLocaleDisplayName(): String =
    replace(missingDisplayNameCommaSpace, ", ")

fun Iterable<String>.parseSetLangs(): MutableList<SingleLocale> {
    return this.mapNotNull {
        try {
            val separatorIndex = it.lastIndexOf(',')
            require(separatorIndex > 0 && separatorIndex < it.lastIndex)
            val name = it.substring(0, separatorIndex).normalizeLocaleDisplayName()
            val tag = it.substring(separatorIndex + 1)
            SingleLocale(name, tag)
        } catch (e: Exception) {
            Log.e(BuildConfig.APPLICATION_ID, e.stackTraceToString())
            null
        }
    }.toMutableList()
}

internal fun List<SingleLocale>.sortPinnedLocalesForMigration(): MutableList<SingleLocale> =
    sortedWith(
        compareBy<SingleLocale> { it.name.lowercase(Locale.ROOT) }
            .thenBy { it.languageTag }
    ).toMutableList()

fun Set<String>.withoutLocale(singleLocale: SingleLocale): MutableSet<String> =
    filterTo(mutableSetOf()) {
        it.substringAfterLast(',') != singleLocale.languageTag
    }
