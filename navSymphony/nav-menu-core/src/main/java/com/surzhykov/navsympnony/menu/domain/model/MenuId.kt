package com.surzhykov.navsympnony.menu.domain.model

import com.surzhykov.navsymphony.screen.core.ScreenRoute
import kotlin.reflect.KClass

/**
 * Represents a unique identifier for a menu item in the application.
 *
 * This class uses a value class (`@JvmInline`) to provide compile-time safety and efficiency by
 * wrapping an integer value. It ensures that menu identifiers are treated as distinct entities,
 * preventing accidental mixing with plain integers.
 *
 * Each [MenuId] is associated with either a manually assigned ID (integer) or an auto generated
 * ID based on the hashcode of a [ScreenRoute].
 *
 * @property id The underlying integer value representing the menu ID.
 *
 * @constructor Creates a [MenuId] derived from the [hashCode] of a [KClass] representing a
 * [ScreenRoute]. This is useful for associating menu items with specific screens in the
 * application. Using the [KClass] ensures that the generated ID is consistent across runs and
 * is unique within the application for each [ScreenRoute]. If you have multiple instances of
 * the same screen (i.e., open the same screen twice) the id will be the same
 *
 * @see ScreenRoute
 */
@JvmInline
value class MenuId internal constructor(val id: Int) {
    constructor(kClass: KClass<out ScreenRoute>) : this(kClass.hashCode())
}