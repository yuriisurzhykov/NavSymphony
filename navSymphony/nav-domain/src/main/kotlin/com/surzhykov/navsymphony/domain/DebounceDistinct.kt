package com.surzhykov.navsymphony.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

// It's just a marker for key reference in [DebounceDistinct]
private val NULL = Any()

private val defaultKeySelector: (Any?) -> Any? = { it }
private val defaultAreEquivalent: (Any?, Any?) -> Boolean = { old, new -> old == new }

/**
 * Returns a [Flow] that emits values from the original flow, suppressing repetitions of equivalent items
 * that occur within a specified time window.
 *
 * Unlike `distinctUntilChanged`, this operator only filters out consecutive duplicate items
 * if they arrive within the [windowMillis] period since the last emission.
 *
 * This is useful for suppressing noisy signals such as navigation events or UI inputs
 * that may emit duplicates in rapid succession, while still allowing logically equivalent
 * events to pass through after the debounce period.
 *
 * Example:
 * ```
 * flowOf("A", "A", "A", delay(200), "A")
 *   .debounceDistinctWithin(100)
 *   .collect { println(it) } // Prints: A, A (after delay)
 * ```
 *
 * @param windowMillis The duration (in milliseconds) during which duplicate emissions are suppressed.
 * @param keySelector A function to extract the comparison key from the emitted value. Defaults to identity.
 * @param areEquivalent A function to determine whether two keys are equivalent. Defaults to structural equality.
 */
fun <T> Flow<T>.debounceDistinctWithin(
    windowMillis: Long,
    keySelector: (T) -> Any? = defaultKeySelector,
    areEquivalent: (old: Any?, new: Any?) -> Boolean = defaultAreEquivalent,
): Flow<T> {
    return when {
        this is DebounceDistinct && windowMillis == this.windowDuration.inWholeMilliseconds && keySelector == this.keySelector && areEquivalent == this.areEquivalent -> this
        else -> DebounceDistinct(windowMillis.milliseconds, this, areEquivalent, keySelector)
    }
}

/**
 * Returns a [Flow] that emits values from the original flow, suppressing repetitions of equivalent
 * items that occur within a specified time window.
 *
 * This overload accepts a [Duration] for time window specification.
 *
 * @param windowDuration The time window during which equivalent emissions are filtered out.
 * @param keySelector A function to extract the comparison key from the emitted value. Defaults to identity.
 * @param areEquivalent A function to determine whether two keys are equivalent. Defaults to structural equality.
 *
 * @see debounceDistinctWithin
 */
fun <T> Flow<T>.debounceDistinctWithin(
    windowDuration: Duration,
    keySelector: (T) -> Any? = defaultKeySelector,
    areEquivalent: (old: Any?, new: Any?) -> Boolean = defaultAreEquivalent,
): Flow<T> {
    return when {
        this is DebounceDistinct && windowDuration == this.windowDuration && keySelector == this.keySelector && areEquivalent == this.areEquivalent -> this
        else -> DebounceDistinct(windowDuration, this, areEquivalent, keySelector)
    }
}

private class DebounceDistinct<T>(
    val windowDuration: Duration,
    private val upstream: Flow<T>,
    @JvmField val areEquivalent: (old: Any?, new: Any?) -> Boolean,
    @JvmField val keySelector: (T) -> Any?,
) : Flow<T> {
    override suspend fun collect(collector: FlowCollector<T>) {
        var previousKey: Any? = NULL
        var lastEmitTime: Long = System.currentTimeMillis()
        upstream.collect { value ->
            val now = System.currentTimeMillis()
            val key = keySelector(value)
            if (previousKey === NULL ||
                !areEquivalent(previousKey, key) ||
                now - lastEmitTime >= windowDuration.inWholeMilliseconds
            ) {
                previousKey = key
                lastEmitTime = now
                collector.emit(value)
            }
        }
    }
}