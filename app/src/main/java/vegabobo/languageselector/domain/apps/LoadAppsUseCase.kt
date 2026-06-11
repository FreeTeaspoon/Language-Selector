package vegabobo.languageselector.domain.apps

import vegabobo.languageselector.data.apps.AppRepository
import javax.inject.Inject

class LoadAppsUseCase @Inject constructor(
    private val appRepository: AppRepository
) {
    suspend operator fun invoke(): List<AppInfo> = appRepository.loadInstalledApps()
}
