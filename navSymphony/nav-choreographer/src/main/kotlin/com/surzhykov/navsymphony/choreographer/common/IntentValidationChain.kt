package com.surzhykov.navsymphony.choreographer.common

import com.surzhykov.navsymphony.graph.core.node.NavigationNode

interface IntentValidationChain {

    /**
     * Validates a [NavigationIntent] against a [NavigationNode].
     *
     * @param intent The intent to validate.
     * @param node The current navigation node to validate against.
     * @return The [ValidationResult] of the validation.
     */
    suspend fun validate(intent: NavigationIntent, node: NavigationNode): ValidationResult

    /**
     * Base implementation of [IntentValidationChain] that iterates over a set of chains.
     *
     * This class orchestrates the validation process by delegating to a set of validation chains.
     * It handles different [ValidationResult] types:
     * - [ValidationResult.Invalid]: Returns immediately, as the intent is not processable.
     * - [ValidationResult.Valid]: Continues to the next chain.
     * - [ValidationResult.Redirect]: Combines redirect results.
     *
     * @param chains The set of [IntentValidationChain] to be executed.
     */
    open class Base(
        private val chains: Set<IntentValidationChain>
    ) : IntentValidationChain {

        /**
         * Validates the given [intent] against the provided [node] using the set of validation chains.
         * @return The aggregate validation result
         */
        override suspend fun validate(
            intent: NavigationIntent,
            node: NavigationNode
        ): ValidationResult {
            // Make the redirect result nullable, because if the result is not a redirect then
            // there is no purpose in creating it. Although, if the result is a redirect, then
            // it will be returned with the combination of other returns.
            var redirectResult: ValidationResult.Redirect? = null
            chains.forEach { chain ->
                when (val result: ValidationResult = chain.validate(intent, node)) {
                    // If the result is invalid, the we can return it immediately, because
                    // the intent cannot be processed and there is no point in continuing
                    // the validation.
                    is ValidationResult.Invalid  -> return result

                    // If the result is ignore, the we can return it immediately, because
                    // the intent cannot be processed and there is no point in continuing
                    // the validation.
                    is ValidationResult.Ignore   -> return result

                    // If the current result is valid, it doesn't mean that other chains
                    // will return a valid result, so we continue to the next chain.
                    is ValidationResult.Valid    -> return@forEach

                    // If the result is redirect, we need to combine it with the previous
                    // redirect result, if there is one. If there is no previous redirect
                    // result, we set the current result as the redirect result.
                    is ValidationResult.Redirect -> {
                        if (redirectResult == null) {
                            redirectResult = result
                        } else {
                            val redirectIntents = result.redirectBuilder
                            redirectResult.redirectBuilder.addIntents(redirectIntents.build().requiredIntents)
                        }
                    }
                }
            }

            // If the redirect result is not null, it means that at least one chain returned
            return redirectResult ?: ValidationResult.Valid
        }
    }
}