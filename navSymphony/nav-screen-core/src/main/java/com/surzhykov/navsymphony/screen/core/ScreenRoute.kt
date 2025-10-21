package com.surzhykov.navsymphony.screen.core

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.Polymorphic

/**
 * Represents a route within the application's navigation graph.
 *
 * This interface acts as a marker for different screen destinations. Each screen within the app
 * should implement this interface, defining a unique route to that screen.
 *
 * Implementing this interface allows for:
 * - Type-safe navigation using a sealed class or similar.
 * - Clear and organized management of the app's navigation structure.
 * - Easy identification and handling of specific screens.
 *
 * Example Usage:
 * ```
 * @Immutable
 * object HomeScreen : ScreenRoute
 *
 * @Immutable
 * data class DetailScreen(val id: Int) : ScreenRoute
 * ```
 */
@Immutable
@Polymorphic
interface ScreenRoute {

    @Stable
    fun routeId() = this::class.hashCode()

    @Immutable
    @Polymorphic
    interface Menu : ScreenRoute
}