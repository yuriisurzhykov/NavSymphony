package com.surzhykov.navsympnony.menu.domain.chain

import com.surzhykov.navsymphony.graph.core.node.NavigationNode
import com.surzhykov.navsympnony.menu.domain.builder.MenuItemBuilder

/**
 * A factory interface responsible for creating [MenuItemBuilder] instances.
 *
 * This interface provides a standardized way to generate [MenuItemBuilder] objects based on a
 * given [NavigationNode.AutoNavigableNode]. Implementations of this interface are responsible
 * for determining how to construct a [MenuItemBuilder] from the provided navigation node.
 */
interface MenuItemBuilderFactory {

    /**
     * Creates a [MenuItemBuilder] for a given [NavigationNode.AutoNavigableNode].
     *
     * @param node The [NavigationNode.AutoNavigableNode] for which to create a menu item builder.
     * This node represents a destination in the navigation graph that should be accessible via a
     * menu item. It must contain all the necessary information, such as label, icon, and route,
     * to build the menu item.
     *
     * @return A [MenuItemBuilder] configured with the details of the provided
     * [NavigationNode.AutoNavigableNode]. This builder can be used to create a concrete menu item
     * that represents the specified navigation destination.
     */
    fun create(node: NavigationNode.AutoNavigableNode): MenuItemBuilder

    class Base : MenuItemBuilderFactory {
        override fun create(node: NavigationNode.AutoNavigableNode): MenuItemBuilder =
            MenuItemBuilder(node)
    }
}