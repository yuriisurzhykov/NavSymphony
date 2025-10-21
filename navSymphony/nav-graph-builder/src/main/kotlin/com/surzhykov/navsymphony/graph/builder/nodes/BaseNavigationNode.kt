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
import com.surzhykov.navsymphony.screen.core.AbstractViewModel
import com.surzhykov.navsymphony.screen.core.DelicateScreenApi
import com.surzhykov.navsymphony.screen.core.ScreenIntent
import com.surzhykov.navsymphony.screen.core.ScreenRoute
import com.surzhykov.navsymphony.screen.core.ScreenState
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * Implementation of [NavigationNode] that represents a regular screen within an application. The
 * class holds all necessary references for components that required to instantiate a node as a
 * screen in Jetpack Compose navigation framework.
 * */
@Immutable
internal open class BaseNavigationNode<R : ScreenRoute, S : ScreenState, I : ScreenIntent, VM : AbstractViewModel<S, I>>(
    private val routeKClass: KClass<R>,
    private val viewModelKClass: KClass<VM>,
    private val serializer: KSerializer<R>,
    private val viewModelBuilder: ViewModelBuilder,
    override val screenInitializer: LazyScreenInitializer<R, S, I, VM>,
    override val parametersMap: Map<KType, NavType<*>>,
    override val nodeMetaData: NodeMetaData,
    override val nodeAppearanceInfo: NodeAppearance,
) : NavigationNode {

    @Stable
    @OptIn(DelicateScreenApi::class)
    override fun composeNode(navGraphBuilder: NavGraphBuilder) {
        navGraphBuilder.composable(
            screenInitializer.value,
            routeKClass,
            parametersMap
        ) { navBackStack ->
            screenInitializer.value.Pane(
                modifier = Modifier.fillMaxSize(),
                route = navBackStack.toRoute(serializer),
                viewModel = viewModelBuilder.viewModel(viewModelKClass),
            )
            DisposableEffect(Unit) {
                onDispose { screenInitializer.clear() }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BaseNavigationNode<*, *, *, *>) return false

        if (screenInitializer != other.screenInitializer) return false
        if (routeKClass != other.routeKClass) return false
        if (viewModelKClass != other.viewModelKClass) return false
        if (serializer != other.serializer) return false
        if (parametersMap != other.parametersMap) return false
        if (viewModelBuilder != other.viewModelBuilder) return false
        if (nodeMetaData != other.nodeMetaData) return false
        if (nodeAppearanceInfo != other.nodeAppearanceInfo) return false

        return true
    }

    override fun hashCode(): Int {
        var result = screenInitializer.hashCode()
        result = 31 * result + routeKClass.hashCode()
        result = 31 * result + viewModelKClass.hashCode()
        result = 31 * result + serializer.hashCode()
        result = 31 * result + parametersMap.hashCode()
        result = 31 * result + viewModelBuilder.hashCode()
        result = 31 * result + nodeMetaData.hashCode()
        result = 31 * result + nodeAppearanceInfo.hashCode()
        return result
    }

    override fun toString(): String {
        return "BaseNavigationNode(" +
                "routeKClass=$routeKClass, " +
                "viewModelKClass=$viewModelKClass, " +
                "serializer=$serializer, " +
                "viewModelBuilder=$viewModelBuilder, " +
                "screenInitializer=$screenInitializer, " +
                "parametersMap=$parametersMap, " +
                "nodeMetaData=$nodeMetaData, " +
                "nodeAppearanceInfo=$nodeAppearanceInfo" +
                ")"
    }
}