package com.surzhykov.navsymphony.choreographer.common

import com.surzhykov.navsymphony.domain.Logger
import com.surzhykov.navsymphony.screen.core.ScreenRoute
import com.surzhykov.navsymphony.window.core.AbstractOverlayWindow

/**
 * Represents an intent to perform a navigation action within the application.
 * This is a sealed class, which means all its subclasses are known at compile time,
 * allowing for exhaustive `when` expressions when handling different types of navigation intents.
 *
 * @property sender The entity that initiated the navigation intent.
 * @property priority The priority of this navigation intent, higher values indicate higher priority.
 * @property timeOccurred The timestamp when the navigation intent was created. Used primarily for debugging.
 */
sealed class NavigationIntent(
    open val sender: IntentSender,
    open val priority: Int,
    // This parameter is primarily for debugging and tracking purposes.
    open val timeOccurred: Long = System.currentTimeMillis(),
) {

    abstract fun transformToCommand(): NavigationCommand

    /**
     * Represents a request to go back to the previous screen.
     *
     * @property sender The entity that initiated the back navigation.
     * @property priority The priority of this back navigation.
     */
    class Back(
        override val sender: IntentSender,
        override val priority: Int,
    ) : NavigationIntent(sender, priority) {
        override fun transformToCommand(): NavigationCommand = NavigationCommand.Back()
    }

    /**
     * Represents a request to navigate to a specific route, potentially popping up to that route.
     *
     * @property route The destination route to navigate to.
     * @property inclusive Whether to include the specified route in the pop-up. If true the given
     * route will also be popped.
     * @property sender The entity that initiated the navigation.
     * @property priority The priority of this navigation.
     */
    data class PopUpTo(
        internal val route: ScreenRoute,
        private val inclusive: Boolean,
        override val sender: IntentSender,
        override val priority: Int,
    ) : NavigationIntent(sender, priority) {
        override fun transformToCommand(): NavigationCommand =
            NavigationCommand.PopUpTo(route, inclusive)
    }

    /**
     * Represents a request to navigate to a specific route.
     *
     * @property route The destination route to navigate to.
     * @property navOptions Navigation options for this navigation intent.
     * @property sender The entity that initiated the navigation.
     * @property priority The priority of this navigation.
     */
    data class NavigateTo(
        internal val route: ScreenRoute,
        internal val navOptions: NavigationOptions,
        override val sender: IntentSender,
        override val priority: Int,
        private val logger: Logger = navigationLogger(),
    ) : NavigationIntent(sender, priority) {
        override fun transformToCommand(): NavigationCommand =
            NavigationCommand.NavigateTo(route, navOptions, logger)
    }

    /**
     * Represents a request to display a dialog.
     *
     * @property dialog The dialog window to display.
     * @property sender The entity that requested the dialog to be shown.
     * @property priority The priority of showing this dialog.
     */
    data class DisplayDialog(
        internal val dialog: AbstractOverlayWindow,
        override val sender: IntentSender,
        override val priority: Int,
        private val dialogIdToDismiss: String? = null,
        private val logger: Logger = navigationLogger(),
    ) : NavigationIntent(sender, priority) {

        override fun transformToCommand(): NavigationCommand =
            NavigationCommand.Dialog(dialog, dialogIdToDismiss, logger)
    }

    data class DismissOverlay(
        override val sender: IntentSender,
        override val priority: Int,
        private val dialogId: String,
        private val logger: Logger = navigationLogger(),
    ) : NavigationIntent(sender, priority) {
        override fun transformToCommand(): NavigationCommand =
            NavigationCommand.DismissDialog(dialogId, logger)
    }

    /**
     * Represents an event indicating a timeout due to user inactivity.
     *
     * @property priority The priority of this timeout event.
     */
    class InteractionTimeout(
        override val priority: Int,
        private val logger: Logger = navigationLogger(),
    ) : NavigationIntent(IntentSender.System, priority) {
        override fun transformToCommand(): NavigationCommand =
            NavigationCommand.ClearBackStack(logger)
    }

    /**
     * Represents a request to completely clear the back stack.
     * This effectively resets the navigation history.
     *
     * @property sender The entity that initiated the clear back stack request.
     * @property priority The priority of this clear back stack operation.
     */
    class ClearBackStack(
        override val sender: IntentSender,
        override val priority: Int,
        private val logger: Logger = navigationLogger(),
    ) : NavigationIntent(sender, priority) {
        override fun transformToCommand(): NavigationCommand =
            NavigationCommand.ClearBackStack(logger)
    }

    /**
     * Represents a completion signal for a navigation transaction.
     *
     * This intent is used internally within the navigation system to indicate that a previously
     * initiated navigation transaction has been successfully completed. It serves as a signal to
     * proceed with the original navigation intent.
     *
     * This class is not intended for direct usage by application developers. It's designed to
     * manage internal state and communication within the navigation framework.
     *
     * @property route The [ScreenRoute] associated with the completed navigation transaction.
     * This route represents the final destination of the navigation.
     */
    data class CompleteNavTransaction(val route: ScreenRoute) :
        NavigationIntent(IntentSender.System, 0) {
        override fun transformToCommand(): NavigationCommand {
            throw IllegalAccessException(
                "This intent doesn't support transformation to UI navigation command. " +
                        "It works as a signal to proceed with original navigation intent!"
            )
        }
    }
}
