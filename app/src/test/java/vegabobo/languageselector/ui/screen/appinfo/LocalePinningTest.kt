package vegabobo.languageselector.ui.screen.appinfo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import vegabobo.languageselector.LocaleManager

class LocalePinningTest {
    @Test
    fun pinLocale_prefersLanguageOnlyTag() {
        val region = LocaleRegion(
            "English",
            arrayListOf(
                SingleLocale("English (United States)", "en-US"),
                SingleLocale("English", "en"),
                SingleLocale("English (United Kingdom)", "en-GB"),
            )
        )

        assertEquals("en", region.pinLocale()?.languageTag)
    }

    @Test
    fun pinLocale_fallsBackToFirstWhenNoLanguageOnly() {
        val region = LocaleRegion(
            "English",
            arrayListOf(
                SingleLocale("English (United States)", "en-US"),
                SingleLocale("English (United Kingdom)", "en-GB"),
            )
        )

        assertEquals("en-US", region.pinLocale()?.languageTag)
    }

    @Test
    fun pinLocale_returnsNullWhenEmpty() {
        assertNull(LocaleRegion("English", arrayListOf()).pinLocale())
    }

    @Test
    fun containsLocale_matchesByLanguageTag() {
        val pinned = listOf(SingleLocale("English", "en"))

        assertTrue(pinned.containsLocale(SingleLocale("English", "en")))
        assertFalse(pinned.containsLocale(SingleLocale("English (United States)", "en-US")))
    }

    @Test
    fun parseSetLangs_preservesCommasInDisplayName() {
        assertEquals(
            listOf(SingleLocale("Basa Sunda (Latin, Indonesia)", "su-Latn-ID")),
            setOf("Basa Sunda (Latin,Indonesia),su-Latn-ID").parseSetLangs()
        )
    }

    @Test
    fun parseSetLangs_preservesStoredOrder() {
        assertEquals(
            listOf(
                SingleLocale("English", "en"),
                SingleLocale("Japanese", "ja"),
                SingleLocale("Arabic", "ar"),
            ),
            listOf(
                "English,en",
                "Japanese,ja",
                "Arabic,ar",
            ).parseSetLangs()
        )
    }

    @Test
    fun legacyPinMigration_usesStableNameOrder() {
        assertEquals(
            listOf(
                SingleLocale("Arabic", "ar"),
                SingleLocale("English", "en"),
                SingleLocale("Japanese", "ja"),
            ),
            setOf(
                "Japanese,ja",
                "English,en",
                "Arabic,ar",
            ).parseSetLangs().sortPinnedLocalesForMigration()
        )
    }

    @Test
    fun withoutLocale_removesLocaleWithCommaInDisplayName() {
        val pinned = setOf("Basa Sunda (Latin, Indonesia),su-Latn-ID")

        assertEquals(
            emptySet<String>(),
            pinned.withoutLocale(SingleLocale("Basa Sunda (Latin, Indonesia)", "su-Latn-ID"))
        )
    }

    @Test
    fun withoutLocale_keepsRegionalPinsWhenRemovingLanguage() {
        val pinned = setOf(
            "English,en",
            "English (United States),en-US",
            "English (United Kingdom),en-GB"
        )

        assertEquals(
            setOf(
                "English (United States),en-US",
                "English (United Kingdom),en-GB"
            ),
            pinned.withoutLocale(SingleLocale("English", "en"))
        )
    }

    @Test
    fun hasMultipleSelections_requiresMoreThanOneLocale() {
        assertFalse(
            LocaleRegion("Esperanto", arrayListOf(SingleLocale("Esperanto", "eo")))
                .hasMultipleSelections()
        )
        assertTrue(
            LocaleRegion(
                "English",
                arrayListOf(
                    SingleLocale("English", "en"),
                    SingleLocale("English (United States)", "en-US"),
                )
            ).hasMultipleSelections()
        )
    }

    @Test
    fun localeManager_includesAtLeastOneLocalePerLanguage() {
        val languages = LocaleManager().localeList

        assertTrue(languages.isNotEmpty())
        languages.forEach { region ->
            assertTrue("${region.language} should include at least one locale", region.locales.isNotEmpty())
        }
    }
}
