package com.surzhykov.navsymphony.choreographer.presentation

/**
 * Represents intents related to system-level navigation events, such as user interactions
 * or programmatic lock management. These intents are used to manage the navigation state,
 * particularly concerning automatic timeouts or screen locking mechanisms.
 */
sealed interface SystemNavigationIntent {

    /**
     * Represents a generic user interaction that might trigger a navigation action (e.g. pressing
     * buttons, clicking on screen, swiping, dragging, etc.).
     */
    object UserInteraction : SystemNavigationIntent

    /**
     * An intent to acquire a lock that prevents the system from automatically navigating away
     * due to a timeout. This is useful for scenarios where the screen must remain visible
     * for an extended period, such as during a video call or when critical information
     * is being displayed.
     *
     * The lock is acquired for a specific [lockReason], which provides context for why the
     * timeout is being disabled. Multiple locks can be acquired for different reasons.
     * The timeout will only resume once all locks have been released.
     *
     * @property lockReason The reason for acquiring the lock. See [TimeoutLockReason] for possible values.
     * @see ReleaseLock
     * @see TimeoutLockReason
     */
    @SensitiveNavigationApi
    data class AcquireTimeoutLock(val lockReason: TimeoutLockReason) : SystemNavigationIntent

    /**
     * An intent to release a previously acquired navigation lock. This is used to signal
     * that a specific reason for preventing automatic navigation is no longer valid.
     *
     * When all locks acquired via [AcquireTimeoutLock] are released, the system's automatic
     * timeout and navigation mechanisms can resume. This intent should be sent when the
     * condition that required the lock (e.g., a video call ending) is resolved.
     *
     * @see AcquireTimeoutLock
     * @see TimeoutLockReason
     */
    @SensitiveNavigationApi
    data object ReleaseLock : SystemNavigationIntent

}