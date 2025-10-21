package com.surzhykov.navsymphony.choreographer.data

import com.surzhykov.navsymphony.choreographer.common.NavigationOptions
import com.surzhykov.navsymphony.choreographer.common.navigationLogger
import com.surzhykov.navsymphony.domain.Logger
import com.surzhykov.navsymphony.graph.core.graph.NavigationGraph
import com.surzhykov.navsymphony.graph.core.node.NavigationNode
import com.surzhykov.navsymphony.screen.core.ScreenRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.reflect.KClass

/**
 * Interface responsible for managing the state of the navigation stack.
 *
 * This interface provides methods for manipulating the navigation back stack,
 * such as appending new nodes, popping nodes, popping until a specific route,
 * and clearing the entire stack. It also exposes flows to observe the current
 * navigation node and the back stack count.
 */
interface NavigationStateHandler {

    /**
     * A [Flow] that emits the current number of entries in the back stack.
     *
     * This value represents how many navigation destinations are currently stored in the back
     * stack. It effectively reflects the depth of the navigation history.
     *
     * For example:
     * - When the app starts and no navigation has occurred, the `backStackCount` will be 0.
     * - After navigating to one destination, the `backStackCount` will be 1.
     * - After navigating to another destination on top of the previous one, the `backStackCount`
     * will be 2.
     * - When popping the back stack (navigating back), the `backStackCount` decreases accordingly.
     *
     * This can be useful for:
     * - Implementing custom back navigation behavior.
     * - Displaying UI elements based on the navigation depth (e.g., showing/hiding a back button).
     * - Analytics or logging to understand user navigation patterns.
     */
    val backStackCount: StateFlow<Int>

    /**
     * A [StateFlow] that emits the currently active [NavigationNode] in the navigation hierarchy.
     *
     * This flow provides a reactive stream of the current node being displayed or interacted with.
     * It allows observers to react to changes in the navigation state, such as when the user
     * navigates to a new screen or section of the application.
     *
     * Emitted values will always represent a valid [NavigationNode] within the current navigation
     * structure.
     *
     * This property is typically used for:
     * - Updating AppComponent intent actor to properly handle timeout intents.
     *
     * **Note**: It's important to handle potential edge cases, such as initial emission when the flow
     * is first collected.
     */
    val currentNodeFlow: StateFlow<NavigationNode>

    /**
     * This method appends a new [NavigationNode] to the back stack.
     * @return `true` if the navigationNode was successfully appended, `false` if it already existed in
     * the stack or flow has not been able to emit a new navigationNode to a flow.
     * */
    fun append(navigationNode: NavigationNode, keepInStack: Boolean): Boolean

    /**
     * Pops all back stack entries until a route matching the provided [targetRoute] is found.
     *
     * @param targetRoute The class of the route to pop until.
     * @return `true` if a matching route was found and popped, `false` otherwise.
     * */
    fun popUntil(targetRoute: KClass<out ScreenRoute>): Boolean

    /**
     * Pops the last entry from the back stack.
     *
     * @return The navigation node appeared after the pop operation if the stack is not empty,
     * or the initial navigation node if the only one is left in the stack.
     * */
    fun pop(): NavigationNode

    /**
     * Clears all entries from the back stack.
     * */
    fun clear(): Boolean

    class Base @Inject constructor(
        private val graph: NavigationGraph,
        coroutineScope: CoroutineScope,
        private val nodesStack: NavigationBackStack<NavigationNode>,
        private val logger: Logger = navigationLogger(),
    ) : NavigationStateHandler {

        private val mutableNavigationState: MutableStateFlow<NavigationNode>

        override val currentNodeFlow: StateFlow<NavigationNode>
        override val backStackCount: StateFlow<Int>

        init {
            val rootNode = graph[graph.rootClass]
            check(rootNode != null) {
                "Root node not found in navigation graph!" +
                        " Please, verify that you set up navigation graph properly!"
            }
            mutableNavigationState = MutableStateFlow(rootNode)
            nodesStack.add(rootNode, NavigationOptions())
            currentNodeFlow = mutableNavigationState
                .onEach {
                    logger.d(
                        TAG,
                        "Current navigation node: ${it.nodeMetaData.routeKClass.simpleName}"
                    )
                }
                .stateIn(coroutineScope, SharingStarted.Eagerly, rootNode)
            backStackCount = mutableNavigationState
                .map { nodesStack.size }
                .stateIn(coroutineScope, SharingStarted.Eagerly, 0)
        }

        override fun append(navigationNode: NavigationNode, keepInStack: Boolean): Boolean {
            nodesStack.add(navigationNode, NavigationOptions(addToBackStack = keepInStack))
            return mutableNavigationState.tryEmit(navigationNode)
        }

        override fun popUntil(targetRoute: KClass<out ScreenRoute>): Boolean {
            try {
                val targetNode = nodesStack.popUntil { it.nodeMetaData.routeKClass == targetRoute }
                return mutableNavigationState.tryEmit(targetNode)
            } catch (e: NoSuchElementException) {
                return append(graph.rootNode, true)
            }
        }

        override fun pop(): NavigationNode {
            val targetNode = nodesStack.pop()
            mutableNavigationState.tryEmit(targetNode)
            return nodesStack.last()
        }

        override fun clear(): Boolean {
            nodesStack.clear()
            return append(graph.rootNode, true)
        }

        companion object {
            private val TAG = NavigationStateHandler::class.java.simpleName
        }
    }
}