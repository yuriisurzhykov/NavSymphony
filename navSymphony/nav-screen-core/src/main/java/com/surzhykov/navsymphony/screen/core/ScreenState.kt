package com.surzhykov.navsymphony.screen.core

import androidx.compose.runtime.Immutable

/**
 * Represents the UI state of a screen.
 *
 * Implementations of this interface define the various states a screen can be in,
 * including loading, success, or error states.
 *
 * Example:
 * ```
 * data class LoginScreenState(
 *     val isLoading: Boolean,
 *     val username: String,
 *     val password: String,
 *     val errorMessage: String? = null
 * ) : ScreenState
 * ```
 */
@Immutable
interface ScreenState