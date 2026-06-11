package vegabobo.languageselector.domain.apps

object AppListLogic {
    fun sortApps(apps: List<AppInfo>, prioritizeModified: Boolean): List<AppInfo> {
        val byName = compareBy<AppInfo> { it.name.lowercase() }
        return if (prioritizeModified) {
            apps.sortedWith(compareBy<AppInfo> { !it.isModified() }.then(byName))
        } else {
            apps.sortedWith(byName)
        }
    }

    fun visibleHomeApps(
        apps: List<AppInfo>,
        isShowingSystemApps: Boolean
    ): List<AppInfo> {
        if (isShowingSystemApps) return apps
        return apps.filterNot { it.isSystemApp() && !it.isModified() }
    }

    fun searchResults(
        apps: List<AppInfo>,
        query: String,
        selectedLabels: Set<AppLabels>
    ): List<AppInfo> {
        if (query.isBlank()) return emptyList()
        val lowerQuery = query.lowercase()
        return apps.filter { app ->
            if (selectedLabels.contains(AppLabels.MODIFIED) && !app.isModified()) {
                return@filter false
            }
            if (!selectedLabels.contains(AppLabels.SYSTEM_APP) && app.isSystemApp()) {
                return@filter false
            }
            app.pkg.lowercase().contains(lowerQuery) || app.name.lowercase().contains(lowerQuery)
        }
    }
}
