package com.surzhykov.navsymphony.graph.core.node

import com.surzhykov.navsymphony.screen.core.ScreenRoute
import kotlin.reflect.KClass

/**
 * Represents a unique identifier for a node in a navigation graph.
 *
 * This class is used to identify and distinguish different screen routes within a navigation graph.
 * It is an inline value class, meaning it provides the benefits of a type-safe wrapper around an
 * integer, without the runtime overhead of object allocation.
 *
 * @property id The underlying integer ID of the node. This should be considered an internal detail
 * and should not be relied upon directly for anything other than comparisons between [NodeId]
 * instances.
 *
 * @constructor Creates a new `NodeId` with the given integer ID. This constructor is marked as
 * internal because IDs should generally be generated from screen routes (using the other constructor).
 */
@JvmInline
value class NodeId internal constructor(val id: Int) {
    internal constructor(klass: KClass<out ScreenRoute>) : this(klass.hashCode())
}