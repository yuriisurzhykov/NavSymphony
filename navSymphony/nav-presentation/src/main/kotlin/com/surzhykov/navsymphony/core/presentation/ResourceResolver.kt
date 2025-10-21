package com.surzhykov.navsymphony.core.presentation

import androidx.compose.runtime.Composable

/**
 * This interface represents any resolver that can work with Android resources. It becomes very
 * useful when the resource needs to be provided from domain layer to the UI. So instead of providing
 * resource ID, or any, you may be able to provide the resolver and then in the UI you will be able
 * to resolve the resource based on its type.
 * @param T The type of the resource that should be resolved.
 * */
interface ResourceResolver<T : Any> {

    /**
     * Resolves the resources with the type [T] and since the function is @Composable, the actual
     * implementation of this method may use `remember` block to remember resources to avoid
     * unnecessary recompositions.
     *
     *  @return The instance of resource with the type [T]
     * */
    @Composable
    fun resolve(): T
}