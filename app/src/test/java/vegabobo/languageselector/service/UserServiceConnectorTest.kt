package vegabobo.languageselector.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import vegabobo.languageselector.ui.screen.main.OperationMode

class UserServiceConnectorTest {
    @Test
    fun preferredService_prefersRootWhenBothAreConnected() {
        val selected = preferredService(
            mapOf(
                OperationMode.SHIZUKU to "shizuku",
                OperationMode.ROOT to "root"
            )
        )

        assertEquals(OperationMode.ROOT to "root", selected)
    }

    @Test
    fun preferredService_fallsBackToShizukuWhenRootDisconnects() {
        val selected = preferredService(
            mapOf(OperationMode.SHIZUKU to "shizuku")
        )

        assertEquals(OperationMode.SHIZUKU to "shizuku", selected)
    }

    @Test
    fun preferredService_returnsNullWithoutConnections() {
        assertNull(preferredService<String>(emptyMap()))
    }
}
