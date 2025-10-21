package com.surzhykov.navsymphony.graph.core.node

import com.surzhykov.navsymphony.core.presentation.DrawableResolver
import com.surzhykov.navsymphony.core.presentation.StringResolver
import com.surzhykov.navsymphony.graph.core.graph.NavigationGraphDsl

/**
 * Defines the visual appearance of a node in a navigation graph.
 *
 * @property title A [StringResolver] that resolves to the display title of the node.
 * @property icon A [DrawableResolver] that resolves to the icon representing the node.
 */
@NavigationGraphDsl
data class NodeAppearance(
    val title: StringResolver,
    val icon: DrawableResolver,
)