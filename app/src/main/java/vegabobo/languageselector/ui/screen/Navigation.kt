package vegabobo.languageselector.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.serialization.Serializable
import top.yukonga.miuix.kmp.nav.core.NavBackStack
import top.yukonga.miuix.kmp.nav.core.NavCornerClipMode
import top.yukonga.miuix.kmp.nav.core.NavDisplay
import top.yukonga.miuix.kmp.nav.core.NavDisplayEffects
import top.yukonga.miuix.kmp.nav.core.NavKey
import top.yukonga.miuix.kmp.nav.core.rememberNavBackStack
import top.yukonga.miuix.kmp.nav.core.rememberNavSystemCornerRadius
import top.yukonga.miuix.kmp.nav.transition.NavSwipeDirection
import top.yukonga.miuix.kmp.nav.transition.NavTransitions
import top.yukonga.miuix.kmp.theme.MiuixTheme
import vegabobo.languageselector.ui.screen.about.AboutScreen
import vegabobo.languageselector.ui.screen.appinfo.AppInfoScreen
import vegabobo.languageselector.ui.screen.history.HistoryScreen
import vegabobo.languageselector.ui.screen.main.MainScreen

@Serializable
sealed interface AppRoute : NavKey {
    @Serializable
    data object Home : AppRoute

    @Serializable
    data class AppInfo(val packageName: String) : AppRoute

    @Serializable
    data object About : AppRoute

    @Serializable
    data object History : AppRoute
}

private class Navigator(
    val backStack: NavBackStack,
) {
    fun push(key: NavKey) {
        if (backStack.lastOrNull() == key) return
        backStack.add(key)
    }

    fun pop() {
        if (backStack.size <= 1) return
        backStack.removeLastOrNull()
    }

    fun current(): NavKey? = backStack.lastOrNull()
}

@Composable
fun Navigation(
    activityResumeCount: Int,
    requestAppListAccess: (onGranted: () -> Unit, onDenied: () -> Unit) -> Unit,
    requestShizukuAccess: (
        onGranted: () -> Unit,
        onUnavailable: () -> Unit,
        onDenied: () -> Unit
    ) -> Unit,
    openAppSettings: () -> Unit,
    openShizuku: () -> Unit
) {
    val backStack = rememberNavBackStack<AppRoute>(AppRoute.Home)
    val navigator = remember(backStack) { Navigator(backStack) }
    val navCornerRadius = rememberNavSystemCornerRadius()
    val swipeBackDirection = when (LocalLayoutDirection.current) {
        LayoutDirection.Rtl -> NavSwipeDirection.RightToLeft
        else -> NavSwipeDirection.LeftToRight
    }
    NavDisplay(
        backStack = navigator.backStack,
        onBack = { navigator.pop() },
        transition = NavTransitions.MiuixDefault,
        effects = NavDisplayEffects(
            enableCornerClip = true,
            cornerClipRadius = navCornerRadius,
            cornerClipMode = NavCornerClipMode.Leading,
            dimAmount = 0.5f,
            blockInputDuringTransition = false,
            backdropColor = MiuixTheme.colorScheme.surface,
        ),
    ) {
        entry<AppRoute.Home>(swipeDismiss = swipeBackDirection) {
            MainScreen(
                activityResumeCount = activityResumeCount,
                navigateToAppScreen = { navigator.push(AppRoute.AppInfo(it)) },
                navigateToHistory = { navigator.push(AppRoute.History) },
                navigateToAbout = { navigator.push(AppRoute.About) },
                requestAppListAccess = requestAppListAccess,
                requestShizukuAccess = requestShizukuAccess,
                openAppSettings = openAppSettings,
                openShizuku = openShizuku,
            )
        }
        entry<AppRoute.AppInfo>(swipeDismiss = swipeBackDirection) { key ->
            AppInfoScreen(
                appId = key.packageName,
                navigateBack = { navigator.pop() },
            )
        }
        entry<AppRoute.About>(swipeDismiss = swipeBackDirection) {
            AboutScreen(navigateBack = { navigator.pop() })
        }
        entry<AppRoute.History>(swipeDismiss = swipeBackDirection) {
            HistoryScreen(
                navigateBack = { navigator.pop() },
                navigateToApp = { navigator.push(AppRoute.AppInfo(it)) },
            )
        }
    }
}
