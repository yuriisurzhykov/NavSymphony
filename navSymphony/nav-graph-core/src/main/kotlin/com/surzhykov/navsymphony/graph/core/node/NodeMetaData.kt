package com.surzhykov.navsymphony.graph.core.node

import androidx.compose.runtime.Immutable
import com.surzhykov.navsymphony.screen.core.ScreenRoute
import kotlin.reflect.KClass
import kotlin.time.Duration

/**
 * Metadata associated with a navigation node.
 *
 * This class holds information about a specific node in the navigation graph, including its unique
 * identifier, the associated [ScreenRoute], an optional builder for creating the [ScreenRoute]
 * instance in case when the node declared in menu screen, and any navigation requirements that must
 * be met before navigating to this node.
 *
 * @property nodeId A unique identifier for this node.
 * @property routeKClass The Kotlin class representing the [ScreenRoute] associated with this node.
 * @property routeBuilder An optional lambda function that can be used to build an instance of the
 * [ScreenRoute] associated with this node. This is useful when the [ScreenRoute] needs to be
 * dynamically constructed, e.g., with arguments. If `null`, it means the route will have to be
 * retrieved some other way e.g. from a `ScreenRoute` static instance.
 * @property requirements A set of [NavigationRequirement] objects that must be met before
 * navigating to this node. These requirements can represent various conditions, such as
 * authentication status, data availability, or permissions.
 *
 * @constructor Creates a [NodeMetaData] instance with the specified properties.
 */
@Immutable
@ConsistentCopyVisibility
data class NodeMetaData internal constructor(
    val nodeId: NodeId,
    val routeKClass: KClass<out ScreenRoute>,
    val screenTimeoutDuration: Duration,
    val routeBuilder: (() -> ScreenRoute)? = null,
    val requirements: NavigationRequirementHolder = NavigationRequirementHolder(emptyMap()),
) {
    constructor(
        route: KClass<out ScreenRoute>,
        timeoutLimit: Duration,
        requirements: Set<NavigationRequirement> = emptySet(),
        routeBuilder: (() -> ScreenRoute)? = null,
    ) : this(
        NodeId(route),
        route,
        timeoutLimit,
        routeBuilder,
        NavigationRequirementHolder(requirements.associateBy { it::class })
    )
}