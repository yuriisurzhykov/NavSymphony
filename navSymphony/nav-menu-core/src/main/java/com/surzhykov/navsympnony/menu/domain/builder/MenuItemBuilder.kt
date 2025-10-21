package com.surzhykov.navsympnony.menu.domain.builder

import com.surzhykov.navsymphony.core.presentation.DrawableResolver
import com.surzhykov.navsymphony.core.presentation.StringResolver
import com.surzhykov.navsymphony.graph.core.node.NavigationNode
import com.surzhykov.navsymphony.screen.core.ScreenRoute
import com.surzhykov.navsympnony.menu.domain.model.MenuConditionSeverity
import com.surzhykov.navsympnony.menu.domain.model.MenuId
import com.surzhykov.navsympnony.menu.domain.model.MenuItem
import kotlin.reflect.KClass

/**
 * Builder class for creating [MenuItem] instances.
 *
 * This class provides a fluent interface for constructing menu items, allowing you to specify
 * various properties such as ID, route, title, icon, enabled state, and severity.
 *
 * @param navigationNode The navigation node from which to derive initial properties.
 */
class MenuItemBuilder(
    navigationNode: NavigationNode.AutoNavigableNode,
) {
    /** The unique identifier of the menu item. */
    private var idProperty: MenuId = MenuId(navigationNode.nodeMetaData.routeKClass)

    /** The route associated with the menu item. */
    private var routeProperty: KClass<out ScreenRoute> = navigationNode.nodeMetaData.routeKClass

    /** The title of the menu item. */
    private var titleProperty: StringResolver = navigationNode.nodeAppearanceInfo.title

    /** The icon of the menu item. */
    private var iconProperty: DrawableResolver = navigationNode.nodeAppearanceInfo.icon

    /** Indicates whether the menu item is enabled. */
    private var enabledProperty: Boolean = true

    /** The severity level of the menu item. */
    private var severityProperty: MenuConditionSeverity = MenuConditionSeverity.Normal

    /** The route builder for the menu item. */
    private var routeBuilderProperty = navigationNode.routeBuilder

    /**
     * Sets the ID and route of the menu item.
     *
     * @param routeClass The route class to associate with the menu item.
     * @return The [MenuItemBuilder] instance for chaining.
     */
    fun id(routeClass: KClass<out ScreenRoute>) = apply {
        this.idProperty = MenuId(routeClass)
        this.routeProperty = routeClass
    }

    /**
     * Sets the title of the menu item.
     *
     * @param title The title to display for the menu item.
     * @return The [MenuItemBuilder] instance for chaining.
     */
    fun title(title: StringResolver) = apply {
        this.titleProperty = title
    }

    /**
     * Sets the icon of the menu item.
     *
     * @param icon The icon to display for the menu item.
     * @return The [MenuItemBuilder] instance for chaining.
     */
    fun icon(icon: DrawableResolver) = apply {
        this.iconProperty = icon
    }

    /**
     * Enables or disables the menu item.
     *
     * @param enabled Whether the menu item should be enabled.
     * @return The [MenuItemBuilder] instance for chaining.
     * */
    fun enabled(enabled: Boolean) = apply {
        this.enabledProperty = enabled
    }

    /**
     * Sets the severity of the menu item.
     *
     * @param menuConditionSeverity The severity level of the menu item.
     * @return The [MenuItemBuilder] instance for chaining.
     */
    fun severity(menuConditionSeverity: MenuConditionSeverity) = apply {
        this.severityProperty = menuConditionSeverity
    }

    /**
     * Builds and returns the [MenuItem].
     *
     * This method constructs the [MenuItem] with the properties that have been set using the
     * builder methods.
     *
     * @return A [Result] containing the built [MenuItem] or an error if the build process fails.
     */
    fun build(): Result<MenuItem> {
        return Result.success(
            MenuItem(
                menuId = idProperty,
                routeClass = routeProperty,
                title = titleProperty,
                icon = iconProperty,
                enabled = enabledProperty,
                severity = severityProperty,
                routeBuilder = routeBuilderProperty
            )
        )
    }
}