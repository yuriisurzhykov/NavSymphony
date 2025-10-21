package com.surzhykov.navsymphony.screen.core

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * `Screen` is an abstract base class representing a single screen in the application's navigation
 * hierarchy.
 *
 * It defines the core structure and behavior of a screen, including how it renders its content,
 * manages its state, handles user intents, and interacts with the application's navigation and UI
 * elements (like toolbars and bottom bars).
 *
 * **Generics:**
 * - `R`: The type of the `ScreenRoute` associated with this screen. This defines how the screen is
 * accessed.
 * - `S`: The type of the `ScreenState` representing the screen's state data.
 * - `I`: The type of the `ScreenIntent` representing user actions or events that can change the
 * screen state.
 * - `VM`: The type of the `AbstractViewModel` that manages the screen's state and handles intents.
 *
 * **Key Features:**
 * - **State Management:** Screens are driven by a `ScreenState` (`S`) managed by a `ViewModel` (`VM`).
 * - **Intent Handling:** Screens receive `ScreenIntent`s (`I`) from the UI, which are processed by
 * the `ViewModel` to update the state.
 * - **Composable Rendering:** The `Screen()` function defines how the screen content is rendered
 * based on the current state.
 * - **Pane Functionality:** The `Pane()` function allows a screen to be embedded within another
 * screen or layout, while still functioning independently.
 * - **Animation Support:** The `screenAnimationSpec` property allows configuring custom transitions
 * for entering and exiting the screen.
 * - **Toolbar and Bottom Bar Integration:** The `ScreenToolbar()` and `BottomBar()` functions
 * manage the display and styling of these UI elements based on the screen's state.
 * - **Lifecycle Awareness:** Uses `collectAsStateWithLifecycle` for
 * */
@Immutable
abstract class Screen<R : ScreenRoute, S : ScreenState, I : ScreenIntent, VM : AbstractViewModel<S, I>> {

    /**
     *  Specifies the animation used when transitioning between screens.
     *
     *  This property allows customization of the animation behavior for screen transitions within
     *  a navigation system or similar UI flow. By default, it utilizes [ScreenAnimationSpec.Base],
     *  which provides a basic crossfade animation.
     *
     *  You can override this property to provide a different animation specification.
     *
     *  **Example:**
     *  ```kotlin
     *  // Override to use a custom animation spec with a 500ms duration.
     *  override val screenAnimationSpec: ScreenAnimationSpec by lazy {
     *      ScreenAnimationSpec.Base(tween(durationMillis = 500))
     *  }
     *
     *  // Or, you can use a preset like ScreenAnimationSpec.None to disable animations.
     *  override val screenAnimationSpec: ScreenAnimationSpec by lazy {
     *      ScreenAnimationSpec.None()
     *  }
     *  ```
     *
     *  **Available Animation Specs:**
     *  - [ScreenAnimationSpec.Base]: A default crossfade animation.
     *  - [ScreenAnimationSpec.None]: Disables screen transitions animations.
     *
     *  **Key Considerations:**
     *  - The `screenAnimationSpec` affects all screen transitions where it is applied.
     *  - Consider the user experience when choosing an animation. Subtle and smooth transitions often provide the best experience.
     *  - Overly complex or long animations can be distracting or frustrating for users.
     */
    @Stable
    open val screenAnimationSpec: ScreenAnimationSpec by lazy { ScreenAnimationSpec.Base() }

    /**
     * Represents a screen in the navigation hierarchy.
     *
     * This is an abstract function that must be implemented by concrete screen classes.
     * It is responsible for rendering the UI of a specific screen based on its current state.
     *
     * @param modifier The modifier to be applied to the root composable of this screen. This allows
     * customization of the layout and behavior of the screen's UI.
     * @param screenState A [State] object holding the current state of the screen. Changes to this
     * state will trigger recomposition of the screen. The type of the state is defined by the
     * generic type `S`.
     * @param route The route associated with this screen. This is used to identify and navigate to
     * this screen. The type of the route is defined by the generic type `R`.
     * @param onIntent A lambda function that receives an [ScreenIntent] object (of type `I`). This
     * function is used to handle user interactions or events within the screen. It allows sending
     * actions or data to the logic layer.
     *
     * @see Modifier
     * @see State
     */
    @Composable
    @SuppressLint("NotConstructor")
    abstract fun Screen(
        modifier: Modifier,
        screenState: State<S>,
        route: R,
        onIntent: (I) -> Unit,
    )

    /**
     * A composable function that represents a single pane in a navigation hierarchy.
     * This function is responsible for:
     * - Collecting and managing the screen state from the provided [viewModel].
     * - Rendering the screen's content using the [Screen] composable.
     * - Displaying the screen's toolbar using the [ScreenToolbar] composable.
     * - Displaying the screen's bottom bar using the [BottomBar] composable.
     * - Providing an `onIntent` callback for the screen to handle user interactions.
     *
     * The function uses [rememberUpdatedState] to ensure that the latest values of [route] and
     * [viewModel] are used without triggering unnecessary recompositions.
     *
     * @param modifier The [Modifier] to be applied to the pane.
     * @param viewModel The [VM] (ViewModel) instance responsible for managing the screen's state
     * and logic.
     * @param route The current route ([R]) representing the navigation destination of this pane.
     *
     * @see ScreenToolbar
     * @see Screen
     * @see BottomBar
     * @see rememberUpdatedState
     * @see collectAsStateWithLifecycle
     */
    @Composable
    @DelicateScreenApi
    open fun Pane(
        modifier: Modifier,
        viewModel: VM,
        route: R,
    ) {
        val currentRoute by rememberUpdatedState(route)
        // Creating a local copy of viewModel parameter so that we always would have the most recent
        // copy of VM, without triggering recomposition.
        val currentViewModel by rememberUpdatedState(viewModel)
        // Collecting a screen state.
        val screenState = currentViewModel.screenState.collectAsStateWithLifecycle()
        // Creating onIntent callback for actual screen implementation.
        val onIntent by rememberUpdatedState { intent: I ->
            currentViewModel.onIntent(intent)
        }
        // Apply the toolbar using the current screen state.
        ScreenToolbar(screenState, onIntent)
        // Render content of the screen using screenState and onIntent callback function
        Screen(modifier, screenState, currentRoute, onIntent)
        // Apply bottom bar using the current screen state.
        BottomBar(screenState, onIntent)
    }

    /**
     * Applies toolbar style for the current screen when entering composition scope. Toolbar style
     * is obtained from screen state.
     * */
    @Composable
    protected open fun ScreenToolbar(screenState: State<S>, onIntent: (I) -> Unit) {

    }

    /**
     * Applies toolbar style if needed for the current screen when entering composition scope.
     * Bottom bar style is obtained using the screen state.
     * */
    @Composable
    protected open fun BottomBar(screenState: State<S>, onIntent: (I) -> Unit) {

    }
}