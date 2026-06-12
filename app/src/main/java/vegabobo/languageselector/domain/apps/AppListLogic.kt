package vegabobo.languageselector.domain.apps

import java.text.Collator
import java.util.Locale

object AppListLogic {
    fun visibleApps(
        apps: List<AppInfo>,
        preferences: AppListPreferences,
        collator: Collator = Collator.getInstance(Locale.getDefault())
    ): List<AppInfo> = sortApps(
        apps = apps.filter { app ->
            (!preferences.modifiedOnly || app.isModified()) &&
                (preferences.showSystemApps || !app.isSystemApp() || app.isModified())
        },
        preferences = preferences,
        collator = collator
    )

    fun sortApps(
        apps: List<AppInfo>,
        preferences: AppListPreferences,
        collator: Collator = Collator.getInstance(Locale.getDefault())
    ): List<AppInfo> {
        val primary = when (preferences.sortField) {
            AppSortField.Name -> Comparator<AppInfo> { left, right ->
                collator.compare(left.name, right.name)
            }
            AppSortField.PackageName -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.pkg }
            AppSortField.InstallTime -> compareBy<AppInfo> { it.firstInstallTime }
            AppSortField.UpdateTime -> compareBy<AppInfo> { it.lastUpdateTime }
        }
        val orderedPrimary = if (preferences.descending) primary.reversed() else primary
        return apps.sortedWith(
            orderedPrimary.thenBy(String.CASE_INSENSITIVE_ORDER) { it.pkg }
        )
    }

    fun searchResults(
        apps: List<AppInfo>,
        preferences: AppListPreferences,
        query: String,
        collator: Collator = Collator.getInstance(Locale.getDefault())
    ): List<AppInfo> {
        if (query.isBlank()) return emptyList()
        val needle = query.trim().lowercase(Locale.getDefault())
        return visibleApps(apps, preferences, collator).filter { app ->
            app.name.lowercase(Locale.getDefault()).contains(needle) ||
                app.pkg.lowercase(Locale.ROOT).contains(needle)
        }
    }
}
