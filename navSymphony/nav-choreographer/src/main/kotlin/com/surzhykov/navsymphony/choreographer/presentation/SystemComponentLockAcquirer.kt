package com.surzhykov.navsymphony.choreographer.presentation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * An interface for acquiring and releasing a lock on system-level components, such as
 * [SystemComponentIntentActor]. This prevents user interaction during critical, temporary
 * states, for example, when firmware update is being installed.
 *
 * The lock is represented by a [TimeoutLockReason], which provides context for why the components
 * are locked. The current state of the lock is exposed via [lockReasonState], allowing a receivers
 * to observe it and disable themselves accordingly.
 */
@OptIn(SensitiveNavigationApi::class)
interface SystemComponentLockAcquirer {

    /**
     * A [StateFlow] that emits the current reason for the system component lock.
     *
     * It holds a [TimeoutLockReason] when a lock is active, providing context for why
     * interactions are disabled. When no lock is active, it emits `null`.
     *
     * UI components can collect this flow to reactively enable or disable themselves
     * based on the lock state.
     */
    val lockReasonState: StateFlow<TimeoutLockReason?>

    fun acquireLock(lockReason: TimeoutLockReason): Boolean
    fun releaseLock(): Boolean

    class Default @Inject constructor(
        private val lock: MutableStateFlow<TimeoutLockReason?> = MutableStateFlow(null),
    ) : SystemComponentLockAcquirer {

        override val lockReasonState: StateFlow<TimeoutLockReason?> = lock.asStateFlow()

        override fun acquireLock(lockReason: TimeoutLockReason): Boolean {
            return lock.tryEmit(lockReason)
        }

        override fun releaseLock(): Boolean {
            return lock.tryEmit(null)
        }
    }
}