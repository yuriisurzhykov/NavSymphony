package com.surzhykov.navsymphony.choreographer.presentation

import androidx.compose.runtime.Immutable
import com.surzhykov.navsymphony.window.core.OverlayManager

/**
 * [NavigationContext] holds the context required for navigation operations.
 * It contains the [NavigationController] for managing navigation flow and the
 * [OverlayManager] for handling overlays.
 *
 * This class is marked as [Immutable] meaning its state cannot change after creation.
 */
@Immutable
data class NavigationContext(
    /**
     * The controller responsible for managing the navigation flow.
     */
    val navigationController: NavigationController,
    /**
     * The manager responsible for handling overlays.
     */
    val overlayManager: OverlayManager
)