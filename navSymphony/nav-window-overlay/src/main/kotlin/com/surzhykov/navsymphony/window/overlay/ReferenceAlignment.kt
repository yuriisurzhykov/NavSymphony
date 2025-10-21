package com.surzhykov.navsymphony.window.overlay

/**
 * Represents a reference point along a dimension.
 */
enum class HorizontalReferencePoint {
    /** The start (left or top) of the element. */
    Start,

    /** The center of the element. */
    Center,

    /** The end (right or bottom) of the element. */
    End
}

enum class VerticalReferencePoint {
    /** The start (left or top) of the element. */
    Top,

    /** The center of the element. */
    Center,

    /** The end (bottom) of the element. */
    Bottom
}

/**
 * This class encapsulate the idea that an alignment is defined by two reference points: one for the
 * anchor (the target reference) and one for the content (the point that will be aligned to the target).
 * You may think of targeting points this way: firstly, you define what point of your content should
 * be aligned to something. It can be any of 9 points of view: 3 at the top, 3 in the middle and 3
 * at the bottom of view. Then you define a target to which anchor a content. Then you define the
 * anchor point to which the content is going to be linked. The target anchor point also can be one
 * of the 9 points of an anchor: 3 at the top, 3 in the middle and 3 at the bottom of view.
 *
 * This class only works for horizontal alignment of content. To learn about vertical alignment,
 * see [VerticalAlignment].
 *
 * - Case 1: Left edge of content aligns with start of anchor
 * ```
 * HorizontalAlignment(anchorReference = HorizontalHorizontalReferencePoint.Start, contentReference = HorizontalReferencePoint.Start)
 * ```
 * - Case 2: Left edge of content aligns with center of anchor
 * ```
 * HorizontalAlignment(anchorReference = HorizontalReferencePoint.Center, contentReference = HorizontalReferencePoint.Start)
 * ```
 * - Case 3: Left edge of content aligns with end of anchor
 * ```
 * HorizontalAlignment(anchorReference = HorizontalReferencePoint.End, contentReference = HorizontalReferencePoint.Start)
 * ```
 * - Case 4: Center of content aligns with start of anchor
 * ```
 * HorizontalAlignment(anchorReference = HorizontalReferencePoint.Start, contentReference = HorizontalReferencePoint.Center)
 * ```
 * - Case 5: Center of content aligns with center of anchor
 * ```
 * HorizontalAlignment(anchorReference = HorizontalReferencePoint.Center, contentReference = HorizontalReferencePoint.Center)
 * ```
 * - Case 6: Center of content aligns with end of anchor
 * ```
 * HorizontalAlignment(anchorReference = HorizontalReferencePoint.End, contentReference = HorizontalReferencePoint.Center)
 * ```
 * - Case 7: Right edge of content aligns with start of anchor
 * ```
 * HorizontalAlignment(anchorReference = HorizontalReferencePoint.Start, contentReference = HorizontalReferencePoint.End)
 * ```
 * - Case 8: Right edge of content aligns with center of anchor
 * ```
 * HorizontalAlignment(anchorReference = HorizontalReferencePoint.Center, contentReference = HorizontalReferencePoint.End)
 * ```
 * - Case 9: Right edge of content aligns with end of anchor
 * ```
 * HorizontalAlignment(anchorReference = HorizontalReferencePoint.End, contentReference = HorizontalReferencePoint.End)
 * ```
 *
 * @property anchorReference The reference point in the anchor view.
 * @property contentReference The reference point in the content view.
 */
data class HorizontalAlignment(
    val contentReference: HorizontalReferencePoint,
    val anchorReference: HorizontalReferencePoint,
)

/**
 * This class encapsulate the idea that an alignment is defined by two reference points: one for the
 * anchor (the target reference) and one for the content (the point that will be aligned to the target)
 * You may think of targeting points this way: firstly, you define what point of your content should
 * be aligned to something. It can be any of 9 points of view: 3 at the top, 3 in the middle and 3
 * at the bottom of view. Then you define a target to which anchor a content. Then you define the
 * anchor point to which the content is going to be linked. The target anchor point also can be one
 * of the 9 points of an anchor: 3 at the top, 3 in the middle and 3 at the bottom of view.
 *
 * This class only works for vertical alignment of content. To learn about horizontal alignment,
 * see [HorizontalAlignment]
 *
 * For example, the nine horizontal cases you listed can be defined as follows:
 * - Case 1: Left edge of content aligns with start of anchor
 * ```
 * VerticalAlignment(anchorReference = HorizontalHorizontalReferencePoint.Start, contentReference = HorizontalReferencePoint.Start)
 * ```
 * - Case 2: Left edge of content aligns with center of anchor
 * ```
 * VerticalAlignment(anchorReference = VerticalReferencePoint.Center, contentReference = VerticalReferencePoint.Start)
 * ```
 * - Case 3: Left edge of content aligns with end of anchor
 * ```
 * VerticalAlignment(anchorReference = VerticalReferencePoint.End, contentReference = VerticalReferencePoint.Start)
 * ```
 * - Case 4: Center of content aligns with start of anchor
 * ```
 * VerticalAlignment(anchorReference = VerticalReferencePoint.Start, contentReference = VerticalReferencePoint.Center)
 * ```
 * - Case 5: Center of content aligns with center of anchor
 * ```
 * VerticalAlignment(anchorReference = VerticalReferencePoint.Center, contentReference = VerticalReferencePoint.Center)
 * ```
 * - Case 6: Center of content aligns with end of anchor
 * ```
 * VerticalAlignment(anchorReference = VerticalReferencePoint.End, contentReference = VerticalReferencePoint.Center)
 * ```
 * - Case 7: Right edge of content aligns with start of anchor
 * ```
 * VerticalAlignment(anchorReference = VerticalReferencePoint.Start, contentReference = VerticalReferencePoint.End)
 * ```
 * - Case 8: Right edge of content aligns with center of anchor
 * ```
 * VerticalAlignment(anchorReference = VerticalReferencePoint.Center, contentReference = VerticalReferencePoint.End)
 * ```
 * - Case 9: Right edge of content aligns with end of anchor
 * ```
 * VerticalAlignment(anchorReference = VerticalReferencePoint.End, contentReference = VerticalReferencePoint.End)
 * ```
 *
 * @property anchorReference The reference point in the anchor view.
 * @property contentReference The reference point in the content view.
 */
data class VerticalAlignment(
    val contentReference: VerticalReferencePoint,
    val anchorReference: VerticalReferencePoint,
)

/**
 * Creates a [HorizontalAlignment] using current [HorizontalReferencePoint] and provided at the left.
 * Current point (which is at the left side of statement) is used as a [HorizontalAlignment.contentReference]
 * and the right one is used as a [HorizontalAlignment.anchorReference].
 * */
infix fun HorizontalReferencePoint.to(referencePoint: HorizontalReferencePoint): HorizontalAlignment {
    return HorizontalAlignment(this, referencePoint)
}

/**
 * Creates a [VerticalAlignment] using current [VerticalReferencePoint] and provided at the left.
 * Current point (which is at the left side of statement) is used as a [VerticalAlignment.contentReference]
 * and the right one is used as a [VerticalAlignment.anchorReference].
 * */
infix fun VerticalReferencePoint.to(referencePoint: VerticalReferencePoint): VerticalAlignment {
    return VerticalAlignment(this, referencePoint)
}