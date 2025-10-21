package com.surzhykov.navsymphony.choreographer.data

import com.surzhykov.navsymphony.choreographer.common.NavigationIntent

/**
 * Represents the outcome of a navigation transaction, providing information about the next step.
 * A transaction can either continue to a new destination or revert to the original one.
 *
 * @property intent The [NavigationIntent] that triggered this transaction.
 */
sealed interface TransactionInfo {

    val intent: NavigationIntent

    data class Continue(
        override val intent: NavigationIntent,
    ) : TransactionInfo

    data class BackToOriginal(
        override val intent: NavigationIntent,
    ) : TransactionInfo
}