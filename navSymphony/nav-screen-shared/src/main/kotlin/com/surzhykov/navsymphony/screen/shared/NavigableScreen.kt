package com.surzhykov.navsymphony.screen.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.surzhykov.navsymphony.choreographer.presentation.AppComponentIntentActor
import com.surzhykov.navsymphony.choreographer.presentation.LocalNavigation
import com.surzhykov.navsymphony.screen.core.DelicateScreenApi
import com.surzhykov.navsymphony.screen.core.Screen
import com.surzhykov.navsymphony.screen.core.ScreenIntent
import com.surzhykov.navsymphony.screen.core.ScreenRoute
import com.surzhykov.navsymphony.screen.core.ScreenState
import com.surzhykov.navsymphony.window.core.LocalOverlayManager

/**
 * Abstract base screen class that supports app-level navigation intents.
 *
 * This screen integrates with [AppComponentIntentActor] to handle one-time navigation commands
 * (emitted by [NavigationViewModel]) and execute them using the global navigation context.
 *
 * Subclasses must override [Screen] to implement UI rendering logic based on [ScreenState].
 *
 * Requires that [LocalNavigation] provides an instance of [AppComponentIntentActor].
 *
 * @param R Screen route type, must implement [ScreenRoute].
 * @param S Screen state type, must implement [ScreenState].
 * @param I Screen intent type, must implement [ScreenIntent].
 * @param VM ViewModel for this screen, must extend [NavigationViewModel].
 */
abstract class NavigableScreen<R, S, I, VM> : Screen<R, S, I, VM>()
        where R : ScreenRoute, S : ScreenState, I : ScreenIntent, VM : NavigationViewModel<S, I> {

    /**
     * Default implementation of screen layout that:
     * - Collects the screen state from the ViewModel.
     * - Observes [NavigationViewModel.navigationQueue] and forwards navigation intents to the
     * [LocalNavigation] context.
     * - Renders toolbar, screen content, and bottom bar using the current state.
     *
     * This composable must be used as the rendering entry point for screens extending
     * [NavigableScreen].
     *
     * @param modifier Modifier to apply to the screen container.
     * @param viewModel ViewModel instance scoped to the screen.
     * @param route The screen route representing current navigation context.
     */
    @Composable
    @DelicateScreenApi
    override fun Pane(modifier: Modifier, viewModel: VM, route: R) {
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

        // Initialize navigation mechanism and collect navigation intents.
        val navigation by rememberUpdatedState(LocalNavigation.current)
        require(navigation is AppComponentIntentActor) {
            "In order to render a screen LocalNavigation must be a `AppComponentIntentActor`!"
        }
        LaunchedEffect(Unit) {
            viewModel.navigationQueue.collect { intent ->
                (navigation as AppComponentIntentActor).publishIntent(intent)
            }
        }

        // Display overlay window if any.
        // 1) Obtain overlay manager from composition local. Use rememberUpdatedState to ensure
        // that the latest overlay manager is used.
        val localOverlayManager by rememberUpdatedState(LocalOverlayManager.current)
        // 2) Collect overlay window from the ViewModel with lifecycle awareness.
        val overlayWindow = currentViewModel.overlayWindowState.collectAsStateWithLifecycle()
        DisposableEffect(overlayWindow.value?.id) {
            // 3) Display overlay window if any.
            val overlay = overlayWindow.value
            overlay?.let { localOverlayManager.showOverlay(it) }
            onDispose {
                // 4) On dispose remove overlay window from the manager.
                overlay?.let { localOverlayManager.dismissOverlay(it.id) }
            }
        }

        // Apply the toolbar using the current screen state.
        ScreenToolbar(screenState, onIntent)
        // Render content of the screen using screenState and onIntent callback function
        Screen(modifier, screenState, currentRoute, onIntent)
        // Apply bottom bar using the current screen state.
        BottomBar(screenState, onIntent)
    }
}