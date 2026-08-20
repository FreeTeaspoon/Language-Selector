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
