package vegabobo.languageselector.ui.screen.main

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import vegabobo.languageselector.dao.AppInfoEntity

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
    val isShowSystemAppsHome: Boolean = false,
    val snackBarDisplay: SnackBarDisplay = SnackBarDisplay.NONE,

    /* Search bar */
    val isExpanded: Boolean = false,
    val searchTextFieldValue: String = "",
    val selectLabels: Set<AppLabels> = emptySet()
)

enum class AppLabels {
    SYSTEM_APP, MODIFIED
}

data class AppInfo(
    val icon: ImageBitmap,
    val name: String,
    val pkg: String,
    val labels: List<AppLabels> = emptyList()
) {
    fun isSystemApp() = labels.contains(AppLabels.SYSTEM_APP)
    fun isModified() = labels.contains(AppLabels.MODIFIED)
}

fun AppInfo.toAppInfoEntity(): AppInfoEntity {
    return AppInfoEntity(this.pkg, this.name, System.currentTimeMillis())
}

fun PackageManager.getLabel(applicationInfo: ApplicationInfo): String {
    return applicationInfo.loadLabel(this).toString()
}

fun PackageManager.getAppIcon(applicationInfo: ApplicationInfo): Drawable {
    return this.getApplicationIcon(applicationInfo)
}

fun PackageManager.getAppIconBitmap(applicationInfo: ApplicationInfo): ImageBitmap {
    return getAppIcon(applicationInfo).toBitmap().asImageBitmap()
}
