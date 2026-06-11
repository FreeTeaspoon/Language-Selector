package vegabobo.languageselector.domain.apps

import android.content.pm.ApplicationInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppListLogicTest {
    @Test
    fun visibleHomeApps_hidesUnknownAndUnmodifiedSystemApps() {
        val apps = listOf(
            app("user", system = false, state = ModifiedState.Unknown),
            app("system-unknown", system = true, state = ModifiedState.Unknown),
            app("system-unmodified", system = true, state = ModifiedState.Unmodified),
            app("system-modified", system = true, state = ModifiedState.Modified)
        )

        val visible = AppListLogic.visibleHomeApps(apps, isShowingSystemApps = false)

        assertEquals(listOf("user", "system-modified"), visible.map { it.pkg })
    }

    @Test
    fun sortApps_prioritizesModifiedThenLabel() {
        val apps = listOf(
            app("zeta", state = ModifiedState.Unmodified),
            app("beta", state = ModifiedState.Modified),
            app("alpha", state = ModifiedState.Modified),
            app("gamma", state = ModifiedState.Unknown)
        )

        val sorted = AppListLogic.sortApps(apps, prioritizeModified = true)

        assertEquals(listOf("alpha", "beta", "gamma", "zeta"), sorted.map { it.name })
    }

    @Test
    fun searchResults_matchesLabelAndPackage() {
        val apps = listOf(
            app("com.example.calendar", name = "Agenda"),
            app("com.example.notes", name = "Notebook")
        )

        assertEquals(
            listOf("com.example.calendar"),
            AppListLogic.searchResults(apps, "agenda", emptySet()).map { it.pkg }
        )
        assertEquals(
            listOf("com.example.notes"),
            AppListLogic.searchResults(apps, "example.notes", emptySet()).map { it.pkg }
        )
    }

    @Test
    fun searchResults_modifiedAndSystemFiltersOnlyIncludeKnownMatches() {
        val apps = listOf(
            app("user-modified", system = false, state = ModifiedState.Modified),
            app("user-unknown", system = false, state = ModifiedState.Unknown),
            app("system-modified", system = true, state = ModifiedState.Modified),
            app("system-unmodified", system = true, state = ModifiedState.Unmodified)
        )

        val modifiedOnly = AppListLogic.searchResults(
            apps,
            query = "modified",
            selectedLabels = setOf(AppLabels.MODIFIED)
        )
        val modifiedIncludingSystem = AppListLogic.searchResults(
            apps,
            query = "modified",
            selectedLabels = setOf(AppLabels.MODIFIED, AppLabels.SYSTEM_APP)
        )

        assertEquals(listOf("user-modified"), modifiedOnly.map { it.pkg })
        assertEquals(
            listOf("user-modified", "system-modified"),
            modifiedIncludingSystem.map { it.pkg }
        )
        assertTrue(modifiedIncludingSystem.none { it.modifiedState != ModifiedState.Modified })
    }

    private fun app(
        pkg: String,
        name: String = pkg,
        system: Boolean = false,
        state: ModifiedState = ModifiedState.Unmodified
    ): AppInfo = AppInfo(
        applicationInfo = ApplicationInfo().apply { packageName = pkg },
        name = name,
        pkg = pkg,
        systemApp = system,
        modifiedState = state
    )
}
