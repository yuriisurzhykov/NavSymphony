package com.surzhykov.navsymphony.choreographer.common

import java.util.TreeSet
import javax.inject.Inject

/**
 * [RedirectChainBuilder] helps to create a set of [NavigationIntent]s for navigation
 * transaction when screen requires protected area or privacy policy acknowledge, so that
 * `NavigationChoreographer` would be able to process the intents one-by-one.
 *
 * It allows adding multiple [NavigationIntent]s and then building them into a final set.
 * After building, the builder is reset and ready to be used again.
 */
class RedirectChainBuilder @Inject constructor(
    private val originalIntent: NavigationIntent
) {

    // Stores the navigation intents added to the builder.
    private val intents = HashSet<NavigationIntent>()

    /**
     * Adds a [NavigationIntent] to the current set of intents.
     *
     * @param intent The [NavigationIntent] to add.
     * @return The current [RedirectChainBuilder] instance, allowing for method chaining.
     */
    fun addIntent(intent: NavigationIntent) = apply {
        intents.add(intent)
    }

    fun addIntents(intents: Set<NavigationIntent>) = apply {
        this.intents.addAll(intents)
    }

    /**
     * Builds a set of all [NavigationIntent]s added so far.
     *
     * After building, the internal list of intents is cleared so the builder can be used again.
     *
     * @return A set containing all [NavigationIntent]s that were added.
     */
    fun build(): Transaction {
        val requiredIntents = TreeSet<NavigationIntent> { intent1, intent2 ->
            intent2.priority - intent1.priority
        }.apply { addAll(intents) }
        return Transaction(
            requiredIntents = requiredIntents,
            originalIntent = originalIntent
        ).also {
            intents.clear() // Clear the intents after building the set.
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RedirectChainBuilder) return false

        if (originalIntent != other.originalIntent) return false
        if (intents != other.intents) return false

        return true
    }

    override fun hashCode(): Int {
        var result = originalIntent.hashCode()
        result = 31 * result + intents.hashCode()
        return result
    }

    override fun toString(): String {
        return "RedirectChainBuilder(originalIntent=$originalIntent, intents=$intents)"
    }
}