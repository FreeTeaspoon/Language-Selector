package vegabobo.languageselector.domain.apps

import android.content.pm.ApplicationInfo

enum class AppLabels {
    SYSTEM_APP, MODIFIED
}

enum class ModifiedState {
    Unknown, Modified, Unmodified, Unavailable
}

data class AppListItem(
    val applicationInfo: ApplicationInfo,
    val name: String,
    val pkg: String,
    val systemApp: Boolean,
    val modifiedState: ModifiedState = ModifiedState.Unknown
) {
    fun isSystemApp() = systemApp
    fun isModified() = modifiedState == ModifiedState.Modified
}

typealias AppInfo = AppListItem
