package com.surzhykov.navsymphony.window.overlay

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import com.surzhykov.navsymphony.window.core.AbstractOverlayWindow
import com.surzhykov.navsymphony.window.core.LocalOverlayManager

/**
 * BaseOverlayWindow is an implementation of [AbstractOverlayWindow] that provides standard overlay window functionality.
 *
 * It displays content that overlays the primary UI. The overlay's position is calculated based on the provided
 * [anchorViewInfo], which determines how the overlay is anchored relative to another view. When a click is detected
 * outside the overlay content, the [onDismiss] callback is invoked.
 *
 * @param content The composable content to be displayed within the overlay.
 * @param anchorViewInfo Information about the anchor view used to position the overlay.
 * @param onDismiss A callback invoked when a dismiss action is requested (e.g., clicking outside the overlay).
 */
open class BaseOverlayWindow(
    private val content: @Composable BoxScope.() -> Unit,
    override val anchorViewInfo: AnchorViewInfo,
    override val onDismiss: () -> Unit = {},
) : AbstractOverlayWindow(anchorViewInfo = anchorViewInfo, onDismiss = onDismiss) {

    @Composable
    override fun BoxScope.RenderContent(modifier: Modifier) {
        content()
    }
}

/**
 * Displays an overlay window that is anchored to a specific view.
 *
 * The overlay window is designed to display content over the primary UI (such as dialogs or popups),
 * and its position is determined by the provided [anchorViewInfo]. Clicking outside the overlay content
 * triggers the [onDismiss] callback to hide the overlay.
 *
 * The overlay is registered with the [OverlayManager] using a [DisposableEffect]. When this composable
 * enters the composition, it creates a [BaseOverlayWindow] and adds it to the manager. When the composable
 * leaves the composition, the overlay is automatically removed.
 *
 * The [anchorViewInfo] should be obtained using [Modifier.onGloballyPositioned] and the corresponding
 * helper functions, and must be remembered in the parent composable to avoid unnecessary recompositions.
 *
 * @param anchorViewInfo The information of the anchor view used to position the overlay.
 * @param onDismiss A callback invoked to dismiss the overlay (e.g., when clicking outside the content).
 * @param content The composable content to display within the overlay window.
 * @sample OverlayWindowPreview
 */
@Composable
fun OverlayWindow(
    anchorViewInfo: AnchorViewInfo,
    onDismiss: () -> Unit = {},
    content: @Composable (BoxScope.() -> Unit),
) {
    val overlayManager = LocalOverlayManager.current
    DisposableEffect(anchorViewInfo, onDismiss, content) {
        val overlay = BaseOverlayWindow(
            content = content,
            anchorViewInfo = anchorViewInfo,
            onDismiss = onDismiss,
        )
        overlayManager.showOverlay(overlay)
        onDispose {
            overlayManager.dismissOverlay(overlay.id)
        }
    }
}
