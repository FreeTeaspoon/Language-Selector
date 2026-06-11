package vegabobo.languageselector.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import vegabobo.languageselector.IUserService

object UserServiceConnector {
    private val _service = MutableStateFlow<IUserService?>(null)
    val service = _service.asStateFlow()

    fun update(service: IUserService?) {
        _service.value = service
    }

    fun current(): IUserService? = _service.value

    fun isConnected(): Boolean = _service.value != null

    suspend fun awaitService(timeoutMillis: Long = 20_000L): IUserService? =
        current() ?: withTimeoutOrNull(timeoutMillis) {
            service.filterNotNull().first()
        }
}
