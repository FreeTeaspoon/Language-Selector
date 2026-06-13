package vegabobo.languageselector.ui.screen.main

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import vegabobo.languageselector.dao.AppInfoEntity
import vegabobo.languageselector.domain.apps.AppInfo
import vegabobo.languageselector.domain.apps.AppListPreferences

enum class OperationMode { NONE, SHIZUKU, ROOT }

enum class SearchPhase { Expanded, Expanding, Collapsed, Collapsing }

enum class SearchResultState { Default, Empty, Results }

data class AppSearchStatus(
    val label: String = "",
    val searchText: String = "",
    val current: SearchPhase = SearchPhase.Collapsed,
    val offsetY: Dp = 0.dp,
    val resultStatus: SearchResultState = SearchResultState.Default
) {
    val isVisible: Boolean get() = current != SearchPhase.Collapsed
    fun isExpand(): Boolean = current == SearchPhase.Expanded
    fun isCollapsed(): Boolean = current == SearchPhase.Collapsed
    fun shouldExpand(): Boolean = current == SearchPhase.Expanded || current == SearchPhase.Expanding
    fun shouldCollapsed(): Boolean = current == SearchPhase.Collapsed || current == SearchPhase.Collapsing
    fun isAnimatingExpand(): Boolean = current == SearchPhase.Expanding
}

fun AppSearchStatus.openRequested(): AppSearchStatus =
    if (current == SearchPhase.Collapsed) {
        copy(current = SearchPhase.Expanding)
    } else {
        this
    }

fun AppSearchStatus.animationFinished(): AppSearchStatus = when (current) {
    SearchPhase.Expanding -> copy(current = SearchPhase.Expanded)
    SearchPhase.Collapsing -> copy(
        searchText = "",
        current = SearchPhase.Collapsed,
        resultStatus = SearchResultState.Default
    )
    else -> this
}

fun AppSearchStatus.closeRequested(): AppSearchStatus = when {
    searchText.isNotEmpty() -> copy(searchText = "", resultStatus = SearchResultState.Default)
    current == SearchPhase.Expanding || current == SearchPhase.Expanded -> copy(current = SearchPhase.Collapsing)
    else -> this
}

fun AppSearchStatus.cancelRequested(): AppSearchStatus = when (current) {
    SearchPhase.Expanding,
    SearchPhase.Expanded -> copy(
        searchText = "",
        current = SearchPhase.Collapsing,
        resultStatus = SearchResultState.Default
    )
    else -> this
}

fun AppSearchStatus.withMeasuredOffset(offsetY: Dp): AppSearchStatus {
    return if (current == SearchPhase.Collapsed) {
        copy(offsetY = offsetY)
    } else {
        this
    }
}

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
    val search: AppSearchStatus = AppSearchStatus(),
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
