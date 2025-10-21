package com.surzhykov.navsympnony.menu

import androidx.compose.runtime.Immutable
import com.surzhykov.navsymphony.screen.core.ScreenIntent
import com.surzhykov.navsymphony.screen.core.ScreenRoute

/**
 * Represents the possible intents (actions or events) that can be triggered on the Menu Screen.
 *
 * This sealed interface defines the different actions that the Menu Screen can handle.
 * Each data class within this interface represents a specific intent.
 *
 * The use of a sealed interface ensures that all implementations of [MenuScreenIntent] are defined
 * within this file.
 */
@Immutable
sealed interface MenuScreenIntent : ScreenIntent {

    @Immutable
    data class LoadMenu(
        val route: ScreenRoute.Menu,
    ) : MenuScreenIntent
}