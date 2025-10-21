package com.surzhykov.navsymphony.choreographer.common

import com.surzhykov.navsymphony.choreographer.presentation.NavigationContext
import com.surzhykov.navsymphony.domain.Logger
import com.surzhykov.navsymphony.screen.core.ScreenRoute
import com.surzhykov.navsymphony.window.core.AbstractOverlayWindow

/**
 * Represents a command for navigation within the application.
 * This sealed class provides a set of predefined navigation actions.
 */
sealed class NavigationCommand {

    /**
     * Executes the navigation command within the given navigation context.
     *
     * @param context The context providing access to the navigation and overlay management tools.
     */
    abstract fun execute(context: NavigationContext)

    /**
     * Represents a navigation command to navigate to a specific screen.
     *
     * @property route The route representing the destination screen.
     */
    data class NavigateTo(
        private val route: ScreenRoute,
        private val navOptions: NavigationOptions,
        private val logger: Logger = navigationLogger(),
    ) : NavigationCommand() {
        /**
         * Executes the navigation to the specified screen.
         *
         * @param context The context providing access to the navigation controller.
         */
        override fun execute(context: NavigationContext) {
            logger.d(TAG, "NavigateTo: $route, with options: $navOptions")
            context.navigationController.navigate(route, navOptions)
        }
    }

    /**
     * Represents a navigation command to pop up to a specific screen in the back stack.
     *
     * @property route The route representing the target screen to pop up to.
     * @property inclusive Whether to include the target screen in the pop operation.
     */
    data class PopUpTo(
        val route: ScreenRoute,
        val inclusive: Boolean,
        private val logger: Logger = navigationLogger(),
    ) : NavigationCommand() {

        /**
         * Executes the pop-up to the specified screen.
         *
         * @param context The context providing access to the navigation controller.
         */
        override fun execute(context: NavigationContext) {
            logger.d(TAG, "PopUpTo: $route")
            context.navigationController.navigateBackTo(route, inclusive)
        }
    }

    /**
     * Represents a navigation command to navigate back to the previous screen.
     */
    data class Back(
        private val logger: Logger = navigationLogger(),
    ) : NavigationCommand() {

        /**
         * Executes the navigation back to the previous screen.
         *
         * @param context The context providing access to the navigation controller.
         */
        override fun execute(context: NavigationContext) {
            logger.d(TAG, "Back")
            context.navigationController.navigateBack() // Navigate back to the previous screen
        }
    }

    /**
     * Represents a navigation command to display a dialog window.
     *
     * @property overlayWindow The dialog window to be displayed.
     */
    data class Dialog(
        private val overlayWindow: AbstractOverlayWindow,
        private val dialogIdToDismiss: String? = null,
        private val logger: Logger = navigationLogger(),
    ) : NavigationCommand() {

        /**
         * Executes the display of the specified dialog window.
         *
         * @param context The context providing access to the overlay manager.
         */
        override fun execute(context: NavigationContext) {
            logger.d(TAG, "Display Dialog: $overlayWindow")
            // Dismiss the previous dialog window, if any
            if (dialogIdToDismiss != null) context.overlayManager.dismissOverlay(dialogIdToDismiss)

            // Show the dialog window with the provided onDismiss action
            context.overlayManager.showOverlay(overlayWindow)
        }
    }

    /**
     * Represents a command to dismiss a specific dialog window.
     *
     * @property dialogId The unique identifier of the dialog to be dismissed.
     */
    data class DismissDialog(
        private val dialogId: String,
        private val logger: Logger = navigationLogger(),
    ) : NavigationCommand() {
        override fun execute(context: NavigationContext) {
            logger.d(TAG, "Dismiss Dialog: $dialogId")
            context.overlayManager.dismissOverlay(dialogId)
        }
    }

    /**
     * Represents a navigation command to clear the entire back stack.
     */
    data class ClearBackStack(
        private val logger: Logger = navigationLogger(),
    ) : NavigationCommand() {

        /**
         * Executes the clearing of the entire back stack.
         *
         * @param context The context providing access to the navigation controller.
         */
        override fun execute(context: NavigationContext) {
            logger.d(TAG, "ClearBackStack")
            context.navigationController.clearBackStack() // Clear the entire back stack
        }
    }

    /** Constants for this sealed class */
    companion object {
        private val TAG = NavigationCommand::class.java.simpleName
    }
}