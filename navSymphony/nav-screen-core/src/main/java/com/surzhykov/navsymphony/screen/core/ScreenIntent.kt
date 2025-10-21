package com.surzhykov.navsymphony.screen.core

import androidx.compose.runtime.Immutable

/**
 * Represents a user action or event that triggers a state change in a screen.
 *
 * Implementations of this interface define various user intents, such as button clicks,
 * text input updates, or any other UI interactions that modify the state of a screen.
 *
 * Example:
 * ```
 * sealed class LoginIntent : ScreenIntent {
 *     object SubmitLogin : LoginIntent()
 *     data class UpdateUsername(val username: String) : LoginIntent()
 *     data class UpdatePassword(val password: String) : LoginIntent()
 * }
 * ```
 */
@Immutable
interface ScreenIntent