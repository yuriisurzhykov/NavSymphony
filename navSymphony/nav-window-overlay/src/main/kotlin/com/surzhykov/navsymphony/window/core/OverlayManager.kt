package com.surzhykov.navsymphony.window.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * OverlayManager is responsible for managing and rendering overlay windows.
 *
 * It maintains a reactive list of active overlays (of type [AbstractOverlayWindow]) and provides functions
 * to add (show) and remove (dismiss) overlays. Overlays are rendered by invoking the [RenderOverlays]
 * composable function, which iterates over the list of overlays and renders each one.
 */
@Stable
class OverlayManager {

    private val overlays = mutableStateListOf<AbstractOverlayWindow>()

    /**
     * Registers a new overlay window.
     *
     * @param overlay The overlay window to be displayed.
     */
    fun showOverlay(overlay: AbstractOverlayWindow) {
        overlays.add(overlay)
    }

    /**
     * Dismisses an overlay window by its unique identifier.
     *
     * If an overlay with the specified [id] is found, its [AbstractOverlayWindow.onDismiss] callback
     * is invoked and the overlay is removed from the list.
     *
     * @param id The unique identifier of the overlay to dismiss.
     */
    fun dismissOverlay(id: String) {
        overlays.removeAll { it.id == id }
    }

    /**
     * Renders all active overlays.
     *
     * This composable function iterates over all registered overlays and calls their [AbstractOverlayWindow.Render]
     * methodEach overlay is keyed by its unique [AbstractOverlayWindow.id] to ensure proper recomposition.
     */
    @Composable
    fun RenderOverlays() {
        val localOverlays = remember { overlays }
        if (localOverlays.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize()) {
                localOverlays.forEach { overlay ->
                    key(overlay.id) {
                        overlay.Render()
                    }
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OverlayManager) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}

/**
 * CompositionLocal for accessing the current [OverlayManager].
 *
 * This should be provided by [OverlayHostProvider] at the root of the application content.
 */
val LocalOverlayManager = compositionLocalOf<OverlayManager> {
    error("OverlayManager is not provided. Please wrap your content with OverlayHostProvider.")
}