package com.surzhykov.navsymphony.window.overlay

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

@Immutable
interface AnchorViewInfo {

    companion object {
        /** A default [AnchorViewInfo] with zero size and position. */
        val Zero: AnchorViewInfo = Simple(DpSize.Zero, DpOffset.Zero)

        /** An unspecified [AnchorViewInfo] with unspecified size and position. */
        val Unspecified: AnchorViewInfo = Simple(DpSize.Unspecified, DpOffset.Unspecified)
    }

    /**
     * Computes the offset for positioning content relative to the anchor,
     * based on the specified horizontal and vertical alignment.
     *
     * @param contentSize The size of the content to be displayed.
     * @param screenSize The size of the screen in dp. This is used to clamp the final offset
     * so that the content does not exceed screen bounds.
     * @return A [DpOffset] representing the top-left position where the content should be placed.
     */
    @Stable
    fun contentOffset(contentSize: DpSize, screenSize: DpSize): DpOffset

    /**
     * An immutable data class that contains information about the view used as an anchor for an [OverlayWindow].
     *
     * The anchor view information includes the size and absolute position in global coordinates as well as
     * the desired horizontal and vertical alignment for the content.
     *
     * @property size The actual size of the anchor view.
     * @property absoluteAnchorPosition The absolute position of the anchor view in global coordinates.
     * @property horizontalAlignment The horizontal alignment for positioning content relative to the anchor.
     * @property verticalAlignment The vertical alignment for positioning content relative to the anchor.
     */
    @Immutable
    data class Simple(
        private val size: DpSize,
        private val absoluteAnchorPosition: DpOffset,
        private val horizontalAlignment: HorizontalWindowAlignment = HorizontalWindowAlignment.Start,
        private val verticalAlignment: VerticalWindowAlignment = VerticalWindowAlignment.Top,
    ) : AnchorViewInfo {
        @Stable
        override fun contentOffset(contentSize: DpSize, screenSize: DpSize): DpOffset {
            // Calculate the base offset without screen bounds constraints.
            val baseX = when (horizontalAlignment) {
                HorizontalWindowAlignment.Start -> absoluteAnchorPosition.x
                HorizontalWindowAlignment.Center -> absoluteAnchorPosition.x + (size.width / 2) - (contentSize.width / 2)
                HorizontalWindowAlignment.End -> absoluteAnchorPosition.x + size.width - contentSize.width
                HorizontalWindowAlignment.BeforeStart -> absoluteAnchorPosition.x - contentSize.width
                HorizontalWindowAlignment.BeforeCenter -> absoluteAnchorPosition.x + (size.width / 2) - contentSize.width
            }
            val baseY = when (verticalAlignment) {
                VerticalWindowAlignment.Top -> absoluteAnchorPosition.y
                VerticalWindowAlignment.Center -> absoluteAnchorPosition.y + (size.height / 2)
                VerticalWindowAlignment.Bottom -> absoluteAnchorPosition.y + size.height
            }

            // Clamp the computed offset so that the content does not exceed screen bounds.
            val clampedX = baseX.coerceAtLeast(0.dp)
                .coerceAtMost((screenSize.width - contentSize.width).coerceAtLeast(0.dp))
            val clampedY = baseY.coerceAtLeast(0.dp)
                .coerceAtMost((screenSize.height - contentSize.height).coerceAtLeast(0.dp))

            return DpOffset(clampedX, clampedY)
        }
    }

    @Immutable
    data class Base(
        private val size: DpSize,
        private val absolutePosition: DpOffset,
        val horizontalAlignment: HorizontalAlignment = HorizontalAlignment(
            HorizontalReferencePoint.Start,
            HorizontalReferencePoint.Start
        ),
        val verticalAlignment: VerticalAlignment = VerticalAlignment(
            VerticalReferencePoint.Top,
            VerticalReferencePoint.Top
        ),
    ) : AnchorViewInfo {
        /**
         * Computes the offset for positioning content relative to the anchor.
         *
         * The computation is performed separately for the horizontal and vertical dimensions.
         * For the horizontal dimension:
         * ```
         * anchorPointX = absolutePosition.x + when (horizontalAlignment.anchorReference) {
         *     ReferencePoint.Start -> 0.dp
         *     ReferencePoint.Center -> size.width / 2
         *     ReferencePoint.End -> size.width
         * }
         *
         * contentOffsetX = when (horizontalAlignment.contentReference) {
         *     ReferencePoint.Start -> 0.dp
         *     ReferencePoint.Center -> contentSize.width / 2
         *     ReferencePoint.End -> contentSize.width
         * }
         *
         * baseX = anchorPointX - contentOffsetX
         * ```
         * And similarly for the vertical dimension.
         *
         * After computing the base offset, the values are clamped so that the content does not exceed
         * the screen bounds.
         *
         * @param contentSize The size of the content to be displayed.
         * @param screenSize The size of the screen in dp.
         * @return A [DpOffset] representing the top-left position where the content should be placed.
         */
        @Stable
        override fun contentOffset(contentSize: DpSize, screenSize: DpSize): DpOffset {
            // Calculate horizontal reference points.
            val anchorPointX = when (horizontalAlignment.anchorReference) {
                HorizontalReferencePoint.Start -> absolutePosition.x
                HorizontalReferencePoint.Center -> absolutePosition.x + (size.width / 2)
                HorizontalReferencePoint.End -> absolutePosition.x + size.width
            }
            val contentRefX = when (horizontalAlignment.contentReference) {
                HorizontalReferencePoint.Start -> 0.dp
                HorizontalReferencePoint.Center -> contentSize.width / 2
                HorizontalReferencePoint.End -> contentSize.width
            }
            val baseX = anchorPointX - contentRefX

            // Calculate vertical reference points.
            val anchorPointY = when (verticalAlignment.anchorReference) {
                VerticalReferencePoint.Top -> absolutePosition.y
                VerticalReferencePoint.Center -> absolutePosition.y + (size.height / 2)
                VerticalReferencePoint.Bottom -> absolutePosition.y + size.height
            }
            val contentRefY = when (verticalAlignment.contentReference) {
                VerticalReferencePoint.Top -> 0.dp
                VerticalReferencePoint.Center -> contentSize.height / 2
                VerticalReferencePoint.Bottom -> contentSize.height
            }
            val baseY = anchorPointY - contentRefY

            // Clamp the computed offsets so that the content remains within screen bounds.
            val clampedX = baseX.coerceAtLeast(0.dp)
                .coerceAtMost((screenSize.width - contentSize.width).coerceAtLeast(0.dp))
            val clampedY = baseY.coerceAtLeast(0.dp)
                .coerceAtMost((screenSize.height - contentSize.height).coerceAtLeast(0.dp))

            return DpOffset(clampedX, clampedY)
        }
    }
}