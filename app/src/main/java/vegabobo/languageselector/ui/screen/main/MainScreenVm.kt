package vegabobo.languageselector.ui.screen.main

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.topjohnwu.superuser.Shell
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku
import vegabobo.languageselector.BuildConfig
import vegabobo.languageselector.RootReceivedListener
import vegabobo.languageselector.dao.AppInfoDb
import vegabobo.languageselector.service.UserServiceProvider
import javax.inject.Inject


@HiltViewModel
class MainScreenVm @Inject constructor(
    val app: Application,
    appInfoDb: AppInfoDb
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainScreenState())
    val uiState: StateFlow<MainScreenState> = _uiState.asStateFlow()
    var lastSelectedApp: AppInfo? = null
    val dao = appInfoDb.appInfoDao()

    fun getIndexFromAppInfoItem(): Int {
        return _uiState.value.visibleHomeApps.indexOfFirst { it.pkg == lastSelectedApp?.pkg }
    }

    fun loadOperationMode() {
        if (Shell.getShell().isAlive)
            Shell.getShell().close()
        Shell.getShell()
        if (Shell.isAppGrantedRoot() == true) {
            _uiState.update { it.copy(operationMode = OperationMode.ROOT) }
            RootReceivedListener.onRootReceived()
            return
        }

        val isAvail = Shizuku.pingBinder() &&
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        if (isAvail) {
            _uiState.update { it.copy(operationMode = OperationMode.SHIZUKU) }
            return
        }

        _uiState.update { it.copy(operationMode = OperationMode.NONE) }
    }

    init {
        fillListOfApps()
    }

    fun parseAppInfo(a: ApplicationInfo): AppInfo {
        val isSystemApp = (a.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        val service = UserServiceProvider.getService()
        val languagePreferences = service.getApplicationLocales(a.packageName)
        val labels = arrayListOf<AppLabels>()
        if (isSystemApp)
            labels.add(AppLabels.SYSTEM_APP)
        if (!languagePreferences.isEmpty)
            labels.add(AppLabels.MODIFIED)
        return AppInfo(
            icon = app.packageManager.getAppIconBitmap(a),
            name = app.packageManager.getLabel(a),
            pkg = a.packageName,
            labels = labels
        )
    }

    fun fillListOfApps() {
        viewModelScope.launch(Dispatchers.IO) {
            if (_uiState.value.operationMode == OperationMode.NONE)
                loadOperationMode()
            val packageList = getInstalledPackages().map { parseAppInfo(it) }
            val sortedList =
                packageList.sortedBy { it.name.lowercase() }.sortedBy { !it.isModified() }
            _uiState.update {
                it.copy(
                    listOfApps = sortedList,
                    visibleHomeApps = getVisibleHomeApps(sortedList, it.isShowSystemAppsHome),
                    searchResults = getSearchResults(
                        sortedList,
                        it.searchTextFieldValue,
                        it.selectLabels
                    ),
                    isLoading = false
                )
            }
        }
    }

    fun getInstalledPackages(): List<ApplicationInfo> {
        return app.packageManager.getInstalledApplications(
            PackageManager.ApplicationInfoFlags.of(0)
        ).mapNotNull {
            if (!it.enabled || BuildConfig.APPLICATION_ID == it.packageName)
                null
            else
                it
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
                visibleHomeApps = getVisibleHomeApps(it.listOfApps, newShowSystemApps)
            )
        }
        toggleDropdown()
    }

    fun onClickProceedShizuku() {
        loadOperationMode()
    }

    private val handler = Handler(Looper.getMainLooper())
    private var workRunnable: Runnable? = null
    private val searchDebounceMillis = 150L

    fun onSearchTextFieldChange(newText: String) {
        _uiState.update { it.copy(searchTextFieldValue = newText) }

        if (workRunnable != null)
            handler.removeCallbacks(workRunnable!!)

        workRunnable = Runnable {
            _uiState.update {
                it.copy(searchResults = getSearchResults(it.listOfApps, newText, it.selectLabels))
            }
        }
        handler.postDelayed(workRunnable!!, searchDebounceMillis)
    }

    fun onSearchExpandedChange(isExpanded: Boolean) {
        _uiState.update { it.copy(isExpanded = isExpanded) }
        if (isExpanded)
            updateHistory()
        else
            _uiState.update {
                it.copy(
                    searchTextFieldValue = "",
                    searchResults = emptyList()
                )
            }
    }

    fun onSelectedLabelChange(label: AppLabels) {
        _uiState.update {
            val selectedLabels = if (it.selectLabels.contains(label))
                it.selectLabels - label
            else
                it.selectLabels + label
            it.copy(
                selectLabels = selectedLabels,
                searchResults = getSearchResults(
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
                val listOfApps = _uiState.value.listOfApps
                val idx = listOfApps.indexOfFirst { it.pkg == pkg }
                if (idx == -1)
                    null
                else
                    listOfApps[idx]
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
        if (lastSelectedApp == null) return
        val pkg = app.packageManager.getApplicationInfo(lastSelectedApp!!.pkg, 0)
        val updatedAi = parseAppInfo(pkg)
        val apps = _uiState.value.listOfApps
        val idx = apps.indexOfFirst { it.pkg == updatedAi.pkg }
        if (idx != -1 && updatedAi.labels != apps[idx].labels) {
            val appsWithUpdate = apps.toMutableList()
            appsWithUpdate[idx] = updatedAi
            val newList = appsWithUpdate.sortedBy { it.name.lowercase() }
                .sortedBy { !it.isModified() }.toMutableList()
            _uiState.update {
                it.copy(
                    listOfApps = newList,
                    visibleHomeApps = getVisibleHomeApps(newList, it.isShowSystemAppsHome),
                    searchResults = getSearchResults(
                        newList,
                        it.searchTextFieldValue,
                        it.selectLabels
                    ),
                    snackBarDisplay = if (updatedAi.isModified()) SnackBarDisplay.MOVED_TO_TOP else SnackBarDisplay.MOVED_TO_BOTTOM
                )
            }
            return
        }
    }

    fun resetSnackBarDisplay() = _uiState.update { it.copy(snackBarDisplay = SnackBarDisplay.NONE) }

    fun onClickApp(ai: AppInfo) {
        lastSelectedApp = ai
        addAppToHistory(ai)
    }

    override fun onCleared() {
        workRunnable?.let { handler.removeCallbacks(it) }
        super.onCleared()
    }

    private fun getVisibleHomeApps(
        apps: List<AppInfo>,
        isShowingSystemApps: Boolean
    ): List<AppInfo> {
        if (isShowingSystemApps) return apps
        return apps.filterNot { it.isSystemApp() && !it.isModified() }
    }

    private fun getSearchResults(
        apps: List<AppInfo>,
        query: String,
        selectedLabels: Set<AppLabels>
    ): List<AppInfo> {
        if (query.isBlank()) return emptyList()
        val lQuery = query.lowercase()
        return apps.filter { app ->
            if (selectedLabels.contains(AppLabels.MODIFIED) && !app.isModified())
                return@filter false
            if (!selectedLabels.contains(AppLabels.SYSTEM_APP) && app.isSystemApp())
                return@filter false
            app.pkg.lowercase().contains(lQuery) || app.name.lowercase().contains(lQuery)
        }
    }
}
