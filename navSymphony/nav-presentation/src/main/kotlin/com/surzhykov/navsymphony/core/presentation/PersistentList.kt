package com.surzhykov.navsymphony.core.presentation

import kotlinx.collections.immutable.ImmutableList


/**
 * Returns a new persistent list of the specified elements. The difference between this and
 * [kotlinx.collections.immutable.persistentListOf] is that this implementation takes nullable
 * variable arguments and returns a non-nullable list of elements.
 */
fun <E> persistentNonNullListOf(vararg elements: E?): ImmutableList<E> {
    return kotlinx.collections.immutable.persistentListOf<E>()
        .addAll(elements.asList().filterNotNull())
}