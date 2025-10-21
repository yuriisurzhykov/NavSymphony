package com.surzhykov.navsymphony.window.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * OverlayHostProvider provides an [OverlayManager] to its content and ensures that any overlays
 * are rendered on top of the primary UI.
 *
 * It wraps the provided content in a [CompositionLocalProvider] that supplies the [LocalOverlayManager]
 * with a new instance of [OverlayManager].
 *
 * @param content The primary UI content to be displayed along with the overlay host.
 *
 * To learn more, read the documentation by the [link](https://nortekcontrol.atlassian.net/wiki/spaces/GC2NEX/pages/4434427911/Overlay+Window+API)
 */
@Composable
fun OverlayHostProvider(
    overlayManager: OverlayManager = remember { OverlayManager() },
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalOverlayManager provides overlayManager
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()
            overlayManager.RenderOverlays()
        }
    }
}