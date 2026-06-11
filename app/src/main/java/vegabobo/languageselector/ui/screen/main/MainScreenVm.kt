package vegabobo.languageselector.ui.screen.main

import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.topjohnwu.superuser.Shell
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku
import vegabobo.languageselector.BuildConfig
import vegabobo.languageselector.RootReceivedListener
import vegabobo.languageselector.dao.AppInfoDb
import vegabobo.languageselector.data.apps.AppRepository
import vegabobo.languageselector.data.locales.LocaleRepository
import vegabobo.languageselector.domain.apps.AppInfo
import vegabobo.languageselector.domain.apps.AppLabels
import vegabobo.languageselector.domain.apps.AppListLogic
import vegabobo.languageselector.domain.apps.LoadAppsUseCase
import vegabobo.languageselector.domain.apps.ModifiedState
import vegabobo.languageselector.domain.apps.RefreshAppLocaleStatesUseCase
import javax.inject.Inject

@HiltViewModel
class MainScreenVm @Inject constructor(
    private val loadAppsUseCase: LoadAppsUseCase,
    private val refreshAppLocaleStatesUseCase: RefreshAppLocaleStatesUseCase,
    private val appRepository: AppRepository,
    private val localeRepository: LocaleRepository,
    appInfoDb: AppInfoDb
) : ViewModel() {
    private companion object {
        const val PERF_TAG = "LanguageSelectorPerf"
    }

    private val _uiState = MutableStateFlow(MainScreenState())
    val uiState: StateFlow<MainScreenState> = _uiState.asStateFlow()
    var lastSelectedApp: AppInfo? = null
    private val dao = appInfoDb.appInfoDao()

    private val handler = Handler(Looper.getMainLooper())
    private var workRunnable: Runnable? = null
    private val searchDebounceMillis = 150L

    init {
        loadApps()
    }

    fun getIndexFromAppInfoItem(): Int {
        return _uiState.value.visibleHomeApps.indexOfFirst { it.pkg == lastSelectedApp?.pkg }
    }

    private fun loadApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val metadataStart = SystemClock.elapsedRealtime()
            val apps = loadAppsUseCase()
            logPerf("metadata load: ${apps.size} apps in ${SystemClock.elapsedRealtime() - metadataStart}ms")
            val sortedApps = AppListLogic.sortApps(apps, prioritizeModified = false)
            _uiState.update {
                it.copy(
                    listOfApps = sortedApps,
                    visibleHomeApps = AppListLogic.visibleHomeApps(
                        sortedApps,
                        it.isShowSystemAppsHome
                    ),
                    searchResults = AppListLogic.searchResults(
                        sortedApps,
                        it.searchTextFieldValue,
                        it.selectLabels
                    ),
                    isLoading = false
                )
            }
            refreshOperationModeAndLocaleStates()
        }
    }

    fun loadOperationMode() {
        viewModelScope.launch(Dispatchers.IO) {
            refreshOperationModeAndLocaleStates()
        }
    }

    private suspend fun refreshOperationModeAndLocaleStates() {
        val modeStart = SystemClock.elapsedRealtime()
        val operationMode = resolveOperationMode()
        logPerf("service mode resolved: $operationMode in ${SystemClock.elapsedRealtime() - modeStart}ms")
        _uiState.update { it.copy(operationMode = operationMode) }

        val currentApps = _uiState.value.listOfApps
        if (currentApps.isEmpty()) return

        if (operationMode == OperationMode.NONE) {
            applyLocaleUpdates(
                currentApps.map { it.copy(modifiedState = ModifiedState.Unavailable) },
                prioritizeModified = false,
                refreshComplete = true
            )
            logPerf("locale refresh skipped: service unavailable")
            return
        }

        _uiState.update { it.copy(isLocaleRefreshRunning = true) }
        val refreshStart = SystemClock.elapsedRealtime()
        var refreshedApps = 0
        refreshAppLocaleStatesUseCase(currentApps).collect { updates ->
            refreshedApps += updates.size
            logPerf("locale refresh batch: ${updates.size} apps, total=$refreshedApps")
            applyLocaleUpdates(
                updates = updates,
                prioritizeModified = false,
                refreshComplete = false
            )
        }
        _uiState.update {
            val sortedApps = AppListLogic.sortApps(it.listOfApps, prioritizeModified = true)
            it.copy(
                listOfApps = sortedApps,
                visibleHomeApps = AppListLogic.visibleHomeApps(
                    sortedApps,
                    it.isShowSystemAppsHome
                ),
                searchResults = AppListLogic.searchResults(
                    sortedApps,
                    it.searchTextFieldValue,
                    it.selectLabels
                ),
                isLocaleRefreshRunning = false
            )
        }
        logPerf("locale refresh complete: $refreshedApps apps in ${SystemClock.elapsedRealtime() - refreshStart}ms")
    }

    private suspend fun resolveOperationMode(): OperationMode {
        val hasRoot = runCatching {
            if (Shell.getShell().isAlive) {
                Shell.getShell().close()
            }
            Shell.getShell()
            Shell.isAppGrantedRoot() == true
        }.getOrDefault(false)

        if (hasRoot) {
            RootReceivedListener.onRootReceived()
            return OperationMode.ROOT
        }

        val hasShizuku = Shizuku.pingBinder() &&
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        return if (hasShizuku) OperationMode.SHIZUKU else OperationMode.NONE
    }

    private fun applyLocaleUpdates(
        updates: List<AppInfo>,
        prioritizeModified: Boolean,
        refreshComplete: Boolean
    ) {
        if (updates.isEmpty()) return
        _uiState.update { state ->
            val updatesByPackage = updates.associateBy { it.pkg }
            val mergedApps = state.listOfApps.map { app ->
                updatesByPackage[app.pkg] ?: app
            }
            val sortedApps = AppListLogic.sortApps(mergedApps, prioritizeModified)
            state.copy(
                listOfApps = sortedApps,
                visibleHomeApps = AppListLogic.visibleHomeApps(
                    sortedApps,
                    state.isShowSystemAppsHome
                ),
                searchResults = AppListLogic.searchResults(
                    sortedApps,
                    state.searchTextFieldValue,
                    state.selectLabels
                ),
                isLocaleRefreshRunning = !refreshComplete
            )
        }
    }

    fun toggleDropdown() {
        val newDropdownVisibility = !uiState.value.isDropdownVisible
        _uiState.update { it.copy(isDropdownVisible = newDropdownVisibility) }
    }

    fun toggleSystemAppsVisibility() {
        val newShowSystemApps = !uiState.value.isShowSystemAppsHome
        _uiState.update {
            it.copy(
                isShowSystemAppsHome = newShowSystemApps,
                visibleHomeApps = AppListLogic.visibleHomeApps(it.listOfApps, newShowSystemApps)
            )
        }
        toggleDropdown()
    }

    fun onClickProceedShizuku() {
        loadOperationMode()
    }

    fun onSearchTextFieldChange(newText: String) {
        _uiState.update { it.copy(searchTextFieldValue = newText) }

        workRunnable?.let(handler::removeCallbacks)

        workRunnable = Runnable {
            _uiState.update {
                it.copy(
                    searchResults = AppListLogic.searchResults(
                        it.listOfApps,
                        newText,
                        it.selectLabels
                    )
                )
            }
        }
        handler.postDelayed(workRunnable!!, searchDebounceMillis)
    }

    fun onSearchExpandedChange(isExpanded: Boolean) {
        _uiState.update { it.copy(isExpanded = isExpanded) }
        if (isExpanded) {
            updateHistory()
        } else {
            _uiState.update {
                it.copy(
                    searchTextFieldValue = "",
                    searchResults = emptyList()
                )
            }
        }
    }

    fun onSelectedLabelChange(label: AppLabels) {
        _uiState.update {
            val selectedLabels = if (it.selectLabels.contains(label)) {
                it.selectLabels - label
            } else {
                it.selectLabels + label
            }
            it.copy(
                selectLabels = selectedLabels,
                searchResults = AppListLogic.searchResults(
                    it.listOfApps,
                    it.searchTextFieldValue,
                    selectedLabels
                )
            )
        }
    }

    fun updateHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            val appInfoList = dao.getHistory().map { it.pkg }
            val history = appInfoList.mapNotNull { pkg ->
                _uiState.value.listOfApps.firstOrNull { it.pkg == pkg }
            }
            _uiState.update { it.copy(history = history) }
        }
    }

    fun addAppToHistory(ai: AppInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            if (dao.findByPkg(ai.pkg) == null) {
                dao.insert(ai.toAppInfoEntity())
            }
            dao.setLastSelected(ai.pkg, System.currentTimeMillis())
            updateHistory()
        }
    }

    fun onClickClear() {
        viewModelScope.launch(Dispatchers.IO) {
            dao.cleanLastSelectedAll()
            updateHistory()
        }
    }

    fun reloadLastSelectedItem() {
        val selectedApp = lastSelectedApp ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val baseApp = runCatching { appRepository.loadApp(selectedApp.pkg) }.getOrNull() ?: return@launch
            val updatedAi = baseApp.copy(
                modifiedState = localeRepository.getModifiedState(baseApp.pkg)
            )
            val apps = _uiState.value.listOfApps
            val idx = apps.indexOfFirst { it.pkg == updatedAi.pkg }
            if (idx != -1 && updatedAi.modifiedState != apps[idx].modifiedState) {
                val appsWithUpdate = apps.toMutableList()
                appsWithUpdate[idx] = updatedAi
                val sortedApps = AppListLogic.sortApps(
                    appsWithUpdate,
                    prioritizeModified = !_uiState.value.isLocaleRefreshRunning
                )
                lastSelectedApp = updatedAi
                _uiState.update {
                    it.copy(
                        listOfApps = sortedApps,
                        visibleHomeApps = AppListLogic.visibleHomeApps(
                            sortedApps,
                            it.isShowSystemAppsHome
                        ),
                        searchResults = AppListLogic.searchResults(
                            sortedApps,
                            it.searchTextFieldValue,
                            it.selectLabels
                        ),
                        snackBarDisplay = if (updatedAi.isModified()) {
                            SnackBarDisplay.MOVED_TO_TOP
                        } else {
                            SnackBarDisplay.MOVED_TO_BOTTOM
                        }
                    )
                }
            }
        }
    }

    fun resetSnackBarDisplay() = _uiState.update {
        it.copy(snackBarDisplay = SnackBarDisplay.NONE)
    }

    fun onClickApp(ai: AppInfo) {
        lastSelectedApp = ai
        addAppToHistory(ai)
    }

    override fun onCleared() {
        workRunnable?.let { handler.removeCallbacks(it) }
        super.onCleared()
    }

    private fun logPerf(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(PERF_TAG, message)
        }
    }
}
