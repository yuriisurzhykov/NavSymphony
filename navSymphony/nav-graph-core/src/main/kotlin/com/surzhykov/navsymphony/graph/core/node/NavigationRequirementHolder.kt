package com.surzhykov.navsymphony.graph.core.node

import kotlin.reflect.KClass

/**
 *  A holder for [NavigationRequirement] instances, keyed by their class.
 *
 *  This value class provides a type-safe way to store and retrieve [NavigationRequirement]s
 *  associated with specific navigation operations.
 *
 *  @property requirements A map where the keys are [KClass] instances representing the types of
 *  [NavigationRequirement] and the values are the corresponding [NavigationRequirement] instances.
 */
@JvmInline
value class NavigationRequirementHolder(
    val requirements: Map<KClass<out NavigationRequirement>, NavigationRequirement>
) {

    /**
     * Retrieves a [NavigationRequirement] of the specified type.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : NavigationRequirement> getRequirement(clazz: KClass<T>): T? {
        return requirements[clazz] as? T
    }
}