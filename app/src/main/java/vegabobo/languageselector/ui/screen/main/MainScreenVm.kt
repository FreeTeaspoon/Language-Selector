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
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
    private var resumeRefreshJob: Job? = null
    var lastSelectedApp: AppInfo? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            UserServiceConnector.connection
                .filterNotNull()
                .map { it.mode }
                .distinctUntilChanged()
                .collect { connectedMode ->
                    _uiState.update {
                        it.copy(
                            operationMode = connectedMode,
                            isOperationModeResolved = true
                        )
                    }
                    val state = _uiState.value
                    if (
                        state.listOfApps.isNotEmpty() &&
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

    fun refresh() {
        if (_uiState.value.appListPermissionState == AppListPermissionState.Denied) return
        loadApps(userRefresh = true)
    }

    fun onAppListPermissionGranted() {
        _uiState.update { it.copy(appListPermissionState = AppListPermissionState.Granted) }
        if (_uiState.value.listOfApps.isNotEmpty() || resumeRefreshJob?.isActive == true) return
        resumeRefreshJob = viewModelScope.launch(Dispatchers.IO) {
            refreshJob?.join()
            if (_uiState.value.listOfApps.isEmpty()) {
                loadApps(userRefresh = false)
            }
        }
    }

    fun onAppListPermissionDenied() {
        _uiState.update {
            it.copy(
                appListPermissionState = AppListPermissionState.Denied,
                isLoading = false,
                isRefreshing = false
            )
        }
    }

    fun onShizukuPermissionGranted() {
        _uiState.update { it.copy(shizukuAccessState = ShizukuAccessState.Granted) }
        if (connectionRefreshJob?.isActive == true) return
        connectionRefreshJob = viewModelScope.launch(Dispatchers.IO) {
            refreshJob?.join()
            val operationMode = resolveOperationMode()
            _uiState.update {
                it.copy(
                    operationMode = operationMode,
                    isOperationModeResolved = true
                )
            }
            if (_uiState.value.listOfApps.isEmpty()) {
                loadApps(userRefresh = false)
            } else {
                refreshLocaleStates()
            }
        }
    }

    fun onShizukuUnavailable() {
        _uiState.update { it.copy(shizukuAccessState = ShizukuAccessState.Unavailable) }
    }

    fun onShizukuPermissionDenied() {
        _uiState.update { it.copy(shizukuAccessState = ShizukuAccessState.Denied) }
    }

    private fun loadApps(userRefresh: Boolean) {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    isLoading = it.listOfApps.isEmpty(),
                    isRefreshing = userRefresh && it.listOfApps.isNotEmpty()
                )
            }
            try {
                val firstLoad = _uiState.value.listOfApps.isEmpty()
                val metadataStart = SystemClock.elapsedRealtime()
                coroutineScope {
                    val servicePrefetch = async {
                        val mode = resolveOperationMode()
                        if (mode != OperationMode.NONE) {
                            localeRepository.awaitService()
                        }
                    }
                    val loaded = loadAppsUseCase()
                    val previousStates = _uiState.value.listOfApps.associate { it.pkg to it.modifiedState }
                    val apps = loaded.map { app ->
                        app.copy(modifiedState = previousStates[app.pkg] ?: ModifiedState.Unknown)
                    }
                    logPerf("metadata load: ${apps.size} apps in ${SystemClock.elapsedRealtime() - metadataStart}ms")
                    servicePrefetch.await()
                    if (!firstLoad) {
                        updateDerived { it.copy(listOfApps = apps) }
                    }
                    refreshLocaleStates(apps)
                }
            } finally {
                _uiState.update { it.copy(isRefreshing = false, isLoading = false) }
            }
        }
    }

    private suspend fun refreshLocaleStates(apps: List<AppInfo> = _uiState.value.listOfApps) {
        val modeStart = SystemClock.elapsedRealtime()
        val operationMode = resolveOperationMode()
        logPerf("service mode resolved: $operationMode in ${SystemClock.elapsedRealtime() - modeStart}ms")
        _uiState.update {
            it.copy(operationMode = operationMode, isOperationModeResolved = true)
        }

        if (apps.isEmpty()) return
        if (operationMode == OperationMode.NONE) {
            updateDerived {
                it.copy(
                    listOfApps = apps.map { app ->
                        app.copy(modifiedState = ModifiedState.Unavailable)
                    },
                    isLocaleRefreshRunning = false
                )
            }
            return
        }

        val revealTogether = _uiState.value.listOfApps.isEmpty()
        _uiState.update { it.copy(isLocaleRefreshRunning = true) }
        val refreshStart = SystemClock.elapsedRealtime()
        var refreshedApps = 0
        try {
            if (revealTogether) {
                val byPackage = HashMap<String, AppInfo>(apps.size)
                refreshAppLocaleStatesUseCase(apps).collect { updates ->
                    refreshedApps += updates.size
                    updates.forEach { byPackage[it.pkg] = it }
                }
                updateDerived {
                    it.copy(listOfApps = apps.map { app -> byPackage[app.pkg] ?: app })
                }
            } else {
                refreshAppLocaleStatesUseCase(apps).collect { updates ->
                    refreshedApps += updates.size
                    val byPackage = updates.associateBy { it.pkg }
                    updateDerived { state ->
                        state.copy(
                            listOfApps = state.listOfApps.map { byPackage[it.pkg] ?: it }
                        )
                    }
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

    fun updateSearchStatus(search: AppSearchStatus) = updateDerived {
        it.copy(search = search)
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
        it.copy(search = it.search.copy(searchText = query))
    }

    fun updateSearchOffset(offsetY: Dp) = _uiState.update {
        if (it.search.offsetY == offsetY) it
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
            state.search.searchText
        )
        return state.copy(
            visibleHomeApps = visible,
            searchResults = results,
            search = state.search.copy(
                resultStatus = searchResultStateFor(state.search.searchText, results.isNotEmpty())
            )
        )
    }

    private fun logPerf(message: String) {
        if (BuildConfig.DEBUG) Log.d(PERF_TAG, message)
    }
}
