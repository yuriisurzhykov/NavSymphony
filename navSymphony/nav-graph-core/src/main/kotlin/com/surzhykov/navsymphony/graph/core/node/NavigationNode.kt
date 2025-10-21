package com.surzhykov.navsymphony.graph.core.node

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import com.surzhykov.navsymphony.graph.core.graph.NavigationGraphDsl
import com.surzhykov.navsymphony.screen.core.ScreenRoute
import kotlin.reflect.KType

/**
 * Represents a node within a navigation graph.
 *
 * A NavigationNode defines a single unit of navigation within an application.
 * It holds metadata, appearance information, and a list of its child nodes.
 * It also provides a mechanism to integrate itself into a Compose Navigation graph.
 *
 * @property nodeMetaData The metadata associated with this navigation node. This typically contains
 * information like the route and any arguments associated with this node.
 * @property nodeAppearanceInfo Information about how this node should appear visually, such as a
 * name or icon for display in a UI component.
 */
@Immutable
@NavigationGraphDsl
interface NavigationNode {

    val screenInitializer: LazyScreenInitializer<*, *, *, *>
    val parametersMap: Map<KType, NavType<*>>
    val nodeMetaData: NodeMetaData
    val nodeAppearanceInfo: NodeAppearance

    /**
     * Composes a node within the navigation graph.
     *
     * This function allows you to define and add a custom composable node to a navigation graph.
     * It acts as a placeholder or a container for a specific destination or a group of related
     * destinations within the navigation hierarchy. You can use this to logically organize your
     * navigation flow and associate composables or other navigation related actions with specific
     * nodes.
     *
     * This is analogous to building a directed graph, where each node represents a destination or a
     * point of navigation.
     *
     * Note that this function doesn't directly navigate to the composed content. It only defines a
     * node in the graph.
     *
     * @param navGraphBuilder The [NavGraphBuilder] used to construct the navigation graph. This is
     * where the new node will be added.
     *
     * @see NavGraphBuilder
     * @see androidx.navigation.compose.composable
     * @see androidx.navigation.compose.navigation
     * */
    @Stable
    fun composeNode(navGraphBuilder: NavGraphBuilder)

    /**
     * Represents a node in the navigation graph that automatically defines its own route.
     *
     * This interface extends [NavigationNode] and adds the capability for a node to self-define
     * the [ScreenRoute] it represents. This eliminates the need to manually define routes for each
     * node when building the navigation graph, as the node provides the logic itself.
     *
     * The primary benefit of using this interface is to encapsulate route generation logic within
     * the node itself, improving code organization and maintainability.
     *
     * It is expected that implementors of this interface will be used within a navigation graph
     * builder (e.g., using a library that provides composable navigation functionality).
     */
    interface AutoNavigableNode : NavigationNode {
        val routeBuilder: () -> ScreenRoute
    }

    /**
     * Represents a node in a menu structure that can have child nodes.
     * This interface extends [AutoNavigableNode] and is designed for building hierarchical menus
     * where each node can have zero or more child nodes.
     */
    interface MenuNode : AutoNavigableNode {
        val children: LinkedHashSet<NavigationNode>
    }
}