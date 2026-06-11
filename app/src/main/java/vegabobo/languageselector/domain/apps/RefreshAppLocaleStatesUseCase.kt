package vegabobo.languageselector.domain.apps

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import vegabobo.languageselector.data.locales.LocaleRepository
import javax.inject.Inject

class RefreshAppLocaleStatesUseCase @Inject constructor(
    private val localeRepository: LocaleRepository
) {
    operator fun invoke(
        apps: List<AppInfo>,
        batchSize: Int = 16
    ): Flow<List<AppInfo>> = flow {
        val service = localeRepository.awaitService()
        if (service == null) {
            emit(apps.map { it.copy(modifiedState = ModifiedState.Unavailable) })
            return@flow
        }

        val pendingUpdates = mutableListOf<AppInfo>()
        apps.forEach { app ->
            val modifiedState = runCatching {
                if (service.getApplicationLocales(app.pkg).isEmpty) {
                    ModifiedState.Unmodified
                } else {
                    ModifiedState.Modified
                }
            }.getOrElse { ModifiedState.Unavailable }
            pendingUpdates.add(app.copy(modifiedState = modifiedState))
            if (pendingUpdates.size >= batchSize) {
                emit(pendingUpdates.toList())
                pendingUpdates.clear()
            }
        }
        if (pendingUpdates.isNotEmpty()) {
            emit(pendingUpdates.toList())
        }
    }
}
