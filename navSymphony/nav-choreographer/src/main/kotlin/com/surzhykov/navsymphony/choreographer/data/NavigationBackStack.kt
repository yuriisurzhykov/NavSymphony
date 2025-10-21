package com.surzhykov.navsymphony.choreographer.data

import com.surzhykov.navsymphony.choreographer.common.NavigationOptions
import com.surzhykov.navsymphony.choreographer.common.navigationLogger
import com.surzhykov.navsymphony.domain.Logger
import java.util.concurrent.ConcurrentLinkedDeque


/**
 * [NavigationBackStack] manages a history of navigation entries.
 * It supports adding new entries, removing entries, and clearing the entire history.
 *
 * This class is designed to handle two types of navigation entries:
 * - **Entries Back Stack:** Main history of the navigated screens.
 * - **Non-Retained Entries:** Temporary entries that are cleared upon popping.
 *
 * Think of it like a stack of plates: you can add a plate on top ([add]), remove the top plate
 * ([pop]), or remove all plates ([clear]). You can also search for a particular plate and remove
 * all above it ([popUntil]). But in addition to this, there's a temporary stack of plates that
 * added as an intermediate step ([nonRetainedEntries]). The intermediate back stack are cleared
 * once you try to pop from the main back stack. So basically you can think of [nonRetainedEntries]
 * stack as an entry with multiple screens (entries) that will be popped up when you try to pop.
 *
 * @param T The type of entry stored in the back stack. It can be any object.
 * */
class NavigationBackStack<T : Any>(
    private val logger: Logger = navigationLogger()
) {

    /**
     * The main stack of navigation entries.
     * */
    private val entriesBackStack = ConcurrentLinkedDeque<T>()

    /**
     * Temporary entries that are cleared when popping from the back stack.
     * */
    private val nonRetainedEntries = ConcurrentLinkedDeque<T>()

    /**
     * The total number of entries in both [entriesBackStack] and [nonRetainedEntries].
     */
    val size: Int
        get() = entriesBackStack.size + nonRetainedEntries.size

    /**
     * Adds an [entry] to the back stack based on the provided [options].
     *
     * - If `clearBackStack` is true, it clears the entire back stack before adding.
     * - If `addToBackStack` is true, the entry is added to the main [entriesBackStack] and clears
     * [nonRetainedEntries] back stack.
     *      - It handles `singleTop` by avoiding adding the same entry twice at the top.
     * - If `addToBackStack` is false, the entry is added to [nonRetainedEntries].
     *      - It also handles `singleTop` in the same way as above.
     *
     * @param entry The entry to be added to the back stack.
     * @param options The navigation options that determine how the entry is added.
     */
    fun add(entry: T, options: NavigationOptions) {
        if (options.clearBackStack) {
            clear()
        }
        if (options.addToBackStack) {
            nonRetainedEntries.clear()
            if (entriesBackStack.isEmpty()) {
                entriesBackStack.addLast(entry)
            } else if (!(options.singleTop && entriesBackStack.last == entry)) {
                entriesBackStack.addLast(entry)
            }
        } else {
            if (nonRetainedEntries.isEmpty()) {
                nonRetainedEntries.addLast(entry)
            } else if (!(options.singleTop && nonRetainedEntries.last == entry)) {
                nonRetainedEntries.addLast(entry)
            }
        }
    }

    /**
     * Removes and returns the top entry from the back stack.
     *
     * - If there are entries in [nonRetainedEntries], they are cleared and the last entry from
     * [entriesBackStack] is returned.
     * - If [nonRetainedEntries] is empty and [entriesBackStack] is not empty, the last entry is
     * removed and the new last entry is returned.
     *
     * @return The top entry from the stack after popping.
     * @throws NoSuchElementException if both [nonRetainedEntries] and [entriesBackStack] are empty.
     */
    fun pop(): T {
        if (nonRetainedEntries.isNotEmpty()) {
            nonRetainedEntries.clear()
            return entriesBackStack.last
        } else {
            if (entriesBackStack.isNotEmpty()) {
                entriesBackStack.removeLast()
                return entriesBackStack.last
            } else {
                throw NoSuchElementException("Stack is empty! Can't pop element")
            }
        }
    }

    /**
     * Removes entries from the back stack until a specific entry is found that satisfies the given
     * [predicate].
     *
     * - If [inclusive] is true, the found entry is also removed.
     * - If [inclusive] is false, the found entry remains in the stack.
     * - Clears all entries in [nonRetainedEntries].
     *
     * Example:
     * ```kotlin
     * popUntil(inclusive = true) { it == entry }
     * ```
     *
     * @param inclusive Specifies whether the entry that satisfies the predicate should also be removed.
     * @param predicate A lambda function that checks each entry to determine if it's the desired entry.
     * @return The entry that satisfies the predicate.
     * @throws IllegalStateException if the [entriesBackStack] is empty.
     * @throws NoSuchElementException if no entry is found that satisfies the predicate. In this case,
     * [entriesBackStack] will be empty.
     */
    fun popUntil(inclusive: Boolean = false, predicate: (T) -> Boolean): T {
        if (entriesBackStack.isEmpty()) {
            throw IllegalStateException("Stack is empty!")
        }

        nonRetainedEntries.clear()
        while (entriesBackStack.isNotEmpty()) {
            val entry = entriesBackStack.removeLast()
            if (predicate(entry)) {
                if (!inclusive) {
                    entriesBackStack.addLast(entry)
                }
                return entry
            }
        }
        throw NoSuchElementException("Couldn't find entry, stack is empty now! Current stack size: $size, Entries stack size: ${entriesBackStack.size}")
    }

    /**
     * Returns the last entry added to the stack without removing it.
     *
     * If [nonRetainedEntries] is not empty, it returns the last entry from there.
     * Otherwise, it returns the last entry from [entriesBackStack].
     *
     * @return The last entry added to the stack.
     */
    fun last(): T {
        return if (nonRetainedEntries.isNotEmpty()) {
            nonRetainedEntries.last
        } else {
            entriesBackStack.last
        }
    }

    /**
     * Clears both [entriesBackStack] and [nonRetainedEntries], removing all navigation entries.
     */
    fun clear() {
        entriesBackStack.clear()
        nonRetainedEntries.clear()
    }

    companion object {
        private val TAG = NavigationBackStack::class.java.simpleName
    }
}