package com.surzhykov.navsympnony.menu.domain.chain

import com.surzhykov.navsymphony.graph.core.node.NavigationNode
import com.surzhykov.navsympnony.menu.domain.builder.MenuItemBuilder

/**
 * `MenuBuilderRule` defines a rule that determines whether a navigation node should be included
 * in a menu and, if so, how it should be represented. It allows for a flexible and customizable
 * way to build menus based on the properties of `NavigationNode` instances.
 *
 * Each rule is responsible for evaluating a single `NavigationNode` and making a `RuleDecision`.
 * The `priority` of a rule determines the order in which it is evaluated when multiple rules are
 * combined.
 *
 * Example usage scenarios:
 * - Excluding nodes based on certain metadata.
 * - Modifying the displayed label of a node.
 * - Grouping related nodes together in a submenu.
 * - Conditionally including nodes based on user permissions.
 */
interface MenuBuilderRule {

    /**
     * The priority of this item. Higher values indicate higher priority.
     *
     * Priorities can be used to determine the order in which items should be processed or
     * displayed. For example, an item with a priority of 5 will be considered more important
     * than an item with a priority of 1.
     *
     * The specific meaning of "priority" depends on the context in which this item is used.
     *
     *  It's recommended to use positive integer values for priority, although negative values may
     *  be allowed depending on the specific use case. The system consuming this property should
     *  define and document the valid priority range and any specific meanings attached to
     *  particular priority levels.
     */
    val priority: Int

    /**
     * Evaluates the given [NavigationNode] against the current context to determine if it should be
     * included in the navigation menu.
     *
     * This function checks the various criteria associated with the [NavigationNode], such as its
     * visibility conditions, enabled state, and any potential authorization requirements, to decide
     * whether it is applicable and should be rendered as a menu item. The result is encapsulated in
     * a [RuleDecision] which can be ACCEPTED, REJECTED, or UNDECIDED.
     *
     * @param navigationNode The [NavigationNode] representing a potential menu item to be evaluated.
     * @param menuBuilder The [MenuItemBuilder] used to construct the final menu items. It may
     * contain information relevant to the evaluation process, such as current user context or other
     * global states.
     * @return A [RuleDecision] indicating the outcome of the evaluation:
     *  - [RuleDecision.Include]: The [NavigationNode] is deemed valid and should be included in
     *  the menu.
     *  - [RuleDecision.Exclude]: The [NavigationNode] is deemed invalid and should be excluded from
     *  the menu.
     */
    fun evaluate(
        navigationNode: NavigationNode,
        menuBuilder: MenuItemBuilder,
    ): RuleDecision

    /**
     * A composite [MenuBuilderRule] that combines multiple rules and evaluates them sequentially.
     *
     * This class allows you to define a complex menu building logic by composing several simpler
     * rules. The rules are evaluated in order of their priority, and the final decision is based
     * on the combined results of these rules.
     *
     * If any of the underlying rules returns a [RuleDecision.Exclude], the composite rule will
     * also return a [RuleDecision.Exclude], with the reasons from all failed rules combined. If all
     * rules return [RuleDecision.Include], the composite rule will return [RuleDecision.Include].
     *
     * @property rules A sequence of [MenuBuilderRule] instances that will be evaluated. The rules
     * will be sorted by their [MenuBuilderRule.priority] before evaluation.
     */
    class Composite(
        private val rules: Sequence<MenuBuilderRule>,
    ) : MenuBuilderRule {

        override val priority: Int = Int.MAX_VALUE

        override fun evaluate(
            navigationNode: NavigationNode,
            menuBuilder: MenuItemBuilder,
        ): RuleDecision {
            // Evaluate the decision for each rule
            val decisions = rules
                // Sort the rules by their priority
                .sortedBy { it.priority }
                // Evaluate each rule
                .map { it.evaluate(navigationNode, menuBuilder) }
                // Convert the decisions to a list
                .toList()
            // Retrieve the failed decisions
            val failed = decisions.filterIsInstance<RuleDecision.Exclude>()
            return if (failed.isEmpty()) {
                // If no rules failed, return the decision of the first rule
                RuleDecision.Include
            } else {
                // If rules failed, combine the reasons and return an Exclude decision
                val reason = failed.joinToString("; ") { it.reason }
                RuleDecision.Exclude(navigationNode, reason)
            }
        }
    }
}