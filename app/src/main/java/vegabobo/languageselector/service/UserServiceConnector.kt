package vegabobo.languageselector.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import vegabobo.languageselector.IUserService
import vegabobo.languageselector.ui.screen.main.OperationMode

object UserServiceConnector {
    data class ConnectedService(
        val service: IUserService,
        val mode: OperationMode
    )

    private val services = mutableMapOf<OperationMode, IUserService>()
    private val _connection = MutableStateFlow<ConnectedService?>(null)
    val connection = _connection.asStateFlow()

    @Synchronized
    fun update(mode: OperationMode, service: IUserService?) {
        if (service == null) {
            services.remove(mode)
        } else {
            services[mode] = service
        }
        _connection.value = preferredService(services)?.let { (selectedMode, selectedService) ->
            ConnectedService(selectedService, selectedMode)
        }
    }

    fun current(): IUserService? = _connection.value?.service

    fun current(mode: OperationMode): IUserService? = synchronized(this) {
        services[mode]
    }

    fun currentMode(): OperationMode = _connection.value?.mode ?: OperationMode.NONE

    fun isConnected(mode: OperationMode? = null): Boolean = if (mode == null) {
        _connection.value != null
    } else {
        current(mode) != null
    }

    suspend fun awaitService(timeoutMillis: Long = 20_000L): IUserService? =
        current() ?: withTimeoutOrNull(timeoutMillis) {
            connection.filterNotNull().first().service
        }
}

internal fun <T> preferredService(
    services: Map<OperationMode, T>
): Pair<OperationMode, T>? = services[OperationMode.ROOT]?.let {
    OperationMode.ROOT to it
} ?: services[OperationMode.SHIZUKU]?.let {
    OperationMode.SHIZUKU to it
}
