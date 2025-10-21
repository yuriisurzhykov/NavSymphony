package com.surzhykov.navsymphony.choreographer.presentation

/**
 * Represents the reason why the navigation is locked due to a timeout.
 */
interface TimeoutLockReason {
    data object Generic : TimeoutLockReason
}