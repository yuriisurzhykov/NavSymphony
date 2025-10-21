package com.surzhykov.navsymphony.window.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.surzhykov.navsymphony.core.presentation.DrawableResolver
import com.surzhykov.navsymphony.core.presentation.StringResolver
import com.surzhykov.navsymphony.core.presentation.drawableResolver
import com.surzhykov.navsymphony.core.presentation.stringResolver
import com.surzhykov.navsymphony.window.R

/**
 * Data model representing a dialog button.
 *
 * [DialogButton] encapsulates the text, an optional icon, button colors, enabled state, and an
 * [onClick] callback.
 *
 * **Corner Cases:**
 * - If [enabled] is false, the button will not respond to click events.
 * - When [iconResolver] is null, the button renders without an icon.
 *
 * **Usage Example:**
 * ```
 * val okButton = DialogButton.rememberDialogButtonOk {
 *     // Handle OK button click
 * }
 * okButton.Render(modifier = Modifier.padding(8.dp))
 * ```
 *
 * @property textResolver Resolver for the button text.
 * @property iconResolver Optional resolver for the button icon.
 * @property buttonColor Background color of the button.
 * @property contentColor Color of the button content (text and icon).
 * @property enabled Flag indicating whether the button is enabled.
 * @property onClick Callback invoked when the button is clicked.
 */
@Immutable
data class DialogButton(
    val textResolver: StringResolver,
    val iconResolver: DrawableResolver?,
    val buttonColor: Color,
    val contentColor: Color,
    val enabled: Boolean,
    val onClick: () -> Unit,
) {

    /**
     * Renders the dialog button using the [PrimaryButton] composable.
     *
     * **Usage Example:**
     * ```
     * DialogButton.rememberDialogButtonOk { /* Handle click */ }
     *     .Render(modifier = Modifier.padding(8.dp))
     * ```
     *
     * @param modifier A [Modifier] for additional customization.
     */
    @Composable
    fun Render(modifier: Modifier = Modifier) {
        Button(
            modifier = modifier.background(buttonColor, MaterialTheme.shapes.medium),
            onClick = onClick,
            enabled = enabled,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (iconResolver != null) {
                    Icon(
                        contentDescription = null,
                        imageVector = iconResolver.resolve(),
                        tint = contentColor,
                        modifier = Modifier.alignByBaseline()
                    )
                }
                Text(
                    text = textResolver.asString(),
                    color = contentColor,
                )
            }
        }
    }

    companion object {

        /**
         * Remembers and returns a standard "OK" dialog button.
         *
         * **Usage Example:**
         * ```
         * val okButton = DialogButton.rememberDialogButtonOk {
         *      /* Handle OK click */
         * }
         * ```
         *
         * @param onClick Callback invoked when the button is clicked.
         * @return A [DialogButton] with preset text and icon for "OK".
         */
        @Composable
        fun rememberDialogButtonOk(onClick: () -> Unit): DialogButton {
            val textResolver = stringResolver(R.string.label_button_ok)
            val iconResolver = drawableResolver(R.drawable.ic_button_ok)
            return rememberDialogButton(textResolver, iconResolver, onClick = onClick)
        }

        /**
         * Remembers and returns a standard "Cancel" dialog button.
         *
         * **Usage Example:**
         * ```
         * val cancelButton = DialogButton.rememberDialogButtonCancel { /* Handle Cancel click */ }
         * ```
         *
         * @param onClick Callback invoked when the button is clicked.
         * @return A [DialogButton] with preset text and icon for "Cancel".
         */
        @Composable
        fun rememberDialogButtonCancel(onClick: () -> Unit): DialogButton {
            val textResolver = stringResolver(R.string.label_button_cancel)
            val iconResolver = drawableResolver(R.drawable.ic_button_cancel)
            return rememberDialogButton(textResolver, iconResolver, onClick = onClick)
        }

        /**
         * Remembers and returns a customizable dialog button.
         *
         * **Usage Example:**
         * ```
         * val customButton = DialogButton.rememberDialogButton(
         *      textResolver = stringResolver("Custom"),
         *      iconResolver = null,
         *      onClick = { /* Handle custom click */ }
         * )
         * ```
         *
         * Note: Uses [rememberUpdatedState] to always have the latest [onClick] during recomposition.
         *
         * @param textResolver Resolver for the button text.
         * @param iconResolver Optional resolver for the button icon; defaults to null.
         * @param onClick Callback invoked when the button is clicked.
         * @return A [DialogButton] instance configured with the provided parameters.
         */
        @Composable
        fun rememberDialogButton(
            textResolver: StringResolver,
            iconResolver: DrawableResolver? = null,
            buttonColor: Color = MaterialTheme.colorScheme.primary,
            contentColor: Color = MaterialTheme.colorScheme.onPrimary,
            onClick: () -> Unit,
        ): DialogButton {
            val localOnClick by rememberUpdatedState(onClick)
            return remember {
                DialogButton(
                    textResolver = textResolver,
                    iconResolver = iconResolver,
                    buttonColor = buttonColor,
                    contentColor = contentColor,
                    enabled = true,
                    onClick = localOnClick
                )
            }
        }
    }
}