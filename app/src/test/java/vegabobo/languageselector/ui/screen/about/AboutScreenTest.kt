package vegabobo.languageselector.ui.screen.about

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AboutScreenTest {
    @Test
    fun staticDependencies_includeCoreLibrariesWithoutRuntimeMetadata() {
        val dependencies = staticDependencies()

        assertEquals(
            listOf("Miuix", "Shizuku API", "libsu", "AboutLibraries", "AndroidX", "Kotlin"),
            dependencies.map { it.name }
        )
        assertTrue(dependencies.all { it.summary.isNotBlank() })
    }
}
