package com.surzhykov.navsymphony.screen.shared

import androidx.annotation.CallSuper
import androidx.lifecycle.viewModelScope
import com.surzhykov.navsymphony.choreographer.presentation.AppNavigationIntent
import com.surzhykov.navsymphony.core.presentation.DrawableResolver
import com.surzhykov.navsymphony.core.presentation.StringResolver
import com.surzhykov.navsymphony.domain.Dispatcher
import com.surzhykov.navsymphony.domain.Logger
import com.surzhykov.navsymphony.screen.core.ScreenIntent
import com.surzhykov.navsymphony.screen.core.ScreenState
import com.surzhykov.navsymphony.screen.core.ScreenStateViewModel
import com.surzhykov.navsymphony.screen.core.StateCommunication
import com.surzhykov.navsymphony.window.core.AbstractOverlayWindow
import com.surzhykov.navsymphony.window.dialogs.PrimaryMessageDialog
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Base class for screens that participate in app-level navigation flow.
 *
 * Extends [ScreenStateViewModel] to provide screen state management, while also exposing a
 * side-effect channel [navigationQueue] for one-time navigation commands (e.g. screen transitions,
 * overlays). This allows separation between persistent screen state and transient navigation events.
 *
 * Navigation commands are emitted via [navigate] (suspend) or [launchNavigation]
 * (async fire-and-forget). These events are collected at the UI layer and routed via
 * `NavigationChoreographer`.
 *
 * Usage example:
 * ```
 * viewModel.launchNavigation(AppNavigationIntent.NavigateTo(DestinationRoute))
 * ```
 *
 * @param State Type of the UI state for the screen.
 * @param Intent Type of the UI intent representing user interaction.
 * @param communication State communication holder (mutable).
 * @param dispatcher Coroutine dispatcher used for background execution.
 */
abstract class NavigationViewModel<State : ScreenState, Intent : ScreenIntent>(
    communication: StateCommunication.Mutable<State>,
    private val dispatcher: Dispatcher,
    private val logger: Logger = Logger.AndroidLogger,
) : ScreenStateViewModel<State, Intent>(communication, dispatcher) {

    private val navigationIntentQueue =
        MutableSharedFlow<AppNavigationIntent>(replay = 0, extraBufferCapacity = 1)

    private val windowMutableStateFlow = MutableStateFlow<AbstractOverlayWindow?>(null)

    /**
     * A read-only flow of one-time navigation intents triggered by the ViewModel.
     *
     * Collected in the UI to dispatch navigation side-effects via `AppComponentIntentActor`.
     */
    val navigationQueue: SharedFlow<AppNavigationIntent> = navigationIntentQueue.asSharedFlow()

    /**
     * A read-only flow of the overlay window. The UI should subscribe to it and display using
     * `OverlayManager` and then dispose when either new window occurred or screen is removed
     * from stack.
     * */
    val overlayWindowState: StateFlow<AbstractOverlayWindow?> = windowMutableStateFlow.asStateFlow()

    /**
     * Suspends until the navigation intent is successfully emitted.
     *
     * Use this method if navigation should occur after a blocking operation (e.g. after use case
     * execution).
     *
     * @param intent Navigation intent to emit.
     */
    protected suspend fun navigate(intent: AppNavigationIntent) {
        navigationIntentQueue.emit(intent)
    }

    /**
     * Emits a navigation event asynchronously from a background coroutine.
     *
     * This is the preferred method for triggering navigation without blocking the UI thread.
     *
     * @param intent Navigation intent to emit (e.g. `NavigateTo`, `DisplayOverlay`).
     */
    protected fun launchNavigation(intent: AppNavigationIntent) {
        dispatcher.launchBackground(viewModelScope) { navigationIntentQueue.emit(intent) }
    }

    /**
     * Creates a new instance of [PrimaryMessageDialog] with [severity], [title] and [message],
     * and then pushes it to the [overlayWindowState] through [windowMutableStateFlow].
     * */
    protected fun displayAlert(
        severity: DrawableResolver,
        title: StringResolver,
        message: StringResolver?,
    ) {
        displayOverlay(
            overlayWindow = PrimaryMessageDialog(
                iconResolver = severity,
                titleResolver = title,
                messageResolver = message,
                onDismiss = ::dismissOverlay
            )
        )
    }

    /**
     * Emits the given [overlayWindow] to the [overlayWindowState] for display. The dialog
     * will only be displayed if either UI manually subscribed to the [overlayWindowState] or
     * if the screen is derived from [NavigableScreen] (which will automatically subscribe to
     * the [overlayWindowState], displays it and removes it when the screen is disposed.
     * */
    fun displayOverlay(
        overlayWindow: AbstractOverlayWindow,
    ) {
        windowMutableStateFlow.value = overlayWindow
    }

    /**
     * Clears the current overlay window by setting the [windowMutableStateFlow] to null.
     * This will cause any UI subscribed to [overlayWindowState] to dismiss the overlay.
     */
    protected fun dismissOverlay() {
        windowMutableStateFlow.value = null
    }

    @CallSuper
    override fun onCleared() {
        // Clean up the dialog (if any) before the ViewModel is destroyed
        dismissOverlay()
    }

    companion object {
        private val TAG = NavigationViewModel::class.java.simpleName
    }
}