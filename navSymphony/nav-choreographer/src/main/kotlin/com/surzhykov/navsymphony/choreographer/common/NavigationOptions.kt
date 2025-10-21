package com.surzhykov.navsymphony.choreographer.common

import androidx.compose.runtime.Immutable

/**
 *  Navigation options used when navigating to a destination.
 *
 *  @property singleTop If true, and the destination is already on top of the stack, no new instance
 *  of the destination will be created.
 *  @property addToBackStack If true, the destination will be added to the back stack. This allows
 *  the user to navigate back to it.
 *  @property clearBackStack If true, the back stack will be cleared before navigating to the new
 *  destination. This prevents the user from navigating back.
 */
@Immutable
data class NavigationOptions(
    var singleTop: Boolean = true,
    var addToBackStack: Boolean = true,
    var clearBackStack: Boolean = false,
)