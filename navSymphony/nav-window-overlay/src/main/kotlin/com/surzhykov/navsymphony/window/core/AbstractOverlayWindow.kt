package com.surzhykov.navsymphony.window.core

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.surzhykov.navsymphony.window.overlay.AnchorViewInfo
import java.util.UUID

private val BackgroundSemitransparentColor = Color(0x80000000)

/**
 * Base class for every overlay window.
 * This is where general properties are set, such as unique identifiers, anchor information
 * [anchorViewInfo], and the closure callback [onDismiss].
 *
 * @property id A string ID of an overlay window.
 * @property anchorViewInfo The anchor information for the window, that will affect the offset of
 * overlay window content.
 * @property onDismiss A callback that is to be triggered when a user input requests to dismiss a
 * window.
 * */
@Stable
abstract class AbstractOverlayWindow(
    open val id: String = UUID.randomUUID().toString(),
    open val anchorViewInfo: AnchorViewInfo,
    open val onDismiss: () -> Unit,
) {

    @Composable
    abstract fun BoxScope.RenderContent(modifier: Modifier)

    /**
     * Method for drawing an overlay. A specific implementation overrides this method,
     * defining your own logic and visual presentation.
     */
    @Composable
    fun Render() {
        val density = LocalDensity.current
        val configurations = LocalWindowInfo.current.containerSize
        // Get screen dimensions in dp.
        val screenWidth = configurations.width.dp
        val screenHeight = configurations.height.dp
        val screenSize = remember(density, configurations) { DpSize(screenWidth, screenHeight) }

        // Remember the content size once it is measured.
        var contentSize by remember { mutableStateOf(DpSize.Zero) }
        val backgroundColor = backgroundColorAsState()

        Box(
            modifier = Modifier
                .drawBehind {
                    drawRect(backgroundColor.value)
                }
                .fillMaxSize()
                .clickable(onClick = onDismiss)
        ) {
            // Transition state is used to avoid unnecessary recompositions for AnimatedVisibility
            // component.
            val transitionState = remember {
                // Initially create a state with `false` to make content to not be visible when
                // overlay is displayed, and after that immediately change the state to
                // `visible = true`
                MutableTransitionState(false).apply { targetState = true }
            }
            AnimatedVisibility(
                modifier = Modifier
                    // Apply clickable modifier to content to prevent dismiss when user clicks on
                    // content instead of outside.
                    .clickable(
                        onClick = {},
                        enabled = false
                    )
                    .onSizeChanged { viewIntSize ->
                        with(density) { contentSize = viewIntSize.toSize().toDpSize() }
                    }
                    .absoluteOffset {
                        // Compute the offset for the content based on the anchor info.
                        val offset = anchorViewInfo.contentOffset(contentSize, screenSize)
                        IntOffset(offset.x.roundToPx(), offset.y.roundToPx())
                    },
                visibleState = transitionState,
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                exit = fadeOut() + scaleOut(targetScale = 0.8f)
            ) {
                RenderContent(Modifier)
            }
        }
    }

    @Composable
    protected open fun backgroundColorAsState(): State<Color> {
        return animateColorAsState(
            targetValue = BackgroundSemitransparentColor,
            animationSpec = tween(200)
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractOverlayWindow) return false

        if (id != other.id) return false
        if (anchorViewInfo != other.anchorViewInfo) return false
        if (onDismiss != other.onDismiss) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + anchorViewInfo.hashCode()
        result = 31 * result + onDismiss.hashCode()
        return result
    }
}