package com.surzhykov.navsymphony.window.dialogs

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp

private val DialogMinHeight = 150.dp
private val BoxShadow = 6.dp
private val DialogBackgroundColor = Color(0x80000000)

/**
 * A composable that centers dialog content within its parent Box.
 *
 * This component adapts the dialog's width and height based on the screen orientation and size.
 * - **Width:** Uses 65% of the screen width in landscape mode and 90% in portrait.
 * - **Height:** Enforces a flexible height that is at least [DialogMinHeight] and at most 60%
 *   of the screen height.
 * - Applies theme-based shadow, background, and clipping.
 *
 * **Corner Cases:**
 * - On very small screens the height may be constrained; ensure your content is responsive.
 * - If the content’s intrinsic height is less than [DialogMinHeight], the dialog will still
 *   occupy [DialogMinHeight].
 *
 * **Usage Example:**
 * ```
 * Box(modifier = Modifier.fillMaxSize()) {
 *     CenteredDialogContent {
 *         // Place your RowScope-based dialog content here
 *     }
 * }
 * ```
 *
 * @param modifier Additional [Modifier] for customization.
 * @param content A composable lambda in the [RowScope] to layout child elements.
 */
@Composable
@Suppress("UnusedReceiverParameter")
fun DialogScope.CenteredDialogContent(
    modifier: Modifier = Modifier,
    content: @Composable (BoxScope.() -> Unit),
) {
    val config = LocalWindowInfo.current
    val orientation = LocalConfiguration.current.orientation
    // Based on the orientation set the maximum width that the dialog may use.
    val widthFraction = if (orientation == Configuration.ORIENTATION_LANDSCAPE) 0.65f else 0.9f
    // To make dialog height flexible but limited at the same time, we need to know the screen
    // height and use the maximum 60 percent of the space.
    val dialogMaxHeight by remember(config) {
        derivedStateOf { (config.containerSize.height * 0.6f).dp }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = modifier
                .align(Alignment.Center)
                .fillMaxWidth(widthFraction)
                .requiredHeightIn(min = DialogMinHeight, max = dialogMaxHeight)
                .shadow(
                    elevation = BoxShadow,
                    shape = MaterialTheme.shapes.medium
                )
                .background(
                    color = DialogBackgroundColor,
                    shape = MaterialTheme.shapes.medium
                )
                .clip(MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}