package com.surzhykov.navsymphony.graph.core.node

import com.surzhykov.navsymphony.screen.core.AbstractViewModel
import com.surzhykov.navsymphony.screen.core.Screen
import com.surzhykov.navsymphony.screen.core.ScreenIntent
import com.surzhykov.navsymphony.screen.core.ScreenRoute
import com.surzhykov.navsymphony.screen.core.ScreenState
import java.lang.ref.SoftReference
import kotlin.reflect.KClass

/**
 * An interface for lazily initializing [Screen] instances.
 * It provides a way to defer the creation of a [Screen] until it is actually needed.
 *
 * @param R The type of the [ScreenRoute].
 * @param S The type of the [ScreenState].
 * @param I The type of the [ScreenIntent].
 * @param VM The type of the [AbstractViewModel].
 */
interface LazyScreenInitializer<R : ScreenRoute, S : ScreenState, I : ScreenIntent, VM : AbstractViewModel<S, I>> :
    Lazy<Screen<R, S, I, VM>> {

    /**
     * Clears any cached [Screen] instance, allowing it to be garbage collected.
     */
    fun clear()

    /**
     * A [LazyScreenInitializer] implementation that initializes a [Screen] instance by invoking a
     * provided initializer. It caches the created [Screen] using a [SoftReference] to allow for
     * memory management.
     *
     * @param R The type of the [ScreenRoute].
     * @param S The type of the [ScreenState].
     * @param I The type of the [ScreenIntent].
     * @param VM The type of the [AbstractViewModel].
     * @property valueInitializer The function to invoke to create a new [Screen] instance.
     */
    open class ByInstance<R : ScreenRoute, S : ScreenState, I : ScreenIntent, VM : AbstractViewModel<S, I>>(
        private val valueInitializer: () -> Screen<R, S, I, VM>,
    ) : LazyScreenInitializer<R, S, I, VM> {
        private var screenReference: SoftReference<Screen<R, S, I, VM>>? = null
        override val value: Screen<R, S, I, VM>
            get() = synchronized(this) {
                if (screenReference == null || screenReference?.get() == null) {
                    screenReference = SoftReference(valueInitializer.invoke())
                }
                return screenReference?.get() ?: run {
                    screenReference = SoftReference(valueInitializer.invoke())
                    screenReference?.get()!!
                }
            }

        override fun isInitialized(): Boolean = true
        override fun toString(): String = "ByInstance"
        override fun clear() {
            screenReference?.clear()
        }
    }

    /**
     * A [LazyScreenInitializer] implementation that initializes a [Screen] instance based on its
     * class. It creates a new instance of the specified [Screen] class using its default constructor.
     *
     * @param R The type of the [ScreenRoute].
     * @param S The type of the [ScreenState].
     * @param I The type of the [ScreenIntent].
     * @param VM The type of the [AbstractViewModel].
     * @param screenClass The class of the [Screen] to create.
     */
    class ByClass<R : ScreenRoute, S : ScreenState, I : ScreenIntent, VM : AbstractViewModel<S, I>>(
        screenClass: KClass<out Screen<R, S, I, VM>>,
    ) : ByInstance<R, S, I, VM>(
        valueInitializer = {
            screenClass.java.getConstructor().newInstance() as Screen<R, S, I, VM>
        }
    ) {
        override fun toString(): String = "ByClass"
    }
}