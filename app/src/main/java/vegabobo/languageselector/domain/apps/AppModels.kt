package vegabobo.languageselector.domain.apps

import android.content.pm.ApplicationInfo

enum class ModifiedState {
    Unknown, Modified, Unmodified, Unavailable
}

enum class AppSortField {
    Name, PackageName, InstallTime, UpdateTime
}

data class AppListPreferences(
    val sortField: AppSortField = AppSortField.Name,
    val descending: Boolean = false,
    val showSystemApps: Boolean = false,
    val modifiedOnly: Boolean = false
)

data class AppListItem(
    val applicationInfo: ApplicationInfo,
    val name: String,
    val pkg: String,
    val systemApp: Boolean,
    val firstInstallTime: Long,
    val lastUpdateTime: Long,
    val modifiedState: ModifiedState = ModifiedState.Unknown
) {
    fun isSystemApp() = systemApp
    fun isModified() = modifiedState == ModifiedState.Modified
}

typealias AppInfo = AppListItem
