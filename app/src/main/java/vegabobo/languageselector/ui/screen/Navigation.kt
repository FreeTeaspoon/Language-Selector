package vegabobo.languageselector.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.serialization.Serializable
import top.yukonga.miuix.kmp.nav.core.NavBackStack
import top.yukonga.miuix.kmp.nav.core.NavDisplay
import top.yukonga.miuix.kmp.nav.core.NavKey
import top.yukonga.miuix.kmp.nav.core.rememberNavBackStack
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
    requestAppListAccess: (() -> Unit) -> Unit,
    requestShizukuAccess: (() -> Unit) -> Unit
) {
    val backStack = rememberNavBackStack<AppRoute>(AppRoute.Home)
    val navigator = remember(backStack) { Navigator(backStack) }
    NavDisplay(
        backStack = navigator.backStack,
        onBack = { navigator.pop() },
    ) {
        entry<AppRoute.Home> {
            MainScreen(
                activityResumeCount = activityResumeCount,
                navigateToAppScreen = { navigator.push(AppRoute.AppInfo(it)) },
                navigateToHistory = { navigator.push(AppRoute.History) },
                navigateToAbout = { navigator.push(AppRoute.About) },
                requestAppListAccess = requestAppListAccess,
                requestShizukuAccess = requestShizukuAccess,
            )
        }
        entry<AppRoute.AppInfo> { key ->
            AppInfoScreen(
                appId = key.packageName,
                navigateBack = { navigator.pop() },
            )
        }
        entry<AppRoute.About> {
            AboutScreen(navigateBack = { navigator.pop() })
        }
        entry<AppRoute.History> {
            HistoryScreen(
                navigateBack = { navigator.pop() },
                navigateToApp = { navigator.push(AppRoute.AppInfo(it)) },
            )
        }
    }
}
