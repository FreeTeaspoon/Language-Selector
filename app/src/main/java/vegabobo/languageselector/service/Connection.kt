package vegabobo.languageselector.service

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import vegabobo.languageselector.IUserService
import vegabobo.languageselector.ui.screen.main.OperationMode

class Connection(
    private val mode: OperationMode
) : ServiceConnection {
    @Volatile
    private var bindingRequested = false

    val service: IUserService?
        get() = UserServiceConnector.current(mode)

    @Synchronized
    fun markBindingRequested(): Boolean {
        if (bindingRequested || service != null) return false
        bindingRequested = true
        return true
    }

    @Synchronized
    fun clearBindingRequested() {
        bindingRequested = false
    }

    fun clear() {
        clearBindingRequested()
        UserServiceConnector.update(mode, null)
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        clearBindingRequested()
        UserServiceConnector.update(mode, IUserService.Stub.asInterface(service))
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        clear()
    }

    override fun onBindingDied(name: ComponentName?) {
        clear()
    }

    override fun onNullBinding(name: ComponentName?) {
        clear()
    }
}
