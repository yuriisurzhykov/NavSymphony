package com.surzhykov.navsymphony.screen.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Defines a communication mechanism for managing and observing screen state.
 *
 * This interface provides read and update operations for screen state. The purpose of this
 * interface is to help to build a testable environment so that you can easily mock a test
 * implementation of this interface in your unit tests.
 *
 * @param T The type representing the screen state.
 */
interface StateCommunication<T : ScreenState> {

    /**
     * Provides a mechanism to update the screen state.
     */
    interface Update<T : ScreenState> {

        /**
         * Updates the current state using the provided transformation function.
         *
         * @param updateContract A lambda that takes the current state and returns the updated state.
         */
        fun update(updateContract: (T) -> T)
    }

    /**
     * Provides a mechanism to read the current screen state.
     */
    interface Read<T : ScreenState> {
        /**
         * Returns a [StateFlow] representing the current screen state.
         */
        fun state(): StateFlow<T>
    }

    /**
     * A mutable implementation of [StateCommunication], combining state reading and updating.
     */
    interface Mutable<T : ScreenState> : Update<T>, Read<T>,
        StateCommunication<T>

    /**
     * An abstract implementation of [Mutable] that provides base functionality for state management.
     *
     * @param stateFlow The [MutableStateFlow] used to store and update the state.
     */
    abstract class Abstract<T : ScreenState>(
        private val stateFlow: MutableStateFlow<T>,
    ) : Mutable<T> {
        override fun update(updateContract: (T) -> T) = stateFlow.update(updateContract)
        override fun state(): StateFlow<T> = stateFlow.asStateFlow()
    }

    /**
     * A concrete implementation of [StateCommunication] that manages the screen state using a [MutableStateFlow].
     *
     * @param initialState The initial state of the screen.
     *
     * Example:
     * ```kotlin
     * class LoginViewModel(
     *      dispatcher: Dispatcher,
     *      communication: StateCommunication.Mutable<ProfileScreenState> = StateCommunication.Base(ProfileScreenState())
     * ): ScreenStateViewModel<ProfileScreenState>(communication)
     * ```
     */
    class Base<T : ScreenState>(initialState: T) : Abstract<T>(MutableStateFlow(initialState))
}
