package vegabobo.languageselector.data.apps

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vegabobo.languageselector.BuildConfig
import vegabobo.languageselector.domain.apps.AppInfo
import vegabobo.languageselector.domain.apps.ModifiedState
import vegabobo.languageselector.ui.screen.main.getLabel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    private val app: Application
) {
    private val packageManager: PackageManager
        get() = app.packageManager

    suspend fun loadInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        packageManager.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
            .asSequence()
            .filter { it.enabled && it.packageName != BuildConfig.APPLICATION_ID }
            .map { applicationInfo ->
                AppInfo(
                    applicationInfo = applicationInfo,
                    name = packageManager.getLabel(applicationInfo),
                    pkg = applicationInfo.packageName,
                    systemApp = applicationInfo.isSystemApp(),
                    modifiedState = ModifiedState.Unknown
                )
            }
            .toList()
    }

    suspend fun loadApp(packageName: String): AppInfo = withContext(Dispatchers.IO) {
        val applicationInfo = packageManager.getApplicationInfo(
            packageName,
            PackageManager.ApplicationInfoFlags.of(0)
        )
        AppInfo(
            applicationInfo = applicationInfo,
            name = packageManager.getLabel(applicationInfo),
            pkg = applicationInfo.packageName,
            systemApp = applicationInfo.isSystemApp(),
            modifiedState = ModifiedState.Unknown
        )
    }
}

private fun ApplicationInfo.isSystemApp(): Boolean =
    (flags and ApplicationInfo.FLAG_SYSTEM) != 0
