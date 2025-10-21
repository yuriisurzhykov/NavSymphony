package com.surzhykov.navsymphony.screen.core

import androidx.lifecycle.viewModelScope
import com.surzhykov.navsymphony.domain.Dispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.StateFlow

/**
 * A ViewModel that follows the MVI (Model-View-Intent) pattern, handling screen state and user intents.
 *
 * This ViewModel manages a queue of user intents and processes them asynchronously.
 * The current screen state is exposed as a `StateFlow`, which updates whenever the state changes.
 *
 * @param T The type representing the screen state.
 * @param I The type representing the screen intent.
 * @param stateCommunication The communication mechanism for managing screen state.
 * @param dispatcher The dispatcher used for handling background tasks.
 *
 * Example:
 * ```
 * class LoginViewModel(
 *     stateCommunication: StateCommunication.Mutable<LoginScreenState>,
 *     dispatcher: Dispatcher
 * ) : ScreenStateViewModel<LoginScreenState, LoginIntent>(stateCommunication, dispatcher) {
 *
 *     override fun processIntent(intent: LoginIntent) {
 *         when (intent) {
 *             is LoginIntent.UpdateUsername -> updateState { it.copy(username = intent.username) }
 *             is LoginIntent.UpdatePassword -> updateState { it.copy(password = intent.password) }
 *             is LoginIntent.SubmitLogin -> authenticateUser()
 *         }
 *     }
 *
 *     private fun authenticateUser() {
 *         updateState { it.copy(isLoading = true) }
 *         // Perform authentication and update state accordingly
 *     }
 * }
 * ```
 */
abstract class ScreenStateViewModel<T : ScreenState, I : ScreenIntent>(
    private val stateCommunication: StateCommunication.Mutable<T>,
    dispatcher: Dispatcher,
) : AbstractViewModel<T, I>() {

    private val intentQueue = Channel<I>(BUFFERED)

    /**
     * Exposes the current state of the screen as a [StateFlow].
     */
    override val screenState: StateFlow<T> = stateCommunication.state()

    init {
        dispatcher.launchBackground(viewModelScope) {
            intentQueue.consumeEach {
                processIntent(it)
            }
        }
    }

    /**
     * Processes the given user intent.
     *
     * Implementations should handle different intents and update the state accordingly.
     *
     * @param intent The intent representing a user action.
     */
    protected abstract fun processIntent(intent: I)

    /**
     * Sends a new intent to be processed.
     *
     * @param intent The user intent to process.
     */
    override fun onIntent(intent: I) {
        intentQueue.trySend(intent)
    }

    /**
     * Updates the current screen state using the provided contract.
     *
     * @param contract A lambda that takes the current state and returns the updated state.
     */
    protected fun updateState(contract: (T) -> T) {
        stateCommunication.update(contract)
    }
}