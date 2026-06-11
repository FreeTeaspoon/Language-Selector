package vegabobo.languageselector.ui.screen.main

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import vegabobo.languageselector.dao.AppInfoEntity
import vegabobo.languageselector.domain.apps.AppInfo
import vegabobo.languageselector.domain.apps.AppLabels

enum class OperationMode {
    NONE, SHIZUKU, ROOT
}

enum class SnackBarDisplay {
    NONE, MOVED_TO_TOP, MOVED_TO_BOTTOM
}

data class MainScreenState(
    val listOfApps: List<AppInfo> = emptyList(),
    val visibleHomeApps: List<AppInfo> = emptyList(),
    val searchResults: List<AppInfo> = emptyList(),
    val history: List<AppInfo> = emptyList(),
    val operationMode: OperationMode = OperationMode.NONE,
    val isDropdownVisible: Boolean = false,
    val isAboutDialogVisible: Boolean = false,
    val isLoading: Boolean = true,
    val isLocaleRefreshRunning: Boolean = false,
    val isShowSystemAppsHome: Boolean = false,
    val snackBarDisplay: SnackBarDisplay = SnackBarDisplay.NONE,

    /* Search bar */
    val isExpanded: Boolean = false,
    val searchTextFieldValue: String = "",
    val selectLabels: Set<AppLabels> = emptySet()
)

fun AppInfo.toAppInfoEntity(): AppInfoEntity {
    return AppInfoEntity(this.pkg, this.name, System.currentTimeMillis())
}

fun PackageManager.getLabel(applicationInfo: ApplicationInfo): String {
    return applicationInfo.loadLabel(this).toString()
}

fun PackageManager.getAppIcon(applicationInfo: ApplicationInfo): Drawable {
    return this.getApplicationIcon(applicationInfo)
}
