package vegabobo.languageselector.ui.screen.main

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import vegabobo.languageselector.dao.AppInfoEntity
import vegabobo.languageselector.domain.apps.AppInfo
import vegabobo.languageselector.domain.apps.AppListPreferences

enum class OperationMode { NONE, SHIZUKU, ROOT }

enum class SearchPhase { Collapsed, Expanding, Expanded, Collapsing }

enum class SearchResultState { Default, Empty, Results }

data class AppSearchState(
    val phase: SearchPhase = SearchPhase.Collapsed,
    val query: String = "",
    val collapsedOffsetY: Float = 0f,
    val resultState: SearchResultState = SearchResultState.Default
) {
    val isVisible: Boolean get() = phase != SearchPhase.Collapsed
    fun isCollapsed(): Boolean = phase == SearchPhase.Collapsed
}

fun AppSearchState.openRequested(): AppSearchState =
    if (phase == SearchPhase.Collapsed) copy(phase = SearchPhase.Expanding) else this

fun AppSearchState.animationFinished(): AppSearchState = when (phase) {
    SearchPhase.Expanding -> copy(phase = SearchPhase.Expanded)
    SearchPhase.Collapsing -> AppSearchState(collapsedOffsetY = collapsedOffsetY)
    else -> this
}

fun AppSearchState.closeRequested(): AppSearchState =
    if (query.isNotEmpty()) {
        copy(query = "", resultState = SearchResultState.Default)
    } else {
        copy(phase = SearchPhase.Collapsing)
    }

fun AppSearchState.cancelRequested(): AppSearchState =
    copy(phase = SearchPhase.Collapsing, query = "", resultState = SearchResultState.Default)

fun searchResultStateFor(query: String, hasResults: Boolean): SearchResultState = when {
    query.isBlank() -> SearchResultState.Default
    hasResults -> SearchResultState.Results
    else -> SearchResultState.Empty
}

data class MainScreenState(
    val listOfApps: List<AppInfo> = emptyList(),
    val visibleHomeApps: List<AppInfo> = emptyList(),
    val searchResults: List<AppInfo> = emptyList(),
    val operationMode: OperationMode = OperationMode.NONE,
    val preferences: AppListPreferences = AppListPreferences(),
    val search: AppSearchState = AppSearchState(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLocaleRefreshRunning: Boolean = false,
    val isOperationModeResolved: Boolean = false
)

fun AppInfo.toAppInfoEntity(): AppInfoEntity =
    AppInfoEntity(pkg, name, System.currentTimeMillis())

fun PackageManager.getLabel(applicationInfo: ApplicationInfo): String =
    applicationInfo.loadLabel(this).toString()

fun PackageManager.getAppIcon(applicationInfo: ApplicationInfo): Drawable =
    getApplicationIcon(applicationInfo)
