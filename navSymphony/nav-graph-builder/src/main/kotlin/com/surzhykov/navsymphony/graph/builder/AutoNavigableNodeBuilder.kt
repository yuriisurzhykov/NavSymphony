package com.surzhykov.navsymphony.graph.builder

import com.surzhykov.navsymphony.graph.builder.nodes.AutoNavigableNode
import com.surzhykov.navsymphony.graph.builder.utils.Timeouts
import com.surzhykov.navsymphony.graph.core.ViewModelBuilder
import com.surzhykov.navsymphony.graph.core.graph.NavigationGraphDsl
import com.surzhykov.navsymphony.graph.core.node.LazyScreenInitializer
import com.surzhykov.navsymphony.graph.core.node.NavigationNode
import com.surzhykov.navsymphony.graph.core.node.NavigationRequirement
import com.surzhykov.navsymphony.graph.core.node.NodeAppearance
import com.surzhykov.navsymphony.graph.core.node.NodeMetaData
import com.surzhykov.navsymphony.screen.core.AbstractViewModel
import com.surzhykov.navsymphony.screen.core.ScreenIntent
import com.surzhykov.navsymphony.screen.core.ScreenRoute
import com.surzhykov.navsymphony.screen.core.ScreenState
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass
import kotlin.time.Duration

/**
 * `AutoNavigableNodeBuilder` is a builder for creating `AutoNavigableNode` instances within
 * a  Navigation Graph. It configures a navigation node by associating a [Screen] with its
 * [ScreenRoute], [ScreenState], [ScreenIntent], and [AbstractViewModel], handling route
 * serialization and ultimately builds a [NavigationNode.AutoNavigableNode].
 */
@NavigationGraphDsl
class AutoNavigableNodeBuilder<R, S, I, VM>(
    private val viewModelBuilder: ViewModelBuilder,
    private val screen: LazyScreenInitializer<R, S, I, VM>,
    private val appearance: NodeAppearance,
    private val serializer: KSerializer<R>,
    private val routeClass: KClass<R>,
    private val viewModelClass: KClass<VM>,
    private val routeBuilder: (() -> ScreenRoute),
    screenTimeoutDuration: Duration = Timeouts.DEFAULT_SCREEN_TIMEOUT,
    requirements: MutableMap<NavigationRequirement, NavigationRequirement> = mutableMapOf(),
) : AbstractNodeBuilder(screenTimeoutDuration, requirements),
    NavigationDslBuilder<NavigationNode.AutoNavigableNode> where R : ScreenRoute, S : ScreenState, I : ScreenIntent, VM : AbstractViewModel<S, I> {

    override fun build(): NavigationNode.AutoNavigableNode = AutoNavigableNode(
        screen,
        routeClass,
        viewModelClass,
        serializer,
        parametersTypeMap,
        viewModelBuilder,
        nodeMetaData(),
        appearance,
        routeBuilder,
    )

    private fun nodeMetaData() =
        NodeMetaData(routeClass, screenTimeoutDuration, requirements.values.toSet(), routeBuilder)
}