package com.surzhykov.navsymphony.graph.builder

import com.surzhykov.navsymphony.graph.core.ViewModelBuilder
import com.surzhykov.navsymphony.graph.core.graph.NavigationGraphDsl
import com.surzhykov.navsymphony.graph.core.node.LazyScreenInitializer
import com.surzhykov.navsymphony.graph.core.node.NavigationNode
import com.surzhykov.navsymphony.graph.core.node.NavigationRequirement
import com.surzhykov.navsymphony.graph.core.node.NodeAppearance
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
import kotlin.time.Duration

/**
 * `MenuItemsBuilder` constructs a list of `NavigationNode.AutoNavigableNode` instances for a
 * navigation graph. It provides methods to define both regular `screen()` and `menu()` navigation
 * nodes, ensuring route uniqueness.
 *
 * @property viewModelBuilder The [ViewModelBuilder] used to create view models for screens.
 * @property screenNodes A [LinkedList] holding the built `NavigationNode.AutoNavigableNode` instances.
 */
@NavigationGraphDsl
class MenuItemsBuilder(
    private val screenTimeoutDuration: Duration,
    private val requirements: MutableMap<NavigationRequirement, NavigationRequirement>,
    private val viewModelBuilder: ViewModelBuilder,
    private val screenNodes: LinkedHashMap<KClass<out ScreenRoute>, NavigationNode> = LinkedHashMap(),
) : NavigationDslBuilder<Sequence<NavigationNode>> {

    /**
     * Defines and registers a new screen in the navigation graph.
     *
     * This function creates a screen node with its route, state, intent, ViewModel, and appearance.
     * It uses a builder pattern (`AutoNavigableNodeBuilder`) for flexible configuration.
     *
     * @param screen The [Screen] defining the UI of the screen.
     * @param appearance The visual settings ([NodeAppearance]) for the screen.
     * @param serializer The [KSerializer] for the screen's route data.
     * @param routeClass The [KClass] of the screen's route data.
     * @param viewModelClass The [KClass] of the screen's ViewModel.
     * @param route A lambda providing an instance of the screen's route data.
     * @param init A lambda to configure the [AutoNavigableNodeBuilder].
     *
     * @return [MenuItemsBuilder] The builder for defining more screens (method chaining).
     * @throws IllegalStateException If a screen with the same route already exists.
     */
    fun <R, S, I, VM> screen(
        screen: KClass<out Screen<R, S, I, VM>>,
        appearance: NodeAppearance,
        serializer: KSerializer<R>,
        routeClass: KClass<R>,
        viewModelClass: KClass<VM>,
        route: () -> R,
        init: AutoNavigableNodeBuilder<R, S, I, VM>.() -> Unit,
    ): MenuItemsBuilder where R : ScreenRoute, S : ScreenState, I : ScreenIntent, VM : AbstractViewModel<S, I> {
        val builder = AutoNavigableNodeBuilder(
            viewModelBuilder,
            LazyScreenInitializer.ByClass(screen),
            appearance,
            serializer,
            routeClass,
            viewModelClass,
            route,
            screenTimeoutDuration,
            HashMap(requirements)
        ).apply(init)
        val newScreenNode = builder.build()
        if (screenNodes.containsKey(routeClass)) {
            throw IllegalStateException("Screen with route ${newScreenNode.nodeMetaData.routeKClass} already exists in the graph")
        }
        screenNodes[routeClass] = newScreenNode
        return this
    }

    /**
     * Defines and registers a new screen within the navigation graph.
     *
     * This function configures a screen, specifying its route, state, intent, view model, and
     * appearance, and adds it to the internal list of available screens. It also ensures that
     * no duplicate screens with the same route are added.
     *
     * @param screen A lambda that provides the [Screen] instance for this screen. This allows
     * for lazy initialization of the screen object itself.
     * @param appearance The visual appearance of the screen, defined by a [NodeAppearance] instance.
     * @param serializer The [KSerializer] used to serialize and deserialize the route data of
     * type [R].
     * @param routeClass The [KClass] representing the route data type [R].
     * @param viewModelClass The [KClass] representing the view model type [VM] associated with
     * this screen.
     * @param route A lambda that provides the route object [R] instance for this screen. This
     * defines how to access the screen.
     * @param init A lambda with a [AutoNavigableNodeBuilder] receiver that allows for further
     * customization of the screen. This includes setting up dependencies, navigation actions,
     * and other screen-specific configurations.
     * @return This [MenuItemsBuilder] instance, allowing for method chaining when defining
     * multiple screens.
     * @throws IllegalStateException If a screen with the same route (defined by `routeClass`)
     * already exists in the navigation graph.
     */
    fun <R, S, I, VM> screen(
        screen: () -> Screen<R, S, I, VM>,
        appearance: NodeAppearance,
        serializer: KSerializer<R>,
        routeClass: KClass<R>,
        viewModelClass: KClass<VM>,
        route: () -> R,
        init: AutoNavigableNodeBuilder<R, S, I, VM>.() -> Unit,
    ): MenuItemsBuilder where R : ScreenRoute, S : ScreenState, I : ScreenIntent, VM : AbstractViewModel<S, I> {
        val builder = AutoNavigableNodeBuilder(
            viewModelBuilder,
            LazyScreenInitializer.ByInstance(screen),
            appearance,
            serializer,
            routeClass,
            viewModelClass,
            route,
            screenTimeoutDuration,
            HashMap(requirements)
        ).apply(init)
        val newScreenNode = builder.build()
        if (screenNodes.containsKey(routeClass)) {
            throw IllegalStateException("Screen with route ${newScreenNode.nodeMetaData.routeKClass} already exists in the graph")
        }
        screenNodes[routeClass] = newScreenNode
        return this
    }

    fun <R, S, I, VM> nonMenuScreen(
        screen: KClass<out Screen<R, S, I, VM>>,
        appearance: NodeAppearance,
        serializer: KSerializer<R>,
        routeClass: KClass<R>,
        viewModelClass: KClass<VM>,
        init: ScreenNodeBuilder<R, S, I, VM>.() -> Unit,
    ): MenuItemsBuilder where R : ScreenRoute, S : ScreenState, I : ScreenIntent, VM : AbstractViewModel<S, I> {
        val builder = ScreenNodeBuilder(
            viewModelBuilder,
            LazyScreenInitializer.ByClass(screen),
            appearance,
            serializer,
            routeClass,
            viewModelClass,
            null,
            screenTimeoutDuration,
            HashMap(requirements)
        ).apply(init)
        val newScreenNode = builder.build()
        if (screenNodes.containsKey(routeClass)) {
            throw IllegalStateException("Screen with route ${newScreenNode.nodeMetaData.routeKClass} already exists in the graph")
        }
        screenNodes[routeClass] = newScreenNode
        return this
    }

    /**
     * Defines a menu item within a navigation graph.
     *
     * This function creates a menu entry, specifying its screen, appearance, route, and associated view model.
     * It allows for building nested menu structures.
     *
     * Example Usage:
     * ```kotlin
     * menu(
     *     screen = MyMenuScreen(),
     *     appearance = MyMenuAppearance(),
     *     serializer = MyRoute.serializer(),
     *     routeClass = MyRoute::class,
     *     viewModelClass = MyMenuViewModel::class,
     *     route = { MyRoute() }
     * ) {
     *     // Add child menu items here using menu() within this scope.
     * }
     * ```
     *
     * @param screen The [MenuScreen] instance for this menu item's display.
     * @param appearance The [NodeAppearance] for visual styling.
     * @param serializer The [KSerializer] for route data (for navigation).
     * @param routeClass The [KClass] of the route data, used for identification.
     * @param viewModelClass The [KClass] of the [MenuScreenViewModel].
     * @param route Lambda to provide the route data instance.
     * @param init Lambda for customizing the menu item, including defining child menus using a [MenuNodeBuilder].
     * @return [MenuItemsBuilder] which allows adding more items at the same level.
     * @throws IllegalStateException If a screen with the same route class already exists.
     * */
    fun <R, VM> menu(
        screen: () -> MenuScreen<R, VM>,
        appearance: NodeAppearance,
        serializer: KSerializer<R>,
        routeClass: KClass<R>,
        viewModelClass: KClass<VM>,
        route: () -> R,
        init: MenuNodeBuilder<R, VM>.() -> Unit,
    ): MenuItemsBuilder where R : ScreenRoute.Menu, VM : MenuScreenViewModel {
        val builder = MenuNodeBuilder(
            viewModelBuilder,
            LazyScreenInitializer.ByInstance(screen),
            appearance,
            serializer,
            routeClass,
            viewModelClass,
            route,
            screenTimeoutDuration,
            HashMap(requirements)
        ).apply(init)
        val newScreenNode = builder.build()
        if (screenNodes.containsKey(routeClass)) {
            throw IllegalStateException("Screen with route ${newScreenNode.nodeMetaData.routeKClass} already exists in the graph")
        }
        screenNodes[routeClass] = newScreenNode
        return this
    }

    override fun build(): Sequence<NavigationNode> =
        screenNodes.values.asSequence()
}