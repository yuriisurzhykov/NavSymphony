package com.surzhykov.navsymphony.choreographer.presentation

import androidx.compose.runtime.Stable
import com.surzhykov.navsymphony.choreographer.common.NavigationOptions
import com.surzhykov.navsymphony.screen.core.ScreenRoute

interface NavigationRouter {

    @Stable
    fun <T : ScreenRoute> navigate(
        route: T,
        navOptionsBuilder: NavigationOptions.() -> Unit = {}
    )

    /**
     * Navigates to the specified route.
     *
     * @param route The [ScreenRoute] to navigate to.
     */
    @Stable
    fun <T : ScreenRoute> navigate(
        route: T,
        navOptions: NavigationOptions
    )

    /**
     * Navigates back to the previous screen in the back stack.
     */
    @Stable
    fun navigateBack()

    /**
     * Navigates back to a specific route in the back stack.
     *
     * @param route The [ScreenRoute] to navigate back to.
     */
    @Stable
    fun navigateBackTo(route: ScreenRoute, inclusive: Boolean)

    /**
     * Clears the entire back stack, returning to the home screen.
     */
    @Stable
    fun clearBackStack()

    /**
     * Sends a signal to the `NavigationChoreographer` that a navigation transaction step is
     * finished and it can proceed with the next transaction intent or with the original intent.
     * @param currentRoute The route of the current screen.
     * */
    @SensitiveNavigationApi
    fun completeNavTransactionStep(currentRoute: ScreenRoute)
}