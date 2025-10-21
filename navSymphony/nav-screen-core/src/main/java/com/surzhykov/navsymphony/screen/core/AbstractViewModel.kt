package com.surzhykov.navsymphony.screen.core

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

/**
 * An abstract base class for ViewModels that manage a specific screen's state and intents.
 *
 * This class provides a structured way to handle screen state updates and user interactions
 * (intents).
 * It uses a `StateFlow` to emit the current screen state and exposes an `onIntent` function
 * to process user actions.
 *
 * @param T The type of the screen state. This should be a data class or a sealed interface
 * representing the UI state.
 * @param I The type of the screen intent. This should be a sealed interface or enum representing
 * the user actions.
 */
abstract class AbstractViewModel<T : ScreenState, I : ScreenIntent> : ViewModel() {

    /**
     * Represents the current state of the screen.
     *
     * This [StateFlow] emits the latest screen state of type [T]. Subscribers will receive updates
     * whenever the screen state changes. It's a hot flow, meaning it will start emitting
     * immediately and will retain the latest emitted value for new subscribers.
     *
     * The state provided by this [StateFlow] typically represents the UI data and its associated
     * loading/error states.
     *
     * Example Usage:
     *
     * ```kotlin
     * // In a ViewModel:
     * class MyViewModel(
     *      private val useCase: MyUseCase,
     *      private val useCase2: MyUseCase2
     * ) : AbstractViewModel<MyScreenState, MyScreenIntent>() {
     *
     *     override val screenState: StateFlow<MyScreenState> = combine(
     *          useCase.execute(),
     *          useCase2.execute()
     *     ) { data1, data2 ->
     *          transformDataToState(data1, data2)
     *     }
     *     .distinctUntilChanged()
     *     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), MyScreenState.Loading)
     * }
     *
     * // In a Compose UI:
     * @Composable
     * fun MyScreen(viewModel: MyViewModel) {
     *     val screenState by viewModel.screenState.collectAsStateWithLifecycle()
     * }
     * ```
     *
     * @see StateFlow
     * @see kotlinx.coroutines.flow.MutableStateFlow
     * @see kotlinx.coroutines.flow
     * */
    abstract val screenState: StateFlow<T>

    /**
     * Handles an incoming intent.
     *
     * This function is called when a new intent of type [I] is received.
     * Implementations should override this method to define the specific behavior
     * to be executed when the corresponding intent is received.
     *
     * @param intent The intent to be handled. The type of the intent is [I].
     */
    abstract fun onIntent(intent: I)
}