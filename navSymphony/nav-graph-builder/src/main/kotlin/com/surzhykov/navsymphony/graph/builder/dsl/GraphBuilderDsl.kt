package com.surzhykov.navsymphony.graph.builder.dsl

import com.surzhykov.navsymphony.graph.builder.GraphBuilder
import com.surzhykov.navsymphony.graph.builder.MenuNodeBuilder
import com.surzhykov.navsymphony.graph.builder.ScreenNodeBuilder
import com.surzhykov.navsymphony.graph.core.ViewModelBuilder
import com.surzhykov.navsymphony.graph.core.graph.NavigationGraph
import com.surzhykov.navsymphony.graph.core.graph.NavigationGraphDsl
import com.surzhykov.navsymphony.graph.core.node.NodeAppearance
import com.surzhykov.navsymphony.screen.core.AbstractViewModel
import com.surzhykov.navsymphony.screen.core.Screen
import com.surzhykov.navsymphony.screen.core.ScreenIntent
import com.surzhykov.navsymphony.screen.core.ScreenRoute
import com.surzhykov.navsymphony.screen.core.ScreenState
import com.surzhykov.navsympnony.menu.MenuScreen
import com.surzhykov.navsympnony.menu.MenuScreenViewModel
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

/**
 * Defines a navigation graph using a builder pattern.
 *
 * @param viewModelBuilder The builder responsible for creating ViewModels.
 * @param init The initializer block for configuring the graph.
 * */
@NavigationGraphDsl
fun graph(
    viewModelBuilder: ViewModelBuilder,
    initialRoute: KClass<out ScreenRoute>,
    init: GraphBuilder.() -> Unit,
): NavigationGraph {
    val builder = GraphBuilder(viewModelBuilder, initialRoute).apply(init)
    return builder.build()
}

/**
 * Defines a screen node within a navigation graph.
 *
 * @param screen The screen to be added to the navigation graph.
 * @param appearance The visual appearance of the screen node.
 * @param init A lambda function for configuring the screen node.
 * @param R The type of ScreenRoute
 * @param VM The type of AbstractViewModel
 * */
@NavigationGraphDsl
inline fun <reified R, S, I, reified VM> GraphBuilder.screen(
    screen: KClass<out Screen<R, S, I, VM>>,
    appearance: NodeAppearance,
    noinline init: ScreenNodeBuilder<R, S, I, VM>.() -> Unit = {},
): GraphBuilder where R : ScreenRoute, S : ScreenState, I : ScreenIntent, VM : AbstractViewModel<S, I> {
    return screen(
        screen,
        appearance,
        serializer(),
        R::class,
        VM::class,
        init
    )
}

/**
 * Defines a screen node within a navigation graph using a lambda function to create the screen
 * instance.
 *
 * This function allows you to define a screen node within the navigation graph by providing a
 * lambda that will be invoked to create the screen instance. This is useful when you need to
 * perform custom initialization logic during the screen's creation.
 *
 * @param appearance The visual appearance of the screen node.
 * @param screen A lambda function that returns an instance of the [Screen] to be added to the
 * navigation graph.
 * @param init A lambda function for configuring the screen node.
 * @param R The type of [ScreenRoute] for this screen.
 * @param VM The type of [AbstractViewModel] for this screen.
 */
@NavigationGraphDsl
inline fun <reified R, S, I, reified VM> GraphBuilder.screen(
    appearance: NodeAppearance,
    noinline screen: () -> Screen<R, S, I, VM>,
    noinline init: ScreenNodeBuilder<R, S, I, VM>.() -> Unit = {},
): GraphBuilder where R : ScreenRoute, S : ScreenState, I : ScreenIntent, VM : AbstractViewModel<S, I> {
    return screen(
        screen,
        appearance,
        serializer(),
        R::class,
        VM::class,
        init
    )
}

/**
 * Defines a menu node within a navigation graph.
 *
 * @param screen The menu screen to be added to the navigation graph.
 * @param appearance The visual appearance of the menu node.
 * @param route A lambda function providing the route for the menu.
 * @param init A lambda function for configuring the menu node.
 * @param R The type of ScreenRoute.Menu
 * @param VM The type of MenuScreenViewModel
 * */
@NavigationGraphDsl
inline fun <reified R, reified VM> GraphBuilder.menu(
    noinline screen: () -> MenuScreen<R, VM>,
    appearance: NodeAppearance,
    noinline route: () -> R,
    noinline init: MenuNodeBuilder<R, VM>.() -> Unit,
): GraphBuilder where R : ScreenRoute.Menu, VM : MenuScreenViewModel {
    return menu(
        screen,
        appearance,
        serializer(),
        R::class,
        VM::class,
        route,
        init
    )
}