package com.surzhykov.navsympnony.menu.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import com.surzhykov.navsympnony.menu.domain.model.MenuConditionSeverity

/**
 * A Composable function that returns a specific [Color] based on the [MenuConditionSeverity].
 * This color is intended for the content (e.g., text or icons) that is displayed
 * on top of a background whose color is determined by the severity.
 *
 * @return [Color] The color corresponding to the severity level:
 * - [MenuConditionSeverity.Normal] -> `EdgeTheme.colors.secondaryContainer`
 * - [MenuConditionSeverity.Warning] -> `EdgeTheme.colors.tertiary`
 * - [MenuConditionSeverity.Error] -> `EdgeTheme.colors.error`
 */
@Composable
fun MenuConditionSeverity.contentColorForSeverity(enabled: Boolean): State<Color> {
    val targetColor = when (this) {
        MenuConditionSeverity.Normal  -> MaterialTheme.colorScheme.secondaryContainer
        MenuConditionSeverity.Warning -> MaterialTheme.colorScheme.tertiary
        MenuConditionSeverity.Error   -> MaterialTheme.colorScheme.error
    }
    return if (enabled) {
        rememberUpdatedState(targetColor)
    } else {
        // TODO: Replace '0.6f' with EdgeTheme.colors.disabledAlpha when implemented.
        rememberUpdatedState(targetColor.copy(alpha = 0.6f))
    }
}