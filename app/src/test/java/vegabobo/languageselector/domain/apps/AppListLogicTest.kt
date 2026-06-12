package vegabobo.languageselector.domain.apps

import android.content.pm.ApplicationInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.RuleBasedCollator

class AppListLogicTest {
    @Test
    fun nameSort_usesProvidedLocaleCollator() {
        val collator = RuleBasedCollator("< a < b < z < å < ä < ö")
        val apps = listOf(
            app("pkg.o", name = "ö"),
            app("pkg.a", name = "å"),
            app("pkg.z", name = "z")
        )

        assertEquals(
            listOf("z", "å", "ö"),
            AppListLogic.sortApps(apps, preferences(), collator).map { it.name }
        )
    }

    @Test
    fun packageSort_isCaseInsensitive() {
        val apps = listOf(app("z.pkg"), app("A.pkg"), app("b.pkg"))

        assertEquals(
            listOf("A.pkg", "b.pkg", "z.pkg"),
            AppListLogic.sortApps(
                apps,
                preferences(sortField = AppSortField.PackageName)
            ).map { it.pkg }
        )
    }

    @Test
    fun installTimeSort_comparesRawTimestamps() {
        val apps = listOf(
            app("new", installTime = 30),
            app("old", installTime = 10),
            app("middle", installTime = 20)
        )

        assertEquals(
            listOf("old", "middle", "new"),
            AppListLogic.sortApps(
                apps,
                preferences(sortField = AppSortField.InstallTime)
            ).map { it.pkg }
        )
    }

    @Test
    fun updateTimeSort_supportsReverseOrder() {
        val apps = listOf(
            app("old", updateTime = 10),
            app("middle", updateTime = 20),
            app("new", updateTime = 30)
        )

        assertEquals(
            listOf("new", "middle", "old"),
            AppListLogic.sortApps(
                apps,
                preferences(sortField = AppSortField.UpdateTime, descending = true)
            ).map { it.pkg }
        )
    }

    @Test
    fun equalPrimaryValues_useAscendingPackageTieBreaker() {
        val apps = listOf(
            app("z.pkg", name = "same"),
            app("a.pkg", name = "same")
        )

        assertEquals(
            listOf("a.pkg", "z.pkg"),
            AppListLogic.sortApps(apps, preferences(descending = true)).map { it.pkg }
        )
    }

    @Test
    fun hiddenSystemApps_stillIncludesModifiedSystemApps() {
        val apps = listOf(
            app("user", system = false, state = ModifiedState.Unknown),
            app("system-unknown", system = true, state = ModifiedState.Unknown),
            app("system-unmodified", system = true, state = ModifiedState.Unmodified),
            app("system-modified", system = true, state = ModifiedState.Modified)
        )

        assertEquals(
            listOf("system-modified", "user"),
            AppListLogic.visibleApps(apps, preferences()).map { it.pkg }
        )
    }

    @Test
    fun showSystemApps_includesAllStates() {
        val apps = listOf(
            app("system-unavailable", system = true, state = ModifiedState.Unavailable),
            app("system-unknown", system = true, state = ModifiedState.Unknown),
            app("system-unmodified", system = true, state = ModifiedState.Unmodified)
        )

        assertEquals(
            3,
            AppListLogic.visibleApps(apps, preferences(showSystemApps = true)).size
        )
    }

    @Test
    fun modifiedOnly_includesOnlyKnownModifiedApps() {
        val apps = listOf(
            app("modified", state = ModifiedState.Modified),
            app("unknown", state = ModifiedState.Unknown),
            app("unavailable", state = ModifiedState.Unavailable),
            app("unmodified", state = ModifiedState.Unmodified)
        )

        val visible = AppListLogic.visibleApps(apps, preferences(modifiedOnly = true))

        assertEquals(listOf("modified"), visible.map { it.pkg })
        assertTrue(visible.all { it.modifiedState == ModifiedState.Modified })
    }

    @Test
    fun search_runsAfterSystemAndModifiedFilters() {
        val apps = listOf(
            app("com.example.user", name = "Calendar", state = ModifiedState.Modified),
            app("com.example.hidden", name = "Calendar System", system = true),
            app("com.example.notes", name = "Notes", state = ModifiedState.Modified)
        )

        assertEquals(
            listOf("com.example.user"),
            AppListLogic.searchResults(
                apps,
                preferences(modifiedOnly = true),
                "calendar"
            ).map { it.pkg }
        )
        assertEquals(
            listOf("com.example.notes"),
            AppListLogic.searchResults(apps, preferences(), "example.notes").map { it.pkg }
        )
    }

    @Test
    fun localeStateUpdates_doNotChangeNameBasedOrder() {
        val before = listOf(app("b.pkg", name = "Beta"), app("a.pkg", name = "Alpha"))
        val after = before.map {
            it.copy(modifiedState = if (it.pkg == "b.pkg") ModifiedState.Modified else ModifiedState.Unmodified)
        }

        assertEquals(
            AppListLogic.visibleApps(before, preferences()).map { it.pkg },
            AppListLogic.visibleApps(after, preferences()).map { it.pkg }
        )
    }

    private fun preferences(
        sortField: AppSortField = AppSortField.Name,
        descending: Boolean = false,
        showSystemApps: Boolean = false,
        modifiedOnly: Boolean = false
    ) = AppListPreferences(sortField, descending, showSystemApps, modifiedOnly)

    private fun app(
        pkg: String,
        name: String = pkg,
        system: Boolean = false,
        state: ModifiedState = ModifiedState.Unmodified,
        installTime: Long = 0,
        updateTime: Long = 0
    ): AppInfo = AppInfo(
        applicationInfo = ApplicationInfo().apply { packageName = pkg },
        name = name,
        pkg = pkg,
        systemApp = system,
        firstInstallTime = installTime,
        lastUpdateTime = updateTime,
        modifiedState = state
    )
}
