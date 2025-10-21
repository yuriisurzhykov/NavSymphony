package com.surzhykov.navsymphony.choreographer.data

import com.surzhykov.navsymphony.choreographer.common.NavigationIntent
import com.surzhykov.navsymphony.choreographer.common.Transaction
import com.surzhykov.navsymphony.choreographer.common.navigationLogger
import com.surzhykov.navsymphony.domain.Logger
import javax.inject.Inject

/**
 * Manages navigation transactions, enabling sequential processing of navigation intents.
 *
 * This interface defines the contract for managing transactions, which consist of a series of
 * [NavigationIntent]s that should be executed in order.
 */
interface NavigationTransactionManager {

    /**
     * Retrieves the currently active [NavigationIntent] in the ongoing transaction.
     *
     * @return The current [NavigationIntent] or null if there is no ongoing transaction or if the
     * current intent is not yet determined.
     */
    fun current(): NavigationIntent?

    /**
     * Applies a new [Transaction], setting up the manager to process the required intents.
     *
     * @param transaction The transaction to apply.
     * @throws IllegalStateException If there is an ongoing transaction that has not been completed
     * or canceled. The current transaction must be either canceled or finished before applying a
     * new one.
     */
    fun applyTransaction(transaction: Transaction)

    /**
     * Advances to the next [NavigationIntent] in the current transaction.
     *
     * @return The next [NavigationIntent] to be processed.
     * @throws IllegalStateException If there is no transaction applied or the internal state is invalid.
     */
    fun next(): TransactionInfo

    /**
     * Cancels the current transaction, resetting the manager to a clean state.
     *
     * After cancellation, no intents from the previous transaction will be processed.
     */
    fun cancel()

    /**
     * Indicates whether a transaction is active and has more intents to process.
     */
    fun isActive(): Boolean

    /**
     * Default implementation of [NavigationTransactionManager]. Handles sequencing, state checks,
     * and safety around transaction processing.
     */
    class Base @Inject constructor(
        private val logger: Logger = navigationLogger(),
    ) : NavigationTransactionManager {

        @Volatile
        private var transaction: Transaction? = null

        @Volatile
        private var intentsIterator: Iterator<NavigationIntent>? = null

        @Volatile
        private var current: NavigationIntent? = null

        /**
         * Returns the currently active navigation intent.
         */
        @Synchronized
        override fun current(): NavigationIntent? = current

        /**
         * Applies a new transaction to the manager.
         */
        @Synchronized
        override fun applyTransaction(transaction: Transaction) {
            // If there is already a transaction in progress, throw an exception, because the
            // client code needs either cancel the current transaction or finish it explicitly
            // before applying a new one.
            if (this.transaction != null && intentsIterator?.hasNext() == true) {
                logger.e(TAG, "Attempted to apply transaction while another is still active.")
                throw IllegalStateException(
                    "Current transaction is not finished! Cancel it before applying a new one!"
                )
            }
            this.transaction = transaction
            this.intentsIterator = transaction.requiredIntents.iterator()
            this.current = null
        }

        /**
         * Retrieves the next intent to process or resets the state if the transaction is complete.
         * @return The next intent to process and a flag indicating whether it's the original intent.
         */
        @Synchronized
        override fun next(): TransactionInfo {
            if ((transaction == null && intentsIterator != null) ||
                (transaction != null && intentsIterator == null)
            ) {
                logger.e(TAG, "Invalid transaction state. Resetting.")
                transaction = null
                intentsIterator = null
                current = null
                throw IllegalStateException(
                    "Invalid transaction state! Inconsistent nullability detected. " +
                            "Transaction is null but there are pending intents to be processed!"
                )
            }

            if (transaction == null) {
                // Otherwise, if transaction is null, it means that there is no transaction to proceed.
                throw IllegalAccessException("There is no transaction to proceed!")
            } else if (intentsIterator?.hasNext() == false) {
                // In case there are no more pending intents to process, reset the transaction and
                // return the original intent.
                current = transaction?.originalIntent
                transaction = null
                intentsIterator = null
            } else {
                // If there are more intents to process, get the next one.
                current = intentsIterator?.next()
            }

            val currentIntent =
                current ?: throw IllegalStateException("Current intent is unexpectedly null!")
            // Returns the current intent and a flag indicating whether it's the original intent
            // or the redirect intent.
            return if (transaction == null) {
                TransactionInfo.BackToOriginal(currentIntent)
            } else {
                TransactionInfo.Continue(currentIntent)
            }
        }

        /**
         * Cancels the current transaction.
         */
        @Synchronized
        override fun cancel() {
            transaction = null
            intentsIterator = null
            current = null
        }

        /**
         * Indicates whether a transaction is active and has more intents to process.
         */
        override fun isActive(): Boolean = synchronized(this) {
            transaction != null && intentsIterator != null
        }

        companion object {
            private val TAG = NavigationTransactionManager::class.java.simpleName
        }
    }
}
