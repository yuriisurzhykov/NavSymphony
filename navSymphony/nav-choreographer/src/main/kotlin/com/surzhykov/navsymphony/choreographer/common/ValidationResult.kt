package com.surzhykov.navsymphony.choreographer.common

/**
 * Represents the outcome of a validation process for a [NavigationIntent].
 *
 * This sealed class allows for three distinct outcomes:
 * - [Valid]: The validation passed successfully.
 * - [Invalid]: The validation failed, with a descriptive message.
 * - [Redirect]: The validation requires a redirect to another [NavigationIntent].
 */
sealed class ValidationResult {

    /**
     * Represents a successful validation.
     *
     * Indicates that the [NavigationIntent] is valid and can be executed. No additional data
     * is needed for a successful result.
     */
    data object Valid : ValidationResult()

    /**
     * Represents a failed validation.
     *
     * Holds the error message for failed validation.
     *
     * @property message A human-readable message explaining why the validation failed.
     */
    data class Invalid(val message: String) : ValidationResult()

    /**
     * Represents a validation that should be ignored.
     *
     * This result indicates that the current validation process should be halted and no further
     * action (like proceeding with navigation or showing an error) should be taken based on this
     * outcome. It effectively signals to the validation system to disregard the current intent and
     * its corresponding validator.
     */
    data object Ignore : ValidationResult()

    /**
     * Represents a validation that requires a redirect to a different [NavigationIntent].
     * Holds information about the redirect.
     *
     * @property originalIntent The original [NavigationIntent] that triggered the validation.
     * @property redirectBuilder A builder responsible for constructing the redirect chain.
     */
    data class Redirect(
        val originalIntent: NavigationIntent,
        val redirectBuilder: RedirectChainBuilder
    ) : ValidationResult()
}