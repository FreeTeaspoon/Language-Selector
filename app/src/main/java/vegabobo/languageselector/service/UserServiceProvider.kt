package vegabobo.languageselector.service

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import vegabobo.languageselector.IUserService
import vegabobo.languageselector.ui.screen.main.OperationMode

object UserServiceProvider {

    private val tag = this.javaClass.simpleName

    val shizukuConnection = Connection(OperationMode.SHIZUKU)
    val rootConnection = Connection(OperationMode.ROOT)
    val opMode: OperationMode
        get() = UserServiceConnector.currentMode()

    // Blocking
    fun getService(): IUserService {
        return UserServiceConnector.current()
            ?: runBlocking { UserServiceConnector.awaitService() }
            ?: throw Exception("Service unavailable.")
    }

    fun run(
        onFail: () -> Unit = {},
        onConnected: suspend IUserService.() -> Unit,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val connectedService = UserServiceConnector.current()
                ?: UserServiceConnector.awaitService()
            if (connectedService == null) {
                Log.e(tag, "Service unavailable.")
                onFail()
                return@launch
            }
            val serviceUid = connectedService.uid
            Log.d(tag, "IUserService available, uid: $serviceUid, mode: $opMode")
            onConnected(connectedService)
        }
    }

    fun isConnected(mode: OperationMode? = null): Boolean =
        UserServiceConnector.isConnected(mode)
}
