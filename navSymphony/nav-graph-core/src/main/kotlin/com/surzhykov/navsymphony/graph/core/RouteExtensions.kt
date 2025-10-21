package com.surzhykov.navsymphony.graph.core

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.ComposeNavigatorDestinationBuilder
import androidx.navigation.get
import androidx.navigation.serialization.decodeArguments
import com.surzhykov.navsymphony.screen.core.AbstractViewModel
import com.surzhykov.navsymphony.screen.core.Screen
import com.surzhykov.navsymphony.screen.core.ScreenAnimationSpec
import com.surzhykov.navsymphony.screen.core.ScreenIntent
import com.surzhykov.navsymphony.screen.core.ScreenRoute
import com.surzhykov.navsymphony.screen.core.ScreenState
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * Add the [Composable] to the [NavGraphBuilder]
 *
 * @param T route from a [KClass] for the destination
 * @param screen A [Screen] information that should be displayed as a composable screen.
 * @param parametersMap map of destination arguments' kotlin type [KType] to its respective custom
 *   [NavType]. May be empty if [T] does not use custom NavTypes.
 * @param content composable for the destination
 */
fun <R, I, S, VM, T : Screen<R, S, I, VM>> NavGraphBuilder.composable(
    screen: T,
    routeClass: KClass<R>,
    parametersMap: Map<KType, @JvmSuppressWildcards NavType<*>>,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) where R : ScreenRoute, I : ScreenIntent, S : ScreenState, VM : AbstractViewModel<S, I> {
    destination(
        ComposeNavigatorDestinationBuilder(
            provider[ComposeNavigator::class],
            routeClass,
            parametersMap,
            content
        )
            .apply {
                val animationSpec: ScreenAnimationSpec = screen.screenAnimationSpec
                this.enterTransition = animationSpec.enterTransition()
                this.exitTransition = animationSpec.exitTransition()
                this.popEnterTransition = animationSpec.popEnterTransition()
                this.popExitTransition = animationSpec.popExitTransition()
                this.sizeTransform = animationSpec.sizeTransform()
            }
    )
}

/**
 * Returns route as an object of type [T]
 *
 * Extrapolates arguments from [NavBackStackEntry.arguments] and recreates object [T]
 *
 * @param serializer A Kotlin serializer that can decode an argument with the type [T] from
 * arguments [Bundle]
 * @return A new instance of this entry's [NavDestination.route] as an object of type [T]
 */
@SuppressLint("RestrictedApi")
fun <T : ScreenRoute> NavBackStackEntry.toRoute(serializer: KSerializer<T>): T {
    val bundle = arguments ?: Bundle()
    val typeMap = destination.arguments.mapValues { it.value.type }
    return serializer.decodeArguments(bundle, typeMap)
}