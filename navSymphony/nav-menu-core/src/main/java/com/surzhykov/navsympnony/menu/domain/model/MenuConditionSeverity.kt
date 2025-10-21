package com.surzhykov.navsympnony.menu.domain.model

/**
 * Represents the severity of a menu condition.
 */
sealed interface MenuConditionSeverity {

    /**
     * Indicates a normal condition without any issues.
     */
    data object Normal : MenuConditionSeverity

    /**
     * Indicates a condition that requires attention but is not critical.
     */
    data object Warning : MenuConditionSeverity

    /**
     * Indicates a critical condition that requires immediate attention.
     */
    data object Error : MenuConditionSeverity
}