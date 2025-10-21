package com.surzhykov.navsymphony.choreographer.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import com.surzhykov.navsymphony.screen.core.ScreenRoute
import com.surzhykov.navsymphony.window.core.LocalOverlayManager
import com.surzhykov.navsymphony.window.core.OverlayManager

/**
 * A CompositionLocal that provides access to the [NavigationController].
 *
 * It allows accessing the navigation controller from anywhere in the Compose hierarchy.
 * If no `NavigationController` is provided, it will throw an error.
 */
val LocalNavigation = staticCompositionLocalOf<NavigationRouter> {
    error("No NavigationController was provided in the composition!")
}

/**
 * Remembers and provides an instance of [UserComponentIntentActor].
 *
 * This function creates and remembers a [UserComponentIntentActor] instance
 * within the composition. The remembered instance will be reused across recompositions.
 *
 * @return An instance of [UserComponentIntentActor].
 */
@Composable
fun rememberUserNavigationComponent(): UserComponentIntentActor {
    // Creates and remembers UserComponentIntentActor during the first composition.
    return remember { UserComponentIntentActor() }
}

/**
 * Remembers and provides a [NavigationContext] instance.
 *
 * This function creates and remembers a [NavigationContext] instance, which combines a
 * [NavigationController] and an [LocalOverlayManager].
 */
@Composable
fun rememberNavigationContext(
    homeRoute: ScreenRoute,
    navController: NavHostController,
    overlayManager: OverlayManager = LocalOverlayManager.current,
): NavigationContext {
    val navigationController = rememberNavigationController(homeRoute, navController)

    // Creates and remembers NavigationContext when either controller or manager changed.
    return remember(navigationController, overlayManager) {
        NavigationContext(navigationController, overlayManager)
    }
}