package com.surzhykov.navsympnony.menu

import androidx.lifecycle.viewModelScope
import com.surzhykov.navsymphony.domain.Dispatcher
import com.surzhykov.navsymphony.graph.core.graph.NavigationGraph
import com.surzhykov.navsymphony.graph.core.node.NavigationNode
import com.surzhykov.navsymphony.screen.core.AbstractViewModel
import com.surzhykov.navsympnony.menu.domain.chain.MenuRuleEngine
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn

/**
 * `MenuScreenViewModel` is an abstract base class for ViewModels that manage the state and logic
 * of a menu screen. It interacts with a `NavigationGraph` to determine the structure and details
 * of the menu and uses a `MenuRuleEngine` to retrieve the actual menu items.
 *
 * This ViewModel exposes a `StateFlow` of `MenuScreenState` which represents the current state
 * of the menu screen and processes `MenuScreenIntent`s to update the menu and trigger navigation.
 *
 * @property navigationGraph The [NavigationGraph] that defines the application's navigation structure,
 * including the nodes associated with the menu.
 * @property menuEngine The [MenuRuleEngine] responsible for retrieving and managing the list of
 * menu items based on the current navigation node.
 */
abstract class MenuScreenViewModel(
    private val navigationGraph: NavigationGraph,
    private val menuEngine: MenuRuleEngine,
    private val dispatcher: Dispatcher = Dispatcher.Base(),
) : AbstractViewModel<MenuScreenState, MenuScreenIntent>() {

    private val routeSharedFlow = MutableSharedFlow<NavigationNode?>(replay = 1)

    /**
     * [screenState] is a [StateFlow] that represents the current state of the menu screen.
     *
     * It combines the list of menu items from `menuEngine.menuItemsFlow()` and the current node
     * information from [routeSharedFlow] to construct a [MenuScreenState].
     *
     * The [MenuScreenState] contains:
     * - The title of the current screen, derived from the current node's [NodeAppearance.title].
     * - The icon of the current screen, derived from the current node's [NodeAppearance.icon].
     * - A persistent list of menu items, obtained from `menuEngine.menuItemsFlow()`.
     *
     * The flow emits a new [MenuScreenState] whenever:
     * - The list of menu items emitted by `menuEngine.menuItemsFlow()` changes.
     * - A new non-null node is emitted by [routeSharedFlow].
     *
     * It uses [distinctUntilChanged] to prevent emitting duplicate states and [stateIn] to create
     * a hot state flow that starts collecting when the first subscriber appears and stops when all
     * subscribers disappear, using [SharingStarted.WhileSubscribed] to control the lifetime of the
     * flow as state.
     * */
    override val screenState: StateFlow<MenuScreenState> = combine(
        menuEngine.menuItemsFlow(),
        routeSharedFlow.filterNotNull()
    ) { menuItems, node ->
        MenuScreenState(
            node.nodeAppearanceInfo.title,
            node.nodeAppearanceInfo.icon,
            menuItems.toPersistentList()
        )
    }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), MenuScreenState())

    /**
     * Handles incoming intents for the MenuScreen.
     *
     * This function processes different types of [MenuScreenIntent] and performs the corresponding
     * actions. Currently, it supports the [MenuScreenIntent.LoadMenu] intent, which triggers
     * navigation and updates the menu engine.
     *
     * @param intent The [MenuScreenIntent] to be processed.
     *
     * @see MenuScreenIntent
     */
    override fun onIntent(intent: MenuScreenIntent) {
        when (intent) {
            is MenuScreenIntent.LoadMenu -> configureNavigationMenu(intent)
        }
    }

    private fun configureNavigationMenu(intent: MenuScreenIntent.LoadMenu) {
        dispatcher.launchBackground(viewModelScope) {
            routeSharedFlow.emit(navigationGraph[intent.route::class])
            menuEngine.configureEngine(intent.route, navigationGraph)
        }
    }

    companion object {
        private val TAG = MenuScreenViewModel::class.simpleName.orEmpty()
    }
}