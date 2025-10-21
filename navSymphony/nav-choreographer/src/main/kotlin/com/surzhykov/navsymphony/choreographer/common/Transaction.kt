package com.surzhykov.navsymphony.choreographer.common

/**
 * Represents a transaction in navigation, encapsulating the required intents to reach a destination
 * and the original intent.
 *
 * This class is used to track the series of `NavigationIntent`s that are needed to arrive
 * at a  particular destination, as well as the `NavigationIntent` that originally triggered
 * the navigation. It provides context and traceability for complex navigation flows.
 * Example:
 * ```kotlin
 * // Assuming a navigation flow where going to ScreenC requires first going to ScreenA and then ScreenB.
 * val intentA = NavigationIntent.Navigate("screenA")
 * val intentB = NavigationIntent.Navigate("screenB")
 * val intentC = NavigationIntent.Navigate("screenC")
 *
 * val transaction = Transaction(setOf(intentA, intentB), intentC)
 *
 * // transaction.requiredIntents will contain {intentA, intentB}
 * // transaction.originalIntent will be intentC
 * ```
 * @property requiredIntents A set of `NavigationIntent`s that are necessary to navigate to the
 * desired destination. These intents represent the sequence of actions or steps required. The
 * order of intents in the set does not matter for the transaction, as the transaction does not
 * enforce order.
 * @property originalIntent The initial `NavigationIntent` that initiated the transaction. This
 * represents the user's starting point or the first action that triggered the navigation flow.
 */
data class Transaction(
    val requiredIntents: Set<NavigationIntent>,
    val originalIntent: NavigationIntent
)