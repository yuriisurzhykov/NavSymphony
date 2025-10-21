package com.surzhykov.navsympnony.menu.domain.model

import androidx.compose.runtime.Immutable
import com.surzhykov.navsymphony.core.presentation.DrawableResolver
import com.surzhykov.navsymphony.core.presentation.StringResolver
import com.surzhykov.navsymphony.screen.core.ScreenRoute
import kotlin.reflect.KClass

/**
 * Represents a single item in a menu screen. MenuItem generally is built from `NavigationNode`
 * and its appearance.
 *
 * @property menuId A unique identifier for this menu item.
 * @property routeClass The Kotlin class representing the route associated with this menu item. This
 * is used for navigation and identifying the destination screen. It should extend [ScreenRoute].
 * @property title A resolver that provides the title text for the menu item. This allows for
 * dynamic or localized text.
 * @property icon A resolver that provides the icon to be displayed for the menu item. This allows
 * for dynamic or themed icons.
 * @property enabled Indicates whether this menu item is currently enabled. If `false`, it will
 * typically be visually disabled and not interactive.
 * @property severity The severity level of the menu item's condition. This can be used to visually
 * emphasize or prioritize certain menu items, e.g., using different colors or icons.
 * See [MenuConditionSeverity].
 * @property routeBuilder A lambda function that builds and returns the [ScreenRoute] instance
 * associated with this menu item. This is called when the user navigates to this menu item's
 * destination.
 *
 * @see MenuId
 * @see ScreenRoute
 * @see StringResolver
 * @see DrawableResolver
 * @see MenuConditionSeverity
 */
@Immutable
data class MenuItem(
    val menuId: MenuId,
    val routeClass: KClass<out ScreenRoute>,
    val title: StringResolver,
    val icon: DrawableResolver,
    val enabled: Boolean,
    val severity: MenuConditionSeverity,
    val routeBuilder: (() -> ScreenRoute),
)