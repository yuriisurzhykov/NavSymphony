package com.surzhykov.navsympnony.menu.domain.chain

import kotlinx.coroutines.flow.Flow

/**
 * `MenuConditionUnit` represents a unit that provides a stream of `MenuBuilderRule` based on
 * certain conditions.
 *
 * This interface is intended to be implemented by classes that manage the dynamic generation of
 * menu rules. These rules can be used to control various aspects of a menu, such as visibility,
 * enabled state, and more, based on the current state or context of the application.
 *
 * Each implementation of `MenuConditionUnit` is responsible for defining the logic that determines
 * when and how `MenuBuilderRule` instances are emitted.
 */
interface MenuConditionUnit {

    /**
     * Returns a [Flow] of [MenuBuilderRule] objects.
     *
     * This function provides a stream of rules that can be used to build or modify a menu. Each
     * emitted [MenuBuilderRule] represents a single rule that should be applied in the order it
     * is received.
     *
     * The flow may emit rules based on various factors, such as application state, middleware state
     * or data updates.
     *
     * This flow is typically collected to dynamically update or configure a menu based on the
     * stream of rules.
     *
     * @return A [Flow] that emits [MenuBuilderRule] objects.
     */
    fun ruleFlow(): Flow<MenuBuilderRule>
}