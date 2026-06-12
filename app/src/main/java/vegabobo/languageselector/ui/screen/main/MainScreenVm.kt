package vegabobo.languageselector.ui.screen.main

import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.SystemClock
import android.util.Log
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.topjohnwu.superuser.Shell
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku
import vegabobo.languageselector.BuildConfig
import vegabobo.languageselector.RootReceivedListener
import vegabobo.languageselector.dao.AppInfoDb
import vegabobo.languageselector.data.apps.AppRepository
import vegabobo.languageselector.data.locales.LocaleRepository
import vegabobo.languageselector.domain.apps.AppInfo
import vegabobo.languageselector.domain.apps.AppListLogic
import vegabobo.languageselector.domain.apps.AppListPreferences
import vegabobo.languageselector.domain.apps.AppSortField
import vegabobo.languageselector.domain.apps.LoadAppsUseCase
import vegabobo.languageselector.domain.apps.ModifiedState
import vegabobo.languageselector.domain.apps.RefreshAppLocaleStatesUseCase
import vegabobo.languageselector.service.UserServiceConnector
import javax.inject.Inject

@HiltViewModel
class MainScreenVm @Inject constructor(
    private val loadAppsUseCase: LoadAppsUseCase,
    private val refreshAppLocaleStatesUseCase: RefreshAppLocaleStatesUseCase,
    private val appRepository: AppRepository,
    private val localeRepository: LocaleRepository,
    private val preferences: SharedPreferences,
    appInfoDb: AppInfoDb
) : ViewModel() {
    private companion object {
        const val PERF_TAG = "LanguageSelectorPerf"
        const val PREF_SORT_FIELD = "apps_sort_field"
        const val PREF_SORT_DESCENDING = "apps_sort_descending"
        const val PREF_SHOW_SYSTEM = "apps_show_system"
        const val PREF_MODIFIED_ONLY = "apps_modified_only"
    }

    private val _uiState = MutableStateFlow(
        MainScreenState(preferences = readPreferences())
    )
    val uiState: StateFlow<MainScreenState> = _uiState.asStateFlow()
    private val dao = appInfoDb.appInfoDao()
    private var refreshJob: Job? = null
    private var connectionRefreshJob: Job? = null
    var lastSelectedApp: AppInfo? = null

    init {
        loadApps(userRefresh = false)
        viewModelScope.launch(Dispatchers.IO) {
            UserServiceConnector.connection
                .filterNotNull()
                .map { it.mode }
                .distinctUntilChanged()
                .collect { connectedMode ->
                    val state = _uiState.value
                    if (
                        state.listOfApps.isNotEmpty() &&
                        state.operationMode != connectedMode &&
                        refreshJob?.isActive != true &&
                        connectionRefreshJob?.isActive != true
                    ) {
                        connectionRefreshJob = viewModelScope.launch(Dispatchers.IO) {
                            refreshLocaleStates()
                        }
                    }
                }
        }
    }

    fun refresh() = loadApps(userRefresh = true)

    private fun loadApps(userRefresh: Boolean) {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch(Dispatchers.IO) {
            if (userRefresh) _uiState.update { it.copy(isRefreshing = true) }
            try {
                val metadataStart = SystemClock.elapsedRealtime()
                val loaded = loadAppsUseCase()
                val previousStates = _uiState.value.listOfApps.associate { it.pkg to it.modifiedState }
                val apps = loaded.map { app ->
                    app.copy(modifiedState = previousStates[app.pkg] ?: ModifiedState.Unknown)
                }
                logPerf("metadata load: ${apps.size} apps in ${SystemClock.elapsedRealtime() - metadataStart}ms")
                updateDerived { it.copy(listOfApps = apps, isLoading = false) }
                refreshLocaleStates()
            } finally {
                _uiState.update { it.copy(isRefreshing = false, isLoading = false) }
            }
        }
    }

    private suspend fun refreshLocaleStates() {
        val modeStart = SystemClock.elapsedRealtime()
        val operationMode = resolveOperationMode()
        logPerf("service mode resolved: $operationMode in ${SystemClock.elapsedRealtime() - modeStart}ms")
        _uiState.update {
            it.copy(operationMode = operationMode, isOperationModeResolved = true)
        }

        val currentApps = _uiState.value.listOfApps
        if (currentApps.isEmpty()) return
        if (operationMode == OperationMode.NONE) {
            updateDerived { state ->
                state.copy(
                    listOfApps = state.listOfApps.map {
                        it.copy(modifiedState = ModifiedState.Unavailable)
                    },
                    isLocaleRefreshRunning = false
                )
            }
            return
        }

        _uiState.update { it.copy(isLocaleRefreshRunning = true) }
        val refreshStart = SystemClock.elapsedRealtime()
        var refreshedApps = 0
        try {
            refreshAppLocaleStatesUseCase(currentApps).collect { updates ->
                refreshedApps += updates.size
                val byPackage = updates.associateBy { it.pkg }
                updateDerived { state ->
                    state.copy(
                        listOfApps = state.listOfApps.map { byPackage[it.pkg] ?: it }
                    )
                }
            }
        } finally {
            _uiState.update { it.copy(isLocaleRefreshRunning = false) }
        }
        logPerf("locale refresh complete: $refreshedApps apps in ${SystemClock.elapsedRealtime() - refreshStart}ms")
    }

    private suspend fun resolveOperationMode(): OperationMode {
        UserServiceConnector.currentMode().takeIf { it != OperationMode.NONE }?.let { return it }
        val hasShizuku = runCatching {
            Shizuku.pingBinder() &&
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        }.getOrDefault(false)
        if (hasShizuku) return OperationMode.SHIZUKU

        val hasRoot = runCatching {
            Shell.getShell()
            Shell.isAppGrantedRoot() == true
        }.getOrDefault(false)
        if (hasRoot) {
            RootReceivedListener.onRootReceived()
            return OperationMode.ROOT
        }
        return OperationMode.NONE
    }

    fun updateSortField(field: AppSortField) {
        updatePreferences(_uiState.value.preferences.copy(sortField = field))
    }

    fun toggleSortDirection() {
        val current = _uiState.value.preferences
        updatePreferences(current.copy(descending = !current.descending))
    }

    fun toggleSystemAppsVisibility() {
        val current = _uiState.value.preferences
        updatePreferences(current.copy(showSystemApps = !current.showSystemApps))
    }

    fun toggleModifiedOnly() {
        val current = _uiState.value.preferences
        updatePreferences(current.copy(modifiedOnly = !current.modifiedOnly))
    }

    private fun updatePreferences(value: AppListPreferences) {
        preferences.edit()
            .putString(PREF_SORT_FIELD, value.sortField.name)
            .putBoolean(PREF_SORT_DESCENDING, value.descending)
            .putBoolean(PREF_SHOW_SYSTEM, value.showSystemApps)
            .putBoolean(PREF_MODIFIED_ONLY, value.modifiedOnly)
            .apply()
        updateDerived { it.copy(preferences = value) }
    }

    private fun readPreferences(): AppListPreferences = AppListPreferences(
        sortField = runCatching {
            AppSortField.valueOf(
                preferences.getString(PREF_SORT_FIELD, AppSortField.Name.name)
                    ?: AppSortField.Name.name
            )
        }.getOrDefault(AppSortField.Name),
        descending = preferences.getBoolean(PREF_SORT_DESCENDING, false),
        showSystemApps = preferences.getBoolean(PREF_SHOW_SYSTEM, false),
        modifiedOnly = preferences.getBoolean(PREF_MODIFIED_ONLY, false)
    )

    fun openSearch() = _uiState.update {
        it.copy(search = it.search.openRequested())
    }

    fun finishSearchExpansion() = _uiState.update {
        it.copy(search = it.search.animationFinished())
    }

    fun requestSearchClose() = updateDerived {
        it.copy(search = it.search.closeRequested())
    }

    fun cancelSearch() = updateDerived {
        it.copy(search = it.search.cancelRequested())
    }

    fun finishSearchCollapse() = updateDerived {
        it.copy(search = it.search.animationFinished())
    }

    fun onSearchQueryChange(query: String) = updateDerived {
        it.copy(search = it.search.copy(query = query))
    }

    fun updateSearchOffset(offsetY: Dp) = _uiState.update {
        if (it.search.collapsedOffsetY == offsetY) it
        else it.copy(search = it.search.withMeasuredOffset(offsetY))
    }

    fun reloadLastSelectedItem() {
        val selectedApp = lastSelectedApp ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val baseApp = runCatching { appRepository.loadApp(selectedApp.pkg) }.getOrNull() ?: return@launch
            val updated = baseApp.copy(
                modifiedState = localeRepository.getModifiedState(baseApp.pkg)
            )
            lastSelectedApp = updated
            updateDerived { state ->
                state.copy(
                    listOfApps = state.listOfApps.map { if (it.pkg == updated.pkg) updated else it }
                )
            }
        }
    }

    fun onClickApp(app: AppInfo) {
        lastSelectedApp = app
        viewModelScope.launch(Dispatchers.IO) {
            if (dao.findByPkg(app.pkg) == null) dao.insert(app.toAppInfoEntity())
            dao.setLastSelected(app.pkg, System.currentTimeMillis())
        }
    }

    private inline fun updateDerived(transform: (MainScreenState) -> MainScreenState) {
        _uiState.update { current -> derive(transform(current)) }
    }

    private fun derive(state: MainScreenState): MainScreenState {
        val visible = AppListLogic.visibleApps(state.listOfApps, state.preferences)
        val results = AppListLogic.searchResults(
            state.listOfApps,
            state.preferences,
            state.search.query
        )
        return state.copy(
            visibleHomeApps = visible,
            searchResults = results,
            search = state.search.copy(
                resultState = searchResultStateFor(state.search.query, results.isNotEmpty())
            )
        )
    }

    private fun logPerf(message: String) {
        if (BuildConfig.DEBUG) Log.d(PERF_TAG, message)
    }
}
