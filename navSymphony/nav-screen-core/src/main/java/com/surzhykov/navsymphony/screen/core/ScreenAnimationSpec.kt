package com.surzhykov.navsymphony.screen.core

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry

/**
 * This typealias only created to shorten the amount of lines for properties declaration.
 *
 * The purpose is to create an alias for enter animation transition for any Jetpack Compose screen.
 * @see EnterTransition
 * @see AnimatedContentTransitionScope
 * */
typealias EnterAnimationTransition = AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards EnterTransition?

/**
 * This typealias only created to shorten the amount of lines for properties declaration.
 *
 * The purpose is to create an alias for exit animation transition for any Jetpack Compose screen.
 * @see ExitTransition
 * @see AnimatedContentTransitionScope
 * */
typealias ExitAnimationTransition = AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards ExitTransition?

/**
 * This typealias only created to shorten the amount of lines for properties declaration.
 *
 * The purpose is to create an alias for animation to transform a size of screen during the transition
 * for any Jetpack Compose screen.
 * @see SizeTransform
 * @see AnimatedContentTransitionScope
 * */
typealias AnimationSizeTransform = AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards SizeTransform?

/**
 * Defines the animation specifications for screen transitions in a navigation context.
 *
 * This interface allows you to customize the enter, exit, pop enter, and pop exit transitions,
 * as well as the size transformation, for screen changes.
 *
 * Each transition type (enter, exit, pop enter, pop exit) and size transformation can be
 * defined independently, allowing for fine-grained control over the visual effects of navigation.
 */
@Immutable
interface ScreenAnimationSpec {

    @Stable
    fun enterTransition(): EnterAnimationTransition?

    @Stable
    fun exitTransition(): ExitAnimationTransition?

    @Stable
    fun popEnterTransition(): EnterAnimationTransition?

    @Stable
    fun popExitTransition(): ExitAnimationTransition?

    @Stable
    fun sizeTransform(): AnimationSizeTransform?

    @Immutable
    open class Base(
        private val enterDirection: AnimatedContentTransitionScope.SlideDirection = AnimatedContentTransitionScope.SlideDirection.Left,
        private val exitDirection: AnimatedContentTransitionScope.SlideDirection = AnimatedContentTransitionScope.SlideDirection.Left,
        private val popEnterDirection: AnimatedContentTransitionScope.SlideDirection = AnimatedContentTransitionScope.SlideDirection.Right,
        private val popExitDirection: AnimatedContentTransitionScope.SlideDirection = AnimatedContentTransitionScope.SlideDirection.Right,
        private val animationSpec: FiniteAnimationSpec<IntOffset> = tween(700)
    ) : ScreenAnimationSpec {

        private val enterTransitionCallback: EnterAnimationTransition by lazy {
            {
                slideIntoContainer(
                    towards = enterDirection,
                    animationSpec = animationSpec
                )
            }
        }

        private val exitTransitionCallback: ExitAnimationTransition by lazy {
            {
                slideOutOfContainer(
                    towards = exitDirection,
                    animationSpec = animationSpec
                )
            }
        }

        private val popEnterTransitionCallback: EnterAnimationTransition by lazy {
            {
                slideIntoContainer(
                    towards = popEnterDirection,
                    animationSpec = animationSpec
                )
            }
        }

        private val popExitTransitionCallback: ExitAnimationTransition by lazy {
            {
                slideOutOfContainer(
                    towards = popExitDirection,
                    animationSpec = animationSpec
                )
            }
        }

        @Stable
        override fun enterTransition() = enterTransitionCallback

        @Stable
        override fun exitTransition() = exitTransitionCallback

        @Stable
        override fun popEnterTransition() = popEnterTransitionCallback

        @Stable
        override fun popExitTransition() = popExitTransitionCallback

        @Stable
        override fun sizeTransform(): (AnimatedContentTransitionScope<NavBackStackEntry>.() -> SizeTransform?)? =
            null
    }

    @Immutable
    class None : ScreenAnimationSpec {
        @Stable
        override fun enterTransition(): EnterAnimationTransition? = null

        @Stable
        override fun exitTransition(): ExitAnimationTransition? = null

        @Stable
        override fun popEnterTransition(): EnterAnimationTransition? = null

        @Stable
        override fun popExitTransition(): ExitAnimationTransition? = null

        @Stable
        override fun sizeTransform(): AnimationSizeTransform? = null

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is None) return false
            return true
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }
}