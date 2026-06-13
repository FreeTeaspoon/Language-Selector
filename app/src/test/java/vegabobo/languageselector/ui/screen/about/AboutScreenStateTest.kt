package vegabobo.languageselector.ui.screen.about

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AboutScreenStateTest {
    @Test
    fun staticDependencies_containsExpectedCoreRows() {
        val dependencies = staticDependencies()
        val names = dependencies.map { it.name }

        assertEquals(6, dependencies.size)
        assertTrue(names.contains("Miuix"))
        assertTrue(names.contains("Shizuku API"))
        assertTrue(names.contains("libsu"))
        assertTrue(names.contains("AboutLibraries"))
        assertTrue(names.contains("AndroidX"))
        assertTrue(names.contains("Kotlin"))
    }

    @Test
    fun staticDependencies_haveSafeOptionalUrls() {
        staticDependencies().forEach { dependency ->
            assertTrue(dependency.summary.isNotBlank())
            dependency.url?.let { assertTrue(it.startsWith("https://")) }
        }
    }
}
