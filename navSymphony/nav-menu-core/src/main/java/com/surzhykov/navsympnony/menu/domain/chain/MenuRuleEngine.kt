package com.surzhykov.navsympnony.menu.domain.chain

import com.surzhykov.navsymphony.domain.Logger
import com.surzhykov.navsymphony.graph.core.graph.NavigationGraph
import com.surzhykov.navsymphony.graph.core.node.NavigationNode
import com.surzhykov.navsymphony.screen.core.ScreenRoute
import com.surzhykov.navsympnony.menu.domain.model.MenuItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine

/**
 * `MenuRuleEngine` is an interface responsible for dynamically generating a list of `MenuItem`s
 * based on a given navigation graph and a set of conditions.
 *
 * It provides a mechanism to include or exclude menu items based on runtime evaluations of defined
 * rules.
 */
interface MenuRuleEngine {

    /**
     * Configures the menu engine based on the provided screen route and navigation graph.
     *
     * This function is responsible for setting up the necessary configurations and dependencies
     * for a specific screen within the application, taking into account the overall navigation
     * structure. The specific actions performed by this function will depend on the `screenRoute`
     * and the design of the `NavigationGraph`.
     *
     * @param screenRoute The route representing the screen to be configured.  This should be a
     * value from the `ScreenRoute.Menu` sealed class, defining which screen is currently being
     * set up.
     * @param navigationGraph The navigation graph that manages the flow between screens. This
     * object might be used to register the screen's route, define transitions, or access other
     * navigation-related information.
     *
     * Example Usage:
     * ```kotlin
     * val myNavigationGraph = NavigationGraph()
     * configureEngine(ScreenRoute.Menu.Home, myNavigationGraph)
     * ```
     */
    suspend fun configureEngine(screenRoute: ScreenRoute.Menu, navigationGraph: NavigationGraph)

    /**
     * Emits a flow of lists of [MenuItem]s.
     *
     * This function provides a reactive stream of lists, where each list represents a snapshot
     * of the available menu items at a particular point in time. The flow can emit multiple lists
     * over time as the underlying menu data changes.
     *
     * Use cases:
     * - Displaying a dynamic menu that can update in real-time.
     * - Observing changes to the available menu items and reacting accordingly.
     * - Maintaining a list of menu options that can be updated asynchronously.
     *
     * Note:
     * - The specific implementation details of how the menu items are retrieved and when new lists
     * are emitted are not defined here and would be determined by the concrete implementation of
     * this function.
     * - It is expected that the implementor will handle error cases. For example by emitting an
     * empty list or a list containing an error state if any error occurred.
     *
     * @return A Flow that emits lists of [MenuItem] objects. Each emitted list
     * represents the current set of available menu items.
     */
    fun menuItemsFlow(): Flow<List<MenuItem>>

    open class Base(
        conditionUnitList: Set<MenuConditionUnit>,
        private val menuBuilderFactory: MenuItemBuilderFactory = MenuItemBuilderFactory.Base(),
        private val logger: Logger = Logger.AndroidLogger,
    ) : MenuRuleEngine {

        // The flow of the current screen route to be used in the menu rule evaluation
        private val screenRouteState = MutableSharedFlow<ScreenRoute.Menu>(replay = 1)

        // The flow of navigation graph to be used in the menu rule evaluation
        private val navigationGraphFlow = MutableSharedFlow<NavigationGraph>(replay = 1)

        // The flow that combines a list of conditions and their rules into a composite rule, that
        // executes evaluation based on priorities of rule.
        private val listOfConditionFlows = conditionUnitList.map {
            val ruleFlow = it.ruleFlow()
            logger.d(TAG, "Creating condition flow for: $it, $ruleFlow")
            ruleFlow
        }
        private val conditionRulesFlow = combine(listOfConditionFlows) { menuRules ->
            logger.d(TAG, "Combining menu rules")
            MenuBuilderRule.Composite(menuRules.asSequence())
        }

        // The main menu items flow
        private val menuItemsFlow = combine(
            conditionRulesFlow,
            screenRouteState,
            navigationGraphFlow
        ) { menuRules, screenRoute, navigationGraph ->
            // Try to retrieve children of menu node from the navigation graph
            val parentMenuNode = navigationGraph.getMenu(screenRoute::class)
            val subNodes = parentMenuNode.getOrThrow().children
            // Build menu items for each node
            val builtMenuItems = subNodes
                .filterIsInstance<NavigationNode.AutoNavigableNode>()
                .mapNotNull { menuNode ->
                    val nodeBuilder = menuBuilderFactory.create(menuNode)
                    // Evaluate the rule decision for each node
                    val ruleDecision = menuRules.evaluate(menuNode, nodeBuilder)
                    val routeClass = menuNode.nodeMetaData.routeKClass
                    if (ruleDecision is RuleDecision.Exclude) {
                        // If the rule decision is to exclude the node, log the reason and return null
                        // for the menu item.
                        val message =
                            "Node ${routeClass.simpleName} excluded. Reason: ${ruleDecision.reason}"
                        logger.w(TAG, message)
                        return@mapNotNull null
                    } else {
                        // Otherwise, log the inclusion and return the built menu item.
                        logger.i(TAG, "Node ${routeClass.simpleName} included")
                        val builderResult = nodeBuilder.build()
                        // Since the node builder may fail to build the menu item, we need to check
                        // the result of the build operation.
                        if (builderResult.isSuccess) {
                            // Return the built menu item if the build operation was successful.
                            return@mapNotNull builderResult.getOrNull()
                        } else {
                            // If the build operation failed, log the exception and return null.
                            logger.e(
                                TAG,
                                "Failed to build menu item for node ${routeClass.simpleName}",
                                builderResult.exceptionOrNull()!!
                            )
                            return@mapNotNull null
                        }
                    }
                }
            return@combine builtMenuItems
        }

        override suspend fun configureEngine(
            screenRoute: ScreenRoute.Menu,
            navigationGraph: NavigationGraph,
        ) {
            screenRouteState.emit(screenRoute)
            navigationGraphFlow.emit(navigationGraph)
        }

        override fun menuItemsFlow(): Flow<List<MenuItem>> = menuItemsFlow

        companion object {
            private val TAG = MenuRuleEngine::class.simpleName.orEmpty()
        }
    }
}