package vegabobo.languageselector.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneInfo
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.scene.rememberSceneState
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.NavDisplayTransitionEffects
import androidx.navigation3.ui.defaultPopTransitionSpec
import androidx.navigation3.ui.defaultPredictivePopTransitionSpec
import androidx.navigation3.ui.defaultTransitionSpec
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.NavigationEventState
import androidx.navigationevent.compose.rememberNavigationEventState
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import kotlinx.serialization.Serializable
import vegabobo.languageselector.ui.screen.about.AboutScreen
import vegabobo.languageselector.ui.screen.appinfo.AppInfoScreen
import vegabobo.languageselector.ui.screen.main.MainScreen

@Serializable
sealed interface AppRoute : NavKey {
    @Serializable
    data object Home : AppRoute

    @Serializable
    data class AppInfo(val packageName: String) : AppRoute

    @Serializable
    data object About : AppRoute
}

private class Navigator(
    val backStack: MutableList<NavKey>
) {
    fun push(key: NavKey) {
        if (backStack.lastOrNull() == key) return
        backStack.add(key)
    }

    fun pop() {
        if (backStack.size <= 1) return
        backStack.removeAt(backStack.lastIndex)
    }

    fun current(): NavKey? = backStack.lastOrNull()
}

@Composable
fun Navigation() {
    val backStack = rememberNavBackStack(AppRoute.Home)
    val navigator = remember(backStack) { Navigator(backStack) }
    var gestureState: NavigationEventState<SceneInfo<NavKey>>? = null

    val entries = rememberDecoratedNavEntries(
        backStack = navigator.backStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
            NavEntryDecorator { content ->
                Box {
                    content.Content()
                }
            }
        ),
        entryProvider = entryProvider {
            entry<AppRoute.Home> {
                MainScreen(
                    navigateToAppScreen = { navigator.push(AppRoute.AppInfo(it)) },
                    navigateToAbout = { navigator.push(AppRoute.About) }
                )
            }
            entry<AppRoute.AppInfo> { key ->
                AppInfoScreen(
                    appId = key.packageName,
                    navigateBack = { navigator.pop() }
                )
            }
            entry<AppRoute.About> {
                AboutScreen(navigateBack = { navigator.pop() })
            }
        }
    )

    val sceneState = rememberSceneState(
        entries = entries,
        sceneStrategies = listOf(SinglePaneSceneStrategy()),
        sceneDecoratorStrategies = emptyList(),
        sharedTransitionScope = null,
        onBack = { navigator.pop() },
    )
    val scene = sceneState.currentScene
    val currentInfo = SceneInfo(scene)
    val previousSceneInfos = sceneState.previousScenes.map { SceneInfo(it) }
    gestureState = rememberNavigationEventState(
        currentInfo = currentInfo,
        backInfo = previousSceneInfos
    )

    NavigationBackHandler(
        state = gestureState,
        isBackEnabled = scene.previousEntries.isNotEmpty(),
        onBackCompleted = { navigator.pop() }
    )

    NavDisplay(
        sceneState = sceneState,
        navigationEventState = gestureState,
        contentAlignment = Alignment.TopStart,
        sizeTransform = null,
        transitionEffects = NavDisplayTransitionEffects(
            blockInputDuringTransition = true
        ),
        predictivePopTransitionSpec = { swipeEdge ->
            defaultPredictivePopTransitionSpec<NavKey>().invoke(this, swipeEdge)
        },
        popTransitionSpec = {
            defaultPopTransitionSpec<NavKey>().invoke(this)
        },
        transitionSpec = {
            defaultTransitionSpec<NavKey>().invoke(this)
        }
    )
}
