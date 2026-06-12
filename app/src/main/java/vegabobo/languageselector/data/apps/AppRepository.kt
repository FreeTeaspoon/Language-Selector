package vegabobo.languageselector.data.apps

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
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
        packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
            .asSequence()
            .mapNotNull { packageInfo -> packageInfo.applicationInfo?.let { packageInfo to it } }
            .filter { (_, applicationInfo) ->
                applicationInfo.enabled && applicationInfo.packageName != BuildConfig.APPLICATION_ID
            }
            .map { (packageInfo, applicationInfo) ->
                AppInfo(
                    applicationInfo = applicationInfo,
                    name = packageManager.getLabel(applicationInfo),
                    pkg = applicationInfo.packageName,
                    systemApp = applicationInfo.isSystemApp(),
                    firstInstallTime = packageInfo.firstInstallTime,
                    lastUpdateTime = packageInfo.lastUpdateTime,
                    modifiedState = ModifiedState.Unknown
                )
            }
            .toList()
    }

    suspend fun loadApp(packageName: String): AppInfo = withContext(Dispatchers.IO) {
        val packageInfo = packageManager.getPackageInfo(
            packageName,
            PackageManager.PackageInfoFlags.of(0)
        )
        val applicationInfo = requireNotNull(packageInfo.applicationInfo)
        AppInfo(
            applicationInfo = applicationInfo,
            name = packageManager.getLabel(applicationInfo),
            pkg = applicationInfo.packageName,
            systemApp = applicationInfo.isSystemApp(),
            firstInstallTime = packageInfo.firstInstallTime,
            lastUpdateTime = packageInfo.lastUpdateTime,
            modifiedState = ModifiedState.Unknown
        )
    }
}

private fun ApplicationInfo.isSystemApp(): Boolean =
    (flags and ApplicationInfo.FLAG_SYSTEM) != 0
