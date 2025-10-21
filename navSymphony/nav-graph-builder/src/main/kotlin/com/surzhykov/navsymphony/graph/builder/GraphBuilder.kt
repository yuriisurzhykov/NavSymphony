package com.surzhykov.navsymphony.graph.builder

import com.surzhykov.navsymphony.graph.core.ViewModelBuilder
import com.surzhykov.navsymphony.graph.core.graph.NavigationGraph
import com.surzhykov.navsymphony.graph.core.graph.NavigationGraphDsl
import com.surzhykov.navsymphony.graph.core.node.LazyScreenInitializer
import com.surzhykov.navsymphony.graph.core.node.NavigationNode
import com.surzhykov.navsymphony.graph.core.node.NodeAppearance
import com.surzhykov.navsymphony.graph.core.node.NodeId
import com.surzhykov.navsymphony.screen.core.AbstractViewModel
import com.surzhykov.navsymphony.screen.core.Screen
import com.surzhykov.navsymphony.screen.core.ScreenIntent
import com.surzhykov.navsymphony.screen.core.ScreenRoute
import com.surzhykov.navsymphony.screen.core.ScreenState
import com.surzhykov.navsympnony.menu.MenuScreen
import com.surzhykov.navsympnony.menu.MenuScreenViewModel
import kotlinx.serialization.KSerializer
import java.util.LinkedList
import kotlin.reflect.KClass

/**
 * DSL builder for constructing a [NavigationGraph].
 *
 * This class provides a fluent interface for defining the structure of a navigation graph,
 * including the screens and their associated metadata. It uses the `@NavigationGraphDsl`
 * annotation to mark its methods as part of the navigation graph DSL.
 *
 * @property viewModelBuilder The builder responsible for creating ViewModels. This will be provided
 * to all navigation nodes so they can be composed in Jetpack Compose. So make sure to pass the
 * instance of [ViewModelBuilder] that will be able to create all ViewModels.
 */
@NavigationGraphDsl
open class GraphBuilder(
    private val viewModelBuilder: ViewModelBuilder,
    private val initialRoute: KClass<out ScreenRoute>,
) : NavigationDslBuilder<NavigationGraph> {

    private val screenNodes = HashMap<NodeId, NavigationNode>()

    /**
     * Registers a new screen within the navigation graph.
     *
     * This function allows you to define a screen's structure, its associated route, state, intent,
     * and view model, along with its visual appearance. It then integrates this screen into the
     * overall navigation graph.
     *
     * @param screen The [Screen] instance representing the logical UI structure of the screen. This
     * defines the base behaviour of the screen.
     * @param appearance The [NodeAppearance] that defines how this screen should visually appear
     * within the navigation graph (e.g., title, icon).
     * @param serializer The [KSerializer] used for serializing and deserializing the screen's route
     * data. This allows for type-safe navigation arguments.
     * @param routeClass The [KClass] representing the screen's route data class (e.g.,
     * `HomeScreenRoute::class`). This class should implement the [ScreenRoute] interface.
     * @param viewModelClass The [KClass] representing the screen's view model class (e.g.,
     * `HomeScreenViewModel::class`). This class should inherit from [AbstractViewModel].
     * @param init A lambda function ([ScreenNodeBuilder].() -> Unit) that allows you to configure
     * the screen's properties, such as composables, transitions, or other behaviors, using the
     * [ScreenNodeBuilder].
     *
     * @return The [GraphBuilder] instance, enabling fluent chaining of screen registrations.
     *
     * @throws IllegalStateException If a screen with the same route has already been registered in the graph. This ensures unique routes within the navigation graph.
     * */
    fun <R, S, I, VM> screen(
        screen: KClass<out Screen<R, S, I, VM>>,
        appearance: NodeAppearance,
        serializer: KSerializer<R>,
        routeClass: KClass<R>,
        viewModelClass: KClass<VM>,
        init: ScreenNodeBuilder<R, S, I, VM>.() -> Unit,
    ): GraphBuilder where R : ScreenRoute, S : ScreenState, I : ScreenIntent, VM : AbstractViewModel<S, I> {
        val builder = ScreenNodeBuilder(
            viewModelBuilder,
            LazyScreenInitializer.ByClass(screen),
            appearance,
            serializer,
            routeClass,
            viewModelClass,
            null
        ).apply(init)
        val newScreenNode = builder.build()
        if (screenNodes.containsKey(newScreenNode.nodeMetaData.nodeId)) {
            throw IllegalStateException("Screen with route ${newScreenNode.nodeMetaData.routeKClass} already exists in the graph")
        }
        screenNodes[newScreenNode.nodeMetaData.nodeId] = newScreenNode
        return this
    }

    fun <R, S, I, VM> screen(
        screen: () -> Screen<R, S, I, VM>,
        appearance: NodeAppearance,
        serializer: KSerializer<R>,
        routeClass: KClass<R>,
        viewModelClass: KClass<VM>,
        init: ScreenNodeBuilder<R, S, I, VM>.() -> Unit,
    ): GraphBuilder where R : ScreenRoute, S : ScreenState, I : ScreenIntent, VM : AbstractViewModel<S, I> {
        val builder = ScreenNodeBuilder(
            viewModelBuilder,
            LazyScreenInitializer.ByInstance(screen),
            appearance,
            serializer,
            routeClass,
            viewModelClass,
            null
        ).apply(init)
        val newScreenNode = builder.build()
        if (screenNodes.containsKey(newScreenNode.nodeMetaData.nodeId)) {
            throw IllegalStateException("Screen with route ${newScreenNode.nodeMetaData.routeKClass} already exists in the graph")
        }
        screenNodes[newScreenNode.nodeMetaData.nodeId] = newScreenNode
        return this
    }

    /**
     * Defines a menu screen within the navigation graph.
     *
     * This function is used to configure and add a new menu screen node to the navigation graph.
     * It allows you to specify the screen's appearance, route, and view model, as well as
     * customize the behavior and layout using the [MenuNodeBuilder].
     *
     * Example Usage:
     * ```kotlin
     * graph {
     *     menu(
     *         screen = MyMenuScreen(),
     *         appearance = MyMenuAppearance(),
     *         serializer = MyMenuRoute.serializer(),
     *         routeClass = MyMenuRoute::class,
     *         viewModelClass = MyMenuViewModel::class,
     *         route = { MyMenuRoute(initialData = "Hello") },
     *     ) {
     *         items {
     *              screen(ScreenA(), ...)
     *              screen(ScreenB(), ...)
     *              menu(SubMenu(), ...)
     *         }
     *         // Other configurations for the Menu
     *     }
     * }
     * ```
     *
     * @param screen The [MenuScreen] implementation representing the UI of the menu.
     * @param appearance The [NodeAppearance] defining the visual style of the menu node.
     * @param serializer The [KSerializer] used for serializing and deserializing the route data.
     * @param routeClass The [KClass] representing the type of the screen's route (e.g., `MyMenuRoute::class`).
     * @param viewModelClass The [KClass] representing the type of the screen's view model (e.g., `MyMenuViewModel::class`).
     * @param route A lambda function that returns an instance of the screen's route. This is typically used
     *              to create a new instance of the route object when navigating to this screen.
     * @param init A lambda function that provides a [MenuNodeBuilder] instance for configuring the menu node.
     *             This is where you can add menu items, define their behavior, and customize the
     *             node's properties.
     * @return The [GraphBuilder] instance, allowing for chaining of screen definitions.
     * @throws IllegalStateException If a screen with the same route already exists in the graph.
     * */
    fun <R, VM> menu(
        screen: () -> MenuScreen<R, VM>,
        appearance: NodeAppearance,
        serializer: KSerializer<R>,
        routeClass: KClass<R>,
        viewModelClass: KClass<VM>,
        route: () -> R,
        init: MenuNodeBuilder<R, VM>.() -> Unit,
    ): GraphBuilder where R : ScreenRoute.Menu, VM : MenuScreenViewModel {
        val builder = MenuNodeBuilder(
            viewModelBuilder,
            LazyScreenInitializer.ByInstance(screen),
            appearance,
            serializer,
            routeClass,
            viewModelClass,
            route
        ).apply(init)
        val newScreenNode = builder.build()
        if (screenNodes.containsKey(newScreenNode.nodeMetaData.nodeId)) {
            throw IllegalStateException("Screen with route ${newScreenNode.nodeMetaData.routeKClass} already exists in the graph")
        }
        screenNodes[newScreenNode.nodeMetaData.nodeId] = newScreenNode
        return this
    }

    override fun build(): NavigationGraph =
        NavigationGraph.Base(LinkedList(screenNodes.values), initialRoute)
}