package vegabobo.languageselector.ui.screen.appinfo

import android.content.pm.ApplicationInfo
import androidx.compose.runtime.mutableStateListOf
import vegabobo.languageselector.domain.apps.ModifiedState
import java.util.Locale

data class LocaleRegion(
    val language: String,
    val locales: ArrayList<SingleLocale>
)

data class SingleLocale(
    val name: String,
    val languageTag: String
) {
    fun toLocale(): Locale {
        return Locale.forLanguageTag(languageTag)
    }
}

fun LocaleRegion.pinLocale(): SingleLocale? {
    if (locales.isEmpty()) return null
    val languageOnly = locales.firstOrNull { locale ->
        val parsed = locale.toLocale()
        parsed.country.isEmpty() && parsed.script.isEmpty() && parsed.variant.isEmpty()
    }
    return languageOnly ?: locales.first()
}

fun List<SingleLocale>.containsLocale(locale: SingleLocale): Boolean =
    any { it.languageTag == locale.languageTag }

data class AppInfoState(
    val applicationInfo: ApplicationInfo? = null,
    val appName: String = "",
    val appPackage: String = "",
    val currentLanguage: String = "",
    val modifiedState: ModifiedState = ModifiedState.Unknown,
    val listOfSuggestedLanguages: MutableList<SingleLocale> = mutableStateListOf(),
    val listOfPinnedLanguages: MutableList<SingleLocale> = mutableStateListOf(),
    val selectedLanguage: Int = -1,
    val listOfAllLanguages: MutableList<LocaleRegion> = mutableStateListOf(),
)
