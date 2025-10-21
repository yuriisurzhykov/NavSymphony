package com.surzhykov.navsymphony.graph.builder.dsl

import com.surzhykov.navsymphony.graph.builder.AutoNavigableNodeBuilder
import com.surzhykov.navsymphony.graph.builder.MenuItemsBuilder
import com.surzhykov.navsymphony.graph.builder.MenuNodeBuilder
import com.surzhykov.navsymphony.graph.builder.ScreenNodeBuilder
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
 * Defines a menu item within a menu node.
 *
 * This function simplifies the creation of a menu item by providing a DSL-like interface.
 * It automatically handles the serialization of the route and the retrieval of class references for
 * both the route and the view model. It should be used to add a new [MenuScreen] to the navigation.
 *
 * @param screen The [MenuScreen] to be added as a menu item.
 * @param appearance The visual appearance of the menu item.
 * @param route A lambda providing the route for the menu item.
 * @param init A lambda for configuring the menu item.
 */
@NavigationGraphDsl
inline fun <reified R, reified VM> MenuItemsBuilder.menu(
    noinline screen: () -> MenuScreen<R, VM>,
    appearance: NodeAppearance,
    noinline route: () -> R,
    noinline init: MenuNodeBuilder<R, VM>.() -> Unit,
): MenuItemsBuilder where R : ScreenRoute.Menu, VM : MenuScreenViewModel {
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

/**
 * Defines a screen within the navigation graph.
 *
 * This function simplifies the process of defining a screen by providing a DSL-like approach.
 * It automatically manages the serialization of the route and fetches class references for
 * the route and view model.
 * It allows additional customization of the screen's properties using the [init] lambda. It should
 * be used to add a new [Screen] to the navigation.
 *
 * @param screen The [Screen] to be added to the navigation graph.
 * @param appearance The visual appearance of the screen.
 * @param route A lambda providing the route for the screen.
 * @param init A lambda for configuring the screen.
 */
@NavigationGraphDsl
inline fun <reified R, S, I, reified VM> MenuItemsBuilder.screen(
    screen: KClass<out Screen<R, S, I, VM>>,
    appearance: NodeAppearance,
    noinline route: () -> R,
    noinline init: AutoNavigableNodeBuilder<R, S, I, VM>.() -> Unit = {},
): MenuItemsBuilder where R : ScreenRoute, S : ScreenState, I : ScreenIntent, VM : AbstractViewModel<S, I> {
    return screen(
        screen,
        appearance,
        serializer(),
        R::class,
        VM::class,
        route,
        init
    )
}

@NavigationGraphDsl
inline fun <reified R, S, I, reified VM> MenuItemsBuilder.nonMenuScreen(
    screen: KClass<out Screen<R, S, I, VM>>,
    appearance: NodeAppearance,
): MenuItemsBuilder where R : ScreenRoute, S : ScreenState, I : ScreenIntent, VM : AbstractViewModel<S, I> {
    return nonMenuScreen(
        screen,
        appearance,
        serializer(),
        R::class,
        VM::class,
    ) {}
}

@NavigationGraphDsl
inline fun <reified R, S, I, reified VM> MenuItemsBuilder.nonMenuScreen(
    screen: KClass<out Screen<R, S, I, VM>>,
    appearance: NodeAppearance,
    noinline init: ScreenNodeBuilder<R, S, I, VM>.() -> Unit,
): MenuItemsBuilder where R : ScreenRoute, S : ScreenState, I : ScreenIntent, VM : AbstractViewModel<S, I> {
    return nonMenuScreen(
        screen,
        appearance,
        serializer(),
        R::class,
        VM::class,
        init,
    )
}