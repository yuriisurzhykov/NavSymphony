package com.surzhykov.navsympnony.menu.domain.chain

import com.surzhykov.navsymphony.graph.core.node.NavigationNode

/**
 * Represents the decision made by a rule when evaluating a [NavigationNode].
 *
 * A rule can either decide to include a node or exclude it based on certain criteria.
 */
sealed interface RuleDecision {

    /**
     * Represents a decision to include an element or item based on a rule.
     */
    data object Include : RuleDecision

    /**
     * Represents a decision to exclude a specific navigation node from being considered during
     * route generation.
     *
     * This class encapsulates the node that should be excluded and the reason for its exclusion.
     * It's part of the routing rule system, allowing for fine-grained control over which nodes are
     * included in the generated routes.
     *
     * @property node The navigation node that should be excluded.
     * @property reason A string describing the reason why this node is being excluded. This can be
     * used for debugging or logging purposes, to understand why a particular node was not included
     * in a generated route.
     */
    data class Exclude(
        val node: NavigationNode,
        val reason: String,
    ) : RuleDecision
}