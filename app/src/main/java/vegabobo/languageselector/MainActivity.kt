package vegabobo.languageselector

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableIntStateOf
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.NavigationEventInput
import androidx.navigationevent.OnBackInvokedDefaultInput
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ipc.RootService
import dagger.hilt.android.AndroidEntryPoint
import rikka.shizuku.Shizuku
import vegabobo.languageselector.service.RootUserService
import vegabobo.languageselector.service.UserService
import vegabobo.languageselector.service.UserServiceProvider
import vegabobo.languageselector.ui.screen.Navigation
import vegabobo.languageselector.ui.screen.main.OperationMode
import vegabobo.languageselector.ui.theme.LanguageSelector

object ShizukuArgs {
    val userServiceArgs =
        Shizuku.UserServiceArgs(
            ComponentName(BuildConfig.APPLICATION_ID, UserService::class.java.name),
        )
            .daemon(false)
            .processNameSuffix("service")
            .debuggable(BuildConfig.DEBUG)
            .version(BuildConfig.VERSION_CODE)
}

private const val GET_INSTALLED_APPS_PERMISSION = "com.android.permission.GET_INSTALLED_APPS"


@AndroidEntryPoint
class MainActivity : ComponentActivity(), Shizuku.OnRequestPermissionResultListener,
    NavigationEventDispatcherOwner {

    override val navigationEventDispatcher = NavigationEventDispatcher()
    private var navigationEventInput: NavigationEventInput? = null
    private var onShizukuPermissionGranted: (() -> Unit)? = null
    private var onShizukuPermissionDenied: (() -> Unit)? = null
    private var onAppListPermissionGranted: (() -> Unit)? = null
    private var onAppListPermissionDenied: (() -> Unit)? = null
    private val activityResumeCount = mutableIntStateOf(0)

    init {
        Shell.enableVerboseLogging = BuildConfig.DEBUG
        Shell.setDefaultBuilder(Shell.Builder.create().setTimeout(10))
    }

    private val acRequestCode = 1
    private val appListRequestCode = 2

    fun bindShizuku() {
        val connection = UserServiceProvider.shizukuConnection
        if (!connection.markBindingRequested()) return
        runCatching {
            Shizuku.bindUserService(ShizukuArgs.userServiceArgs, connection)
        }.onFailure {
            connection.clearBindingRequested()
            Log.e(BuildConfig.APPLICATION_ID, "Failed to bind Shizuku user service", it)
        }
    }

    private fun bindRoot() {
        val connection = UserServiceProvider.rootConnection
        if (!connection.markBindingRequested()) return
        runOnUiThread {
            runCatching {
                val intent = Intent(application, RootUserService::class.java)
                RootService.bind(intent, connection)
            }.onFailure {
                connection.clearBindingRequested()
                Log.e(BuildConfig.APPLICATION_ID, "Failed to bind root user service", it)
            }
        }
    }

    private val REQUEST_PERMISSION_RESULT_LISTENER = this::onRequestPermissionResult

    override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
        if (requestCode != acRequestCode) return
        if (grantResult == PackageManager.PERMISSION_GRANTED) {
            bindShizuku()
            onShizukuPermissionGranted?.invoke()
        } else {
            onShizukuPermissionDenied?.invoke()
        }
        onShizukuPermissionGranted = null
        onShizukuPermissionDenied = null
    }

    fun requestShizukuAccess(
        onPermissionGranted: () -> Unit,
        onPermissionUnavailable: () -> Unit,
        onPermissionDenied: () -> Unit
    ) {
        if (!runCatching { Shizuku.pingBinder() }.getOrDefault(false)) {
            onPermissionUnavailable()
            return
        }
        val permission = runCatching { Shizuku.checkSelfPermission() }.getOrNull()
        if (permission == null) {
            onPermissionUnavailable()
            return
        }
        if (permission == PackageManager.PERMISSION_GRANTED) {
            bindShizuku()
            onPermissionGranted()
            return
        }
        if (runCatching { Shizuku.shouldShowRequestPermissionRationale() }.getOrDefault(false)) {
            onPermissionDenied()
            return
        }
        onShizukuPermissionGranted = onPermissionGranted
        onShizukuPermissionDenied = onPermissionDenied
        runCatching {
            Shizuku.requestPermission(acRequestCode)
        }.onFailure {
            onShizukuPermissionGranted = null
            onShizukuPermissionDenied = null
            onPermissionUnavailable()
        }
    }

    fun requestAppListAccess(
        onPermissionGranted: () -> Unit,
        onPermissionDenied: () -> Unit
    ) {
        if (!supportsRuntimeAppListPermission() || hasAppListPermission()) {
            onPermissionGranted()
            return
        }
        onAppListPermissionGranted = onPermissionGranted
        onAppListPermissionDenied = onPermissionDenied
        ActivityCompat.requestPermissions(
            this,
            arrayOf(GET_INSTALLED_APPS_PERMISSION),
            appListRequestCode
        )
    }

    private fun supportsRuntimeAppListPermission(): Boolean = runCatching {
        packageManager.getPermissionInfo(
            GET_INSTALLED_APPS_PERMISSION,
            0
        ).packageName == "com.lbe.security.miui"
    }.getOrDefault(false)

    private fun hasAppListPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            GET_INSTALLED_APPS_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == appListRequestCode) {
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                onAppListPermissionGranted?.invoke()
            } else {
                onAppListPermissionDenied?.invoke()
            }
            onAppListPermissionGranted = null
            onAppListPermissionDenied = null
        }
    }

    fun openAppSettings() {
        startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
            }
        )
    }

    fun openShizuku() {
        val shizukuPackage = "moe.shizuku.privileged.api"
        val launchIntent = packageManager.getLaunchIntentForPackage(shizukuPackage)
        if (launchIntent != null) {
            startActivity(launchIntent)
        } else {
            startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("https://shizuku.rikka.app/"))
            )
        }
    }

    private fun bindGrantedShizuku() {
        if (hasGrantedShizukuPermission()) {
            bindShizuku()
        }
    }

    private fun hasGrantedShizukuPermission(): Boolean = runCatching {
        Shizuku.pingBinder() &&
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    }.getOrDefault(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        attachNavigationEventInput()
        Shizuku.addRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER)
        RootReceivedListener.setListener(object : IRootListener {
            override fun onRootReceived() {
                bindRoot()
            }
        })
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides this) {
                LanguageSelector {
                    Navigation(
                        activityResumeCount = activityResumeCount.intValue,
                        requestAppListAccess = ::requestAppListAccess,
                        requestShizukuAccess = ::requestShizukuAccess,
                        openAppSettings = ::openAppSettings,
                        openShizuku = ::openShizuku
                    )
                }
            }
        }

        bindGrantedShizuku()
    }

    override fun onResume() {
        super.onResume()
        activityResumeCount.intValue++
        if (
            hasGrantedShizukuPermission() &&
            !UserServiceProvider.isConnected(OperationMode.SHIZUKU)
        ) {
            bindShizuku()
        }
    }

    override fun onDestroy() {
        Shizuku.removeRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER)
        onShizukuPermissionGranted = null
        onShizukuPermissionDenied = null
        onAppListPermissionGranted = null
        onAppListPermissionDenied = null
        navigationEventInput?.let(navigationEventDispatcher::removeInput)
        navigationEventInput = null
        navigationEventDispatcher.dispose()
        RootReceivedListener.destroy()
        if (!isChangingConfigurations) {
            if (UserServiceProvider.isConnected(OperationMode.ROOT)) {
                runCatching { RootService.unbind(UserServiceProvider.rootConnection) }
                UserServiceProvider.rootConnection.clear()
            }
            if (UserServiceProvider.isConnected(OperationMode.SHIZUKU)) {
                runCatching {
                    Shizuku.unbindUserService(
                    ShizukuArgs.userServiceArgs,
                    UserServiceProvider.shizukuConnection,
                    true
                )
                }
                UserServiceProvider.shizukuConnection.clear()
            }
        }
        super.onDestroy()
    }

    private fun attachNavigationEventInput() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            navigationEventInput = OnBackInvokedDefaultInput(onBackInvokedDispatcher).also {
                navigationEventDispatcher.addInput(it)
            }
        }
    }

}
