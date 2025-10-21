package com.surzhykov.navsymphony.window.overlay

import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.toSize

/**
 * An extension modifier that provides [AnchorViewInfo] via a callback when the composable is globally positioned.
 *
 * **Note:** Using this modifier may conflict with other [onGloballyPositioned] modifiers on the same composable.
 *
 * @param horizontalAlignment The desired horizontal alignment for content relative to the anchor.
 * @param verticalAlignment The desired vertical alignment for content relative to the anchor.
 * @param block A lambda that receives the computed [AnchorViewInfo].
 * @return A [Modifier] that triggers the [block] with [AnchorViewInfo] when the view is globally positioned.
 */
fun Modifier.onSimpleAnchorInfoDefined(
    horizontalAlignment: HorizontalWindowAlignment = HorizontalWindowAlignment.Start,
    verticalAlignment: VerticalWindowAlignment = VerticalWindowAlignment.Top,
    block: (AnchorViewInfo) -> Unit,
): Modifier = composed {
    val density = LocalDensity.current
    val globallyPositionedModifier = Modifier.onGloballyPositioned {
        with(it) { block.invoke(anchorViewInfo(density, horizontalAlignment, verticalAlignment)) }
    }
    return@composed this.then(globallyPositionedModifier)
}

/**
 * Calculate the [AnchorViewInfo] for the given composable view, using [onGloballyPositioned]
 * modifier to determine the position and size of an anchor view.
 *
 * @param horizontalAlignment The alignment that will be applied to the content of the [OverlayWindow].
 * This alignment will only be applied as a horizontal. Default alignment is Start to Start, which
 * means that the Start of the content of [OverlayWindow] will be aligned with the Start of an
 * anchor view.
 * @param verticalAlignment The alignment to be applied to the content that is rendering inside of
 * [OverlayWindow] and will only be applied as a vertical alignment. Default alignment is Top to Top,
 * which means that the top of the content will be aligned with the top of anchor view.
 * @param block The callback to be triggered when [AnchorViewInfo] is defined for the given view.
 *
 * @return Returns current [Modifier] with new `Modifier` applied to determine anchor info.
 * */
fun Modifier.onAdvancedAnchorInfoDefined(
    horizontalAlignment: HorizontalAlignment = HorizontalAlignment(
        HorizontalReferencePoint.Start,
        HorizontalReferencePoint.Start
    ),
    verticalAlignment: VerticalAlignment = VerticalAlignment(
        VerticalReferencePoint.Top,
        VerticalReferencePoint.Top
    ),
    block: (AnchorViewInfo) -> Unit,
): Modifier = composed {
    val density = LocalDensity.current
    val globallyPositionedModifier = Modifier.onGloballyPositioned {
        with(it) { block.invoke(anchorViewInfo(density, horizontalAlignment, verticalAlignment)) }
    }
    return@composed this.then(globallyPositionedModifier)
}

/**
 * An extension function for [LayoutCoordinates] that creates an [AnchorViewInfo] based on
 * the current layout coordinates of the view.
 *
 * The anchor view information is computed using [positionInRoot()] to obtain global coordinates.
 *
 * @param density The screen density used to convert pixels to dp.
 * @param horizontalAlignment The desired horizontal alignment for content relative to the anchor.
 * @param verticalAlignment The desired vertical alignment for content relative to the anchor.
 * @return An [AnchorViewInfo] containing the view's size, position, and alignment preferences.
 */
@Stable
fun LayoutCoordinates.anchorViewInfo(
    density: Density,
    horizontalAlignment: HorizontalWindowAlignment = HorizontalWindowAlignment.Start,
    verticalAlignment: VerticalWindowAlignment = VerticalWindowAlignment.Top,
): AnchorViewInfo {
    return with(density) {
        val viewPosition = DpOffset(
            positionInRoot().x.toDp(),
            positionInRoot().y.toDp()
        )
        val viewSize = size.toSize().toDpSize()
        AnchorViewInfo.Simple(viewSize, viewPosition, horizontalAlignment, verticalAlignment)
    }
}

/**
 * An extension function for [LayoutCoordinates] that creates an [AnchorViewInfo] in advanced mode
 * by utilizing [HorizontalAlignment] and [VerticalAlignment].
 *
 * @param density The current density of the screen
 * @param horizontalAlignment The alignment the will be applied to the content of the [OverlayWindow].
 * This alignment will only be applied as a horizontal. Default alignment is Start to Start, which
 * means that the Start of the content of [OverlayWindow] will be aligned with the Start of an
 * anchor view.
 * @param verticalAlignment The alignment to be applied to the content that is rendering inside of
 * [OverlayWindow] and will only be applied as a vertical alignment. Default alignment is Top to Top,
 * which means that the top of the content will be aligned with the top of anchor view.
 *
 * @return Returns an instance of [AnchorViewInfo.Base] with the specified alignments, view size
 * and position.
 * */
@Stable
fun LayoutCoordinates.anchorViewInfo(
    density: Density,
    horizontalAlignment: HorizontalAlignment = HorizontalAlignment(
        HorizontalReferencePoint.Start,
        HorizontalReferencePoint.Start
    ),
    verticalAlignment: VerticalAlignment = VerticalAlignment(
        VerticalReferencePoint.Top,
        VerticalReferencePoint.Top
    ),
): AnchorViewInfo {
    return with(density) {
        val viewPosition = DpOffset(
            positionInRoot().x.toDp(),
            positionInRoot().y.toDp()
        )
        val viewSize = size.toSize().toDpSize()
        AnchorViewInfo.Base(viewSize, viewPosition, horizontalAlignment, verticalAlignment)
    }
}