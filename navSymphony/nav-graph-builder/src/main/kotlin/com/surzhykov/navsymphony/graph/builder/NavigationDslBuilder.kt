package com.surzhykov.navsymphony.graph.builder

import com.surzhykov.navsymphony.graph.core.graph.NavigationGraphDsl

/**
 * `NavigationDslBuilder` is an interface for creating navigation components within a custom
 * navigation DSL.
 *
 * Implement this interface to define builders that construct specific parts of your navigation
 * graph, such as individual screens or nested navigation flows. These builders are used within
 * a domain-specific language (DSL) powered by the `@NavigationGraphDsl` annotation to provide
 * a structured and readable way to define app navigation. The `build()` function is responsible
 * for generating the final navigation component.
 * */
@NavigationGraphDsl
interface NavigationDslBuilder<T : Any> {
    fun build(): T
}