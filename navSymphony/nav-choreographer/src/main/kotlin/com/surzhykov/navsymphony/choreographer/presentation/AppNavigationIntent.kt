package com.surzhykov.navsymphony.choreographer.presentation

import com.surzhykov.navsymphony.choreographer.common.NavigationIntent
import com.surzhykov.navsymphony.choreographer.common.NavigationOptions
import com.surzhykov.navsymphony.screen.core.ScreenRoute
import com.surzhykov.navsymphony.window.core.AbstractOverlayWindow

/**
 * Maps an [AppNavigationIntent] to a [NavigationIntent].
 *
 * This function takes an [AppNavigationIntent] and an [AppComponentIntentActor] and returns
 * a corresponding [NavigationIntent]. It uses a when expression to handle different types
 * of [AppNavigationIntent] and creates the appropriate [NavigationIntent] based on the input.
 *
 * @param actor The [AppComponentIntentActor] providing information about the navigation sender
 * and priority.
 * @return The mapped [NavigationIntent].
 */
internal fun AppNavigationIntent.map(actor: AppComponentIntentActor): NavigationIntent {
    return when (this) {
        is AppNavigationIntent.Back            -> NavigationIntent.Back(
            actor.sender,
            actor.defaultPriority
        )
        is AppNavigationIntent.NavigateTo      -> NavigationIntent.NavigateTo(
            route,
            navOptions,
            actor.sender,
            actor.defaultPriority
        )

        is AppNavigationIntent.PopUpTo         -> NavigationIntent.PopUpTo(
            route,
            inclusive,
            actor.sender,
            actor.defaultPriority
        )

        is AppNavigationIntent.ClearBackStack  -> NavigationIntent.ClearBackStack(
            actor.sender,
            actor.defaultPriority
        )

        is AppNavigationIntent.Dialog          -> NavigationIntent.DisplayDialog(
            dialog = dialog,
            sender = actor.sender,
            priority = actor.defaultPriority
        )
    }
}

/**
 * Represents the different types of navigation intents that can be initiated within the application
 * by user or system interactions.
 *
 * This sealed interface defines a set of possible navigation actions. Each data class or object
 * represents a specific type of navigation intent.
 */
sealed interface AppNavigationIntent {
    /**
     * Represents a request to navigate back to the previous screen.
     */
    data object Back : AppNavigationIntent

    /**
     * Represents a request to clear the entire back stack, removing all previously visited screens.
     */
    data object ClearBackStack : AppNavigationIntent

    /**
     * Represents a request to pop up to a specific screen in the back stack.
     *
     * @param route The route of the screen to pop up to.
     * @param inclusive If true, the target route will also be removed from the back stack.
     */
    data class PopUpTo(
        val route: ScreenRoute,
        val inclusive: Boolean,
    ) : AppNavigationIntent

    /**
     * Represents a request to navigate to a new screen.
     *
     * @param route The route of the screen to navigate to.
     * @param navOptions Navigation option holder which provides with ability to manage how route
     * will be opened and processed.
     */
    data class NavigateTo(
        val route: ScreenRoute,
        val navOptions: NavigationOptions = NavigationOptions(),
    ) : AppNavigationIntent

    /**
     * Represents a request to display an overlay dialog (alert) over a screen.
     * */
    data class Dialog(
        val dialog: AbstractOverlayWindow,
    ) : AppNavigationIntent
}