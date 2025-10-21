package com.surzhykov.navsymphony.window.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.surzhykov.navsymphony.core.presentation.DrawableResolver
import com.surzhykov.navsymphony.core.presentation.StringResolver
import com.surzhykov.navsymphony.core.presentation.persistentNonNullListOf
import com.surzhykov.navsymphony.window.core.LocalOverlayManager

/**
 * Displays a primary dialog with the standard "OK" button.
 *
 * This overload of [PrimaryDialog] provides a simple API for dialogs that require a title,
 * an optional message, and an OK button. The dialog is managed by an overlay that is automatically
 * registered and unregistered.
 *
 * **Corner Cases:**
 * - If [messageResolver] is null, the dialog will render only the title and the OK button.
 *
 * **Usage Example:**
 * ```
 * PrimaryDialog(
 *     iconResolver = myIconResolver,
 *     titleResolver = stringResolver("Dialog Title"),
 *     messageResolver = stringResolver("This is the dialog message."),
 *     onDismiss = { /* Handle dismiss */ }
 * )
 * ```
 *
 * @param iconResolver Resolver for the dialog icon.
 * @param titleResolver Resolver for the dialog title.
 * @param messageResolver (Optional) Resolver for the dialog message.
 * @param onDismiss Callback triggered when the dialog is dismissed.
 */
@Composable
fun PrimaryDialog(
    iconResolver: DrawableResolver,
    titleResolver: StringResolver,
    messageResolver: StringResolver? = null,
    onDismiss: () -> Unit,
) {
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    PrimaryDialog(
        iconResolver = iconResolver,
        titleResolver = titleResolver,
        messageResolver = messageResolver,
        onDismiss = currentOnDismiss,
        positiveButtonSpec = DialogButton.Companion.rememberDialogButtonOk(currentOnDismiss)
    )
}

/**
 * Displays a customizable primary dialog with options for positive, negative, and neutral buttons.
 *
 * This overload allows you to specify custom button specifications. The dialog is rendered as an
 * overlay using [PrimaryDialogWindow].
 *
 * **Corner Cases:**
 * - The list of buttons must contain at least one button; otherwise, the dialog would not be interactive.
 *
 * **Usage Example:**
 * ```
 * PrimaryDialog(
 *     iconResolver = myIconResolver,
 *     titleResolver = stringResolver("Dialog Title"),
 *     messageResolver = stringResolver("Detailed dialog message."),
 *     onDismiss = { /* Handle dismiss */ },
 *     negativeButtonSpec = DialogButton.rememberDialogButtonCancel { /* Cancel action */ },
 *     positiveButtonSpec = DialogButton.rememberDialogButtonOk { /* OK action */ }
 * )
 * ```
 *
 * @param iconResolver Resolver for the dialog icon.
 * @param titleResolver Resolver for the dialog title.
 * @param messageResolver (Optional) Resolver for the dialog message.
 * @param onDismiss Callback triggered when the dialog is dismissed.
 * @param positiveButtonSpec Specification for the positive (confirming) button.
 * @param negativeButtonSpec (Optional) Specification for the negative button.
 * @param neutralButtonSpec (Optional) Specification for the neutral button.
 */
@Composable
fun PrimaryDialog(
    iconResolver: DrawableResolver,
    titleResolver: StringResolver,
    messageResolver: StringResolver?,
    onDismiss: () -> Unit,
    positiveButtonSpec: DialogButton = DialogButton.Companion.rememberDialogButtonOk(onDismiss),
    negativeButtonSpec: DialogButton? = null,
    neutralButtonSpec: DialogButton? = null,
) {
    val overlayManager = LocalOverlayManager.current
    val localOnDismiss by rememberUpdatedState(onDismiss)
    val buttonSpec = remember(positiveButtonSpec, negativeButtonSpec, neutralButtonSpec) {
        persistentNonNullListOf(
            negativeButtonSpec,
            neutralButtonSpec,
            positiveButtonSpec,
        )
    }
    DisposableEffect(onDismiss, titleResolver, messageResolver) {
        val overlay = PrimaryDialogWindow(
            iconResolver = iconResolver,
            titleResolver = titleResolver,
            messageResolver = messageResolver,
            buttonSpecs = buttonSpec,
            onDismiss = localOnDismiss,
        )
        overlayManager.showOverlay(overlay)
        onDispose {
            overlayManager.dismissOverlay(overlay.id)
        }
    }
}