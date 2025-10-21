package com.surzhykov.navsympnony.menu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.surzhykov.navsymphony.screen.core.DelicateScreenApi
import com.surzhykov.navsymphony.screen.core.Screen
import com.surzhykov.navsymphony.screen.core.ScreenRoute

/**
 * `MenuScreen` is an abstract base class for screens that display a menu.
 * It handles common UI logic for menu screens, such as:
 *   - Managing screen state.
 *   - Handling screen intents.
 *   - Rendering a toolbar.
 *   - Rendering a bottom bar.
 *   - Loading menu data when the route changes.
 *
 * This class is designed to be extended by concrete menu screen implementations.
 *
 * @param R The type of the `ScreenRoute.Menu` associated with this screen. This is a sub type of
 * ScreenRoute.Menu.
 * @param VM The type of the `MenuScreenViewModel` associated with this screen. This is a sub type
 * of [MenuScreenViewModel]
 */
@Immutable
abstract class MenuScreen<R, VM> :
    Screen<R, MenuScreenState, MenuScreenIntent, VM>() where R : ScreenRoute.Menu, VM : MenuScreenViewModel {

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
        val onIntent by rememberUpdatedState(currentViewModel::onIntent)
        // Apply the toolbar using the current screen state.
        ScreenToolbar(screenState, onIntent)
        // Render content of the screen using screenState and onIntent callback function
        Screen(modifier, screenState, currentRoute, onIntent)
        // Apply bottom bar using the current screen state.
        BottomBar(screenState, onIntent)

        // Loading menu data when the route changes.
        LaunchedEffect(route) {
            onIntent(MenuScreenIntent.LoadMenu(route))
        }
    }
}