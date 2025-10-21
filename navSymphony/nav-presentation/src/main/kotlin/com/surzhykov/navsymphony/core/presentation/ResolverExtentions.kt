package com.surzhykov.navsymphony.core.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

/**
 * Composable function to remember a [DrawableResolver] in local scope of composable function.
 * It wraps the [DrawableResolver.Resource] into `remember` block so that recompositions won't
 * be triggered because of it.
 *
 * @param resourceId The ID of drawable resource
 * @return An instance of [DrawableResolver.Resource]
 * */
@Composable
fun drawableResolver(@DrawableRes resourceId: Int): DrawableResolver {
    return remember(resourceId) { DrawableResolver.Resource(resourceId) }
}

/**
 * Composable function to remember a [StringResolver] in local scope of composable function. It
 * wraps the [StringResolver.Resource] into remember block so that recompositions won't be triggered
 * because of it.
 *
 * @param resourceId The ID of string resource
 * @return An instance of [StringResolver.Resource]
 * */
@Composable
fun stringResolver(@StringRes resourceId: Int): StringResolver {
    return remember(resourceId) { StringResolver.Resource(resourceId) }
}

/**
 * Creates and remembers a [StringResolver.AnnotateColor] instance. It is used to annotate a string
 * resource text with a color provided as a parameter.
 *
 * @param resourceId The ID of string resource.
 * @param color The color to annotate the string with.
 * */
@Composable
fun stringResolver(@StringRes resourceId: Int, color: Color): StringResolver {
    return remember(resourceId, color) {
        StringResolver.AnnotateColor(
            StringResolver.Resource(resourceId),
            color
        )
    }
}

@Composable
fun stringResolver(@StringRes resourceFormat: Int, vararg args: StringResolver): StringResolver {
    return remember(resourceFormat, *args) {
        StringResolver.Formatter(resourceFormat, *args)
    }
}

@Composable
fun stringResolver(string: String): StringResolver {
    return remember(string) { StringResolver.BaseString(string) }
}

@Composable
fun stringResolver(string: String, vararg args: Any): StringResolver {
    return remember(string) { StringResolver.BaseString(string.format(*args)) }
}

@Composable
fun StringResolver.uppercase(): StringResolver = stringResolver(asString().uppercase())