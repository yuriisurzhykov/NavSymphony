package com.surzhykov.navsymphony.graph.builder

import com.surzhykov.navsymphony.graph.builder.nodes.MenuNavigationNode
import com.surzhykov.navsymphony.graph.builder.utils.Timeouts
import com.surzhykov.navsymphony.graph.core.ViewModelBuilder
import com.surzhykov.navsymphony.graph.core.graph.NavigationGraphDsl
import com.surzhykov.navsymphony.graph.core.node.LazyScreenInitializer
import com.surzhykov.navsymphony.graph.core.node.NavigationNode
import com.surzhykov.navsymphony.graph.core.node.NavigationRequirement
import com.surzhykov.navsymphony.graph.core.node.NodeAppearance
import com.surzhykov.navsymphony.graph.core.node.NodeMetaData
import com.surzhykov.navsymphony.screen.core.ScreenRoute
import com.surzhykov.navsympnony.menu.MenuScreenIntent
import com.surzhykov.navsympnony.menu.MenuScreenState
import com.surzhykov.navsympnony.menu.MenuScreenViewModel
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass
import kotlin.time.Duration

@NavigationGraphDsl
class MenuNodeBuilder<R, VM>(
    private val viewModelBuilder: ViewModelBuilder,
    private val screen: LazyScreenInitializer<R, MenuScreenState, MenuScreenIntent, VM>,
    private val appearance: NodeAppearance,
    private val serializer: KSerializer<R>,
    private val routeClass: KClass<R>,
    private val viewModelClass: KClass<VM>,
    private val routeBuilder: (() -> ScreenRoute),
    screenTimeoutDuration: Duration = Timeouts.DEFAULT_SCREEN_TIMEOUT,
    requirements: MutableMap<NavigationRequirement, NavigationRequirement> = mutableMapOf(),
) : AbstractNodeBuilder(screenTimeoutDuration, requirements),
    NavigationDslBuilder<NavigationNode.MenuNode> where R : ScreenRoute.Menu, VM : MenuScreenViewModel {

    private val items = LinkedHashSet<NavigationNode>()

    fun items(init: MenuItemsBuilder.() -> Unit): MenuNodeBuilder<R, VM> {
        val builder = MenuItemsBuilder(
            screenTimeoutDuration = screenTimeoutDuration,
            requirements = HashMap(requirements),
            viewModelBuilder = viewModelBuilder
        ).apply(init)
        items.addAll(builder.build())
        return this
    }

    override fun build(): NavigationNode.MenuNode = MenuNavigationNode(
        routeClass,
        viewModelClass,
        serializer,
        viewModelBuilder,
        screen,
        parametersTypeMap,
        NodeMetaData(
            routeClass,
            screenTimeoutDuration,
            LinkedHashSet(requirements.values),
            routeBuilder
        ),
        appearance,
        items,
        routeBuilder,
    )
}