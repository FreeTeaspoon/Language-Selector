package vegabobo.languageselector.data.locales

import android.os.LocaleList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vegabobo.languageselector.IUserService
import vegabobo.languageselector.service.UserServiceConnector
import vegabobo.languageselector.domain.apps.ModifiedState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocaleRepository @Inject constructor() {
    suspend fun awaitService(timeoutMillis: Long = 20_000L): IUserService? =
        UserServiceConnector.awaitService(timeoutMillis)

    suspend fun getModifiedState(packageName: String): ModifiedState =
        withServiceOrUnavailable { service ->
            if (service.getApplicationLocales(packageName).isEmpty) {
                ModifiedState.Unmodified
            } else {
                ModifiedState.Modified
            }
        }

    suspend fun getApplicationLocales(packageName: String): LocaleList? = withContext(Dispatchers.IO) {
        awaitService()?.getApplicationLocales(packageName)
    }

    suspend fun getSystemLocales(): LocaleList? = withContext(Dispatchers.IO) {
        awaitService()?.systemLocales
    }

    suspend fun setApplicationLocales(packageName: String, locales: LocaleList): Boolean =
        withContext(Dispatchers.IO) {
            val service = awaitService() ?: return@withContext false
            service.setApplicationLocales(packageName, locales)
            true
        }

    suspend fun forceStopPackage(packageName: String): Boolean = withContext(Dispatchers.IO) {
        val service = awaitService() ?: return@withContext false
        service.forceStopPackage(packageName)
        true
    }

    private suspend fun withServiceOrUnavailable(
        block: suspend (IUserService) -> ModifiedState
    ): ModifiedState = withContext(Dispatchers.IO) {
        val service = awaitService() ?: return@withContext ModifiedState.Unavailable
        runCatching { block(service) }.getOrElse { ModifiedState.Unavailable }
    }
}
