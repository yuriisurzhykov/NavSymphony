package com.surzhykov.navsymphony.graph.builder.nodes

import androidx.compose.runtime.Immutable
import androidx.navigation.NavType
import com.surzhykov.navsymphony.graph.core.ViewModelBuilder
import com.surzhykov.navsymphony.graph.core.node.LazyScreenInitializer
import com.surzhykov.navsymphony.graph.core.node.NavigationNode
import com.surzhykov.navsymphony.graph.core.node.NodeAppearance
import com.surzhykov.navsymphony.graph.core.node.NodeMetaData
import com.surzhykov.navsymphony.screen.core.AbstractViewModel
import com.surzhykov.navsymphony.screen.core.ScreenIntent
import com.surzhykov.navsymphony.screen.core.ScreenRoute
import com.surzhykov.navsymphony.screen.core.ScreenState
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * Represents a navigation node that can be automatically navigated to.
 *
 * This node defines a screen destination within a navigation graph that can be reached via a
 * generated route. It holds essential details like the screen, route, view model, and serialization
 * information. This class is fundamental for defining screens within a navigation graph that
 * supports automatic route construction.
 *
 * @property screenInitializer The [Screen] associated with this node.
 * @property routeKClass The [KClass] of the route type.
 * @property viewModelKClass The [KClass] of the view model type.
 * @property serializer The [KSerializer] for route serialization.
 * @property parametersMap A map of parameter types to their [NavType].
 * @property viewModelBuilder A builder for creating the view model.
 * @property nodeMetaData Metadata for the node (e.g., unique ID).
 * @property nodeAppearanceInfo Visual appearance details of the node.
 * @property routeBuilder A lambda that constructs the [ScreenRoute].
 */
@Immutable
internal class AutoNavigableNode<R, S, I, VM>(
    screen: LazyScreenInitializer<R, S, I, VM>,
    routeKClass: KClass<R>,
    viewModelKClass: KClass<VM>,
    serializer: KSerializer<R>,
    parametersMap: Map<KType, NavType<*>>,
    viewModelBuilder: ViewModelBuilder,
    nodeMetaData: NodeMetaData,
    nodeAppearanceInfo: NodeAppearance,
    override val routeBuilder: () -> ScreenRoute,
) : BaseNavigationNode<R, S, I, VM>(
    routeKClass,
    viewModelKClass,
    serializer,
    viewModelBuilder,
    screen,
    parametersMap,
    nodeMetaData,
    nodeAppearanceInfo,
),
    NavigationNode.AutoNavigableNode where R : ScreenRoute, S : ScreenState, I : ScreenIntent, VM : AbstractViewModel<S, I> {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AutoNavigableNode<*, *, *, *>) return false
        if (!super.equals(other)) return false

        if (routeBuilder != other.routeBuilder) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + routeBuilder.hashCode()
        return result
    }
}