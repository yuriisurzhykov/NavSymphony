package com.surzhykov.navsymphony.window.overlay

/**
 * Represents horizontal alignment options for positioning content relative to an anchor.
 */
enum class HorizontalWindowAlignment {

    /** Content's left edge aligns with the anchor's left edge. */
    Start,

    /** Content is centered horizontally relative to the anchor. */
    Center,

    /** Content's right edge aligns with the anchor's right edge. */
    End,

    /**
     * Content's right edge aligns with the anchor's left edge.
     * This means the content will appear to the left of the anchor.
     */
    BeforeStart,

    /**
     * Content's right edge aligns with the horizontal center of the anchor.
     * (Content is positioned so that its right edge touches the center of the anchor.)
     */
    BeforeCenter
}

/**
 * Represents vertical alignment options for positioning content relative to an anchor.
 */
enum class VerticalWindowAlignment {
    /** Content's top edge aligns with the anchor's top edge. */
    Top,

    /** Content is centered vertically relative to the anchor. */
    Center,

    /** Content's bottom edge aligns with the anchor's bottom edge. */
    Bottom
}