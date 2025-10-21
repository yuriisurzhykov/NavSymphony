package com.surzhykov.navsympnony.menu

import androidx.compose.runtime.Immutable
import com.surzhykov.navsymphony.core.presentation.DrawableResolver
import com.surzhykov.navsymphony.core.presentation.StringResolver
import com.surzhykov.navsymphony.screen.core.ScreenState
import com.surzhykov.navsympnony.menu.domain.model.MenuItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Represents the state of a menu screen.
 *
 * This class holds the necessary data to render a screen containing a list of menu items.
 * It includes properties for the screen's title, icon, and the menu items themselves.
 *
 * @property screenTitle The title of the menu screen. Defaults to an empty string.
 * @property screenIcon The icon to display on the menu screen. Defaults to an empty drawable.
 * @property menuItems An immutable list of [MenuItem] representing the items in the menu. Defaults
 * to an empty list.
 */
@Immutable
open class MenuScreenState(
    val screenTitle: StringResolver = StringResolver.BaseString(""),
    val screenIcon: DrawableResolver = DrawableResolver.Empty(),
    open val menuItems: ImmutableList<MenuItem> = persistentListOf(),
) : ScreenState {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MenuScreenState) return false

        if (screenTitle != other.screenTitle) return false
        if (screenIcon != other.screenIcon) return false
        if (menuItems != other.menuItems) return false

        return true
    }

    override fun hashCode(): Int {
        var result = screenTitle.hashCode()
        result = 31 * result + screenIcon.hashCode()
        result = 31 * result + menuItems.hashCode()
        return result
    }

    override fun toString(): String {
        return "MenuScreenState(screenTitle=$screenTitle, screenIcon=$screenIcon, menuItems=$menuItems)"
    }
}