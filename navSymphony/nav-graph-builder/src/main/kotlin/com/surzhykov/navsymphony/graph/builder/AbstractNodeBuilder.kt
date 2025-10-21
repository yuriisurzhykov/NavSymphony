package com.surzhykov.navsymphony.graph.builder

import androidx.navigation.NavType
import com.surzhykov.navsymphony.graph.core.graph.NavigationGraphDsl
import com.surzhykov.navsymphony.graph.core.node.NavigationRequirement
import kotlin.reflect.KType
import kotlin.time.Duration

/**
 * Base class for builders that construct navigation nodes within a navigation graph.
 *
 * This abstract class provides common functionality for defining requirements and argument type
 * mappings for navigation nodes. Subclasses, like [ScreenNodeBuilder], extend this class to define
 * the specifics of a particular node type.
 *
 * Key Features:
 * - **Navigation Requirements:** Supports the definition of [NavigationRequirement]s, which are
 * conditions that must be met before a navigation node can be accessed.
 * - **Argument Type Mapping:** Allows the association of Kotlin types (`KType`) with corresponding
 * [NavType]s for handling navigation arguments.
 *
 * Example:
 * ```kotlin
 * @NavigationGraphDsl
 * class MyScreenNodeBuilder : AbstractNodeBuilder() {
 *   // ... specific node building logic ...
 * }
 *
 * fun NavGraphBuilder.myGraph() {
 *   screen<MyScreenNodeBuilder>("myScreen") {
 *     require(LoggedInRequirement) // Example requirement
 *     addNavRouteType(typeOf<Int>() to NavType.IntType) // Example argument type mapping
 *     // ... more configurations ...
 *   }
 * }
 * ```
 *
 * @see NavigationRequirement
 * @see NavType
 * @see NavigationGraphDsl
 * @see ScreenNodeBuilder
 */
@NavigationGraphDsl
abstract class AbstractNodeBuilder(
    protected var screenTimeoutDuration: Duration,
    protected val requirements: MutableMap<NavigationRequirement, NavigationRequirement>,
) {

    protected val parametersTypeMap = mutableMapOf<KType, NavType<*>>()

    fun screenTimeout(timeout: Duration) = apply {
        screenTimeoutDuration = timeout
    }

    /**
     * Adds a [NavigationRequirement] to the list of requirements that must be met for this
     * [NavigationNode] to be displayed.
     *
     * [NavigationRequirement]s are used to define conditions that must be satisfied before a screen
     * is allowed to be navigated to. For example, you might have a requirement that the user must
     * be logged in or have a specific permission before they can access a particular screen.
     *
     * This function allows you to add one requirement at a time to the builder. You can chain
     * multiple calls to this function to add multiple requirements.
     *
     * @param requirement The [NavigationRequirement] to add to the list of requirements.
     * @return This [ScreenNodeBuilder] instance, allowing for method chaining.
     */
    fun require(requirement: NavigationRequirement) = apply {
        requirements[requirement] = requirement
    }

    /**
     * Adds a navigation route type to the internal map.
     *
     * This function allows associating a Kotlin type (`KType`) with a corresponding `NavType`.
     * This mapping is crucial for properly handling arguments passed during navigation. It
     * essentially registers how a specific Kotlin type should be interpreted and serialized or
     * deserialized when used as a navigation parameter.
     *
     * Example usage:
     * ```
     * // Assuming 'parametersTypeMap' is a MutableMap<KType, NavType<*>>
     * val builder = MyNavigationBuilder()
     * builder
     *     .addNavRouteType(typeOf<Int>() to NavType.IntType)
     *     .addNavRouteType(typeOf<String>() to NavType.StringType)
     *     .addNavRouteType(typeOf<Boolean>() to NavType.BoolType)
     *     // ... other configurations ...
     * ```
     *
     * @param typePair A [Pair] where:
     * - The first element ([Pair.first]) is the [KType] representing the Kotlin type of the
     * navigation parameter.
     * - The second element ([Pair.second]) is the [NavType] that specifies how this type should
     * be handled by the navigation component.
     * @return The current object instance (using `apply`) to enable method chaining.
     *
     * @see NavType
     * @see KType
     * */
    fun addNavRouteType(typePair: Pair<KType, NavType<*>>) = apply {
        parametersTypeMap[typePair.first] = typePair.second
    }
}