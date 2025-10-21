package com.surzhykov.navsymphony.graph.builder

import com.surzhykov.navsymphony.graph.builder.nodes.BaseNavigationNode
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
 * A builder class for constructing [NavigationNode] instances representing a screen within a
 * navigation graph.
 *
 * This class provides a fluent interface for defining the properties of a screen node, including
 * its appearance, navigation requirements, and parameter types. It encapsulates the logic for
 * creating a [NavigationNode] based on the provided screen definition and associated metadata.
 *
 * @param R The type of the screen's route (e.g., a data class defining navigation parameters).
 * @param S The type of the screen's state (e.g., a data class representing the UI state).
 * @param I The type of the screen's intent (e.g., a sealed class representing user actions).
 * @param VM The type of the screen's ViewModel.
 * @param viewModelBuilder A builder for creating the screen's ViewModel.
 * @param screen The [Screen] instance representing the screen's UI and logic.
 * @param serializer The [KSerializer] used for serializing and deserializing the screen's route.
 * @param routeClass The [KClass] representing the screen's route type.
 * @param viewModelClass The [KClass] representing the screen's ViewModel type.
 */
@NavigationGraphDsl
open class ScreenNodeBuilder<R, S, I, VM>(
    private val viewModelBuilder: ViewModelBuilder,
    private val screen: LazyScreenInitializer<R, S, I, VM>,
    private val appearance: NodeAppearance,
    private val serializer: KSerializer<R>,
    private val routeClass: KClass<R>,
    private val viewModelClass: KClass<VM>,
    private val routeBuilder: (() -> ScreenRoute)?,
    screenTimeoutDuration: Duration = Timeouts.DEFAULT_SCREEN_TIMEOUT,
    requirements: MutableMap<NavigationRequirement, NavigationRequirement> = mutableMapOf(),
) : AbstractNodeBuilder(screenTimeoutDuration, requirements),
    NavigationDslBuilder<NavigationNode> where R : ScreenRoute, S : ScreenState, I : ScreenIntent, VM : AbstractViewModel<S, I> {

    override fun build(): NavigationNode {
        return BaseNavigationNode(
            routeClass,
            viewModelClass,
            serializer,
            viewModelBuilder,
            screen,
            parametersTypeMap,
            nodeMetaData(),
            appearance,
        )
    }

    private fun nodeMetaData() =
        NodeMetaData(routeClass, screenTimeoutDuration, requirements.values.toSet(), routeBuilder)
}