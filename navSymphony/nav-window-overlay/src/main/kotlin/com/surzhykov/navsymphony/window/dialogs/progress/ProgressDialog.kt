package com.surzhykov.navsymphony.window.dialogs.progress

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import com.surzhykov.navsymphony.core.presentation.StringResolver
import com.surzhykov.navsymphony.window.core.LocalOverlayManager

/**
 * Displays a modal overlay dialog indicating that an operation is in progress.
 *
 * The dialog is shown using [LocalOverlayManager] and is dismissed when the composition is disposed.
 * Use [dismissEnabled] to optionally allow the user to dismiss it.
 *
 * @param message Message to display below the animation.
 * @param dismissEnabled Whether user-initiated dismissal is allowed.
 * @param onDismiss Callback to invoke when dialog is dismissed by the user.
 */
@Composable
fun ProgressDialog(
    message: StringResolver,
    dismissEnabled: Boolean,
    onDismiss: () -> Unit
) {
    val overlayManager by rememberUpdatedState(LocalOverlayManager.current)
    DisposableEffect(onDismiss, message, dismissEnabled) {
        val overlay = ProgressDialogWindow(
            messageResolver = message,
            dismissEnabled = dismissEnabled,
            onDismiss = onDismiss,
        )
        overlayManager.showOverlay(overlay)
        onDispose {
            overlayManager.dismissOverlay(overlay.id)
        }
    }
}