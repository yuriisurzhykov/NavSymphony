package com.surzhykov.navsymphony.graph.builder.nodes

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import com.surzhykov.navsymphony.graph.core.ViewModelBuilder
import com.surzhykov.navsymphony.graph.core.composable
import com.surzhykov.navsymphony.graph.core.node.LazyScreenInitializer
import com.surzhykov.navsymphony.graph.core.node.NavigationNode
import com.surzhykov.navsymphony.graph.core.node.NodeAppearance
import com.surzhykov.navsymphony.graph.core.node.NodeMetaData
import com.surzhykov.navsymphony.graph.core.toRoute
import com.surzhykov.navsymphony.screen.core.DelicateScreenApi
import com.surzhykov.navsymphony.screen.core.ScreenRoute
import com.surzhykov.navsympnony.menu.MenuScreenIntent
import com.surzhykov.navsympnony.menu.MenuScreenState
import com.surzhykov.navsympnony.menu.MenuScreenViewModel
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * Represents a node in a navigation graph specifically designed for menu screens.
 *
 * This class encapsulates the necessary information to build a composable destination within a
 * `NavGraphBuilder` for a menu screen. It handles the association between a [MenuScreen], its
 * route, its view model, and the serialization of the route arguments. It also manages child nodes
 * and appearance information.
 *
 * @property screenInitializer The [MenuScreen] instance that represents the UI content of this node.
 * @property routeKClass The KClass of the route type [R]. Used for route identification and type
 * safety.
 * @property viewModelKClass The KClass of the view model type [VM]. Used for view model creation
 * and type safety.
 * @property serializer The [KSerializer] instance used to serialize and deserialize the route
 * parameters.
 * @property parametersMap A map of parameter types ([KType]) to their corresponding [NavType]s.
 * This defines how route parameters are passed and parsed.
 * @property viewModelBuilder The [ViewModelBuilder] responsible for creating the view model
 * instance.
 * @property nodeMetaData Metadata associated with this navigation node, providing additional
 * information about it.
 * @property nodeAppearanceInfo Information about the appearance of this node, like its title.
 * @property children A set of child [NavigationNode.AutoNavigableNode]s that can be navigated
 * to from this node.
 * @property routeBuilder A lambda function that returns the [ScreenRoute] instance associated
 * with this node.
 */
@Immutable
internal data class MenuNavigationNode<R, VM>(
    private val routeKClass: KClass<R>,
    private val viewModelKClass: KClass<VM>,
    private val serializer: KSerializer<R>,
    private val viewModelBuilder: ViewModelBuilder,
    override val screenInitializer: LazyScreenInitializer<R, MenuScreenState, MenuScreenIntent, VM>,
    override val parametersMap: Map<KType, NavType<*>>,
    override val nodeMetaData: NodeMetaData,
    override val nodeAppearanceInfo: NodeAppearance,
    override val children: LinkedHashSet<NavigationNode>,
    override val routeBuilder: () -> ScreenRoute,
) : NavigationNode.MenuNode where R : ScreenRoute.Menu, VM : MenuScreenViewModel {

    @Stable
    @OptIn(DelicateScreenApi::class)
    override fun composeNode(navGraphBuilder: NavGraphBuilder) {
        navGraphBuilder.composable(
            screen = screenInitializer.value,
            routeClass = routeKClass,
            parametersMap = parametersMap
        ) { navBackStack ->
            screenInitializer.value.Pane(
                Modifier.fillMaxSize(),
                viewModelBuilder.viewModel(viewModelKClass),
                navBackStack.toRoute(serializer)
            )
            DisposableEffect(Unit) {
                onDispose { screenInitializer.clear() }
            }
        }
    }
}