package com.surzhykov.navsymphony.choreographer.presentation

import android.annotation.SuppressLint
import androidx.annotation.IdRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.serialization.generateHashCode
import androidx.navigation.serialization.generateRouteWithArgs
import com.surzhykov.navsymphony.choreographer.common.NavigationOptions
import com.surzhykov.navsymphony.choreographer.common.navigationLogger
import com.surzhykov.navsymphony.choreographer.data.NavigationBackStack
import com.surzhykov.navsymphony.domain.Logger
import com.surzhykov.navsymphony.screen.core.ScreenRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer

/**
 * Creates and remembers a [NavigationController] instance.
 *
 * This function is designed to be used within a Compose composable function. It provides a
 * stable instance of [NavigationController] across recompositions.
 *
 * @param homeRoute The route of the home screen, used for clearing the back stack.
 * @param navController The [NavHostController] used for navigation. If not provided, a new one
 *   is created using [rememberNavController].
 * @return A remembered [NavigationController] instance.
 */
@Composable
fun rememberNavigationController(
    homeRoute: ScreenRoute,
    navController: NavHostController = rememberNavController(),
): NavigationController {
    // remember() function ensures that the same instance of NavigationController is used across
    // recompositions.
    val coroutineScope = rememberCoroutineScope()
    return remember(navController, homeRoute) {
        NavigationController.Base(homeRoute, navController, coroutineScope)
    }
}

/**
 * An interface for controlling navigation within the application.
 *
 * This interface provides methods for navigating to different screens, going back, and clearing
 * the navigation back stack.
 */
@Immutable
interface NavigationController : NavigationRouter {

    /**
     * An actual implementation of the [NavigationController] interface.
     *
     * @param homeRoute The route of the home screen.
     * @param navigator The [NavHostController] used for managing navigation.
     */
    @Immutable
    @SuppressLint("RestrictedApi")
    class Base(
        private val homeRoute: ScreenRoute,
        private val navigator: NavHostController,
        coroutineScope: CoroutineScope,
        private val backStack: NavigationBackStack<Pair<String, ScreenRoute>> = NavigationBackStack(),
        private val logger: Logger = navigationLogger(),
    ) : NavigationController {

        init {
            // TODO: Try to implement this in a better way.
            //  https://stackoverflow.com/questions/69806098/laggy-slow-navigation-between-bottomnavigation-composables-jetpack-compose
            coroutineScope.launch {
                // Trying to wait until navigation graph (Jetpack Compose graph) is initialized.
                // After the backstack is not empty anymore it means that graph is initialized
                // and we can add the home screen to the backstack.
                navigator.currentBackStack.first { it.isNotEmpty() }
                backStack.add(
                    generateRouteFilled(homeRoute) to homeRoute,
                    NavigationOptions()
                )
            }
        }

        @Stable
        override fun <T : ScreenRoute> navigate(
            route: T,
            navOptionsBuilder: NavigationOptions.() -> Unit,
        ) = navigate(route, NavigationOptions().apply(navOptionsBuilder))

        @Stable
        override fun <T : ScreenRoute> navigate(
            route: T,
            navOptions: NavigationOptions,
        ) {
            navigator.navigate(route) {
                launchSingleTop = navOptions.singleTop
                if (navOptions.clearBackStack) {
                    popUpTo(homeRoute)
                }
            }.also {
                val routeToAdd =
                    navigator.currentBackStackEntry?.destination?.route.orEmpty() to route
                backStack.add(routeToAdd, navOptions)
            }
        }

        @Stable
        override fun navigateBack() {
            try {
                val routeToReturn = backStack.pop()
                navigator.popBackStack(routeToReturn.first, false)
            } catch (e: NoSuchElementException) {
                // If the pop operation failed it means that the back stack considered to be empty,
                // which means something went wrong.
                logger.e(TAG, "Stack is already empty!", e)
            }
        }

        @Stable
        override fun navigateBackTo(route: ScreenRoute, inclusive: Boolean) {
            try {
                val routeToReturn = backStack.popUntil(inclusive) {
                    it.second == route
                }
                navigator.popBackStack(routeToReturn.first, inclusive)
            } catch (e: IllegalStateException) {
                logger.e(TAG, "Stack is already empty: $route", e)
            } catch (e: NoSuchElementException) {
                logger.e(TAG, "Couldn't find route to return to: $route", e)
            }
        }

        @Stable
        override fun clearBackStack() {
            backStack.clear()
            navigator.popBackStack(homeRoute, false)
            backStack.add(
                navigator.currentBackStackEntry?.destination?.route.orEmpty() to homeRoute,
                NavigationOptions()
            )
        }

        @SensitiveNavigationApi
        override fun completeNavTransactionStep(currentRoute: ScreenRoute) {
            throw IllegalAccessException(
                "This method is not supported in NavigationController.Base! It only works " +
                        "through UserComponentIntentActor! Because a specific intent should be " +
                        "published to proceed with original navigation intent!"
            )
        }

        @OptIn(InternalSerializationApi::class)
        private fun <T : Any> generateRouteFilled(route: T): String {
            val id = route::class.serializer().generateHashCode()
            val destination = navigator.graph.findDestinationComprehensive(id, true)
            // throw immediately if destination is not found within the graph
            requireNotNull(destination) {
                "Destination with route ${route::class.simpleName} cannot be found " +
                        "in navigation graph ${navigator.graph}"
            }
            return generateRouteWithArgs(
                route,
                // get argument typeMap
                destination.arguments.mapValues { it.value.type }
            )
        }

        private fun NavDestination.findDestinationComprehensive(
            @IdRes destinationId: Int,
            searchChildren: Boolean,
            matchingDest: NavDestination? = null,
        ): NavDestination? {
            if (id == destinationId) {
                when {
                    // check parent in case of duplicated destinations to ensure it finds the correct
                    // nested destination
                    matchingDest != null ->
                        if (this == matchingDest && this.parent == matchingDest.parent) return this

                    else -> return this
                }
            }
            val currentGraph = if (this is NavGraph) this else parent!!
            return currentGraph.findNodeComprehensive(
                destinationId,
                currentGraph,
                searchChildren,
                matchingDest
            )
        }

        companion object {
            private val TAG = NavigationController::class.java.simpleName
        }
    }
}