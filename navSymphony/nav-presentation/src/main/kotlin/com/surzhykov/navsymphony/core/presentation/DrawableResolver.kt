package com.surzhykov.navsymphony.core.presentation

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp

interface DrawableResolver : ResourceResolver<ImageVector> {

    @Composable
    fun asDrawable(): Drawable?

    @Immutable
    data class Resource(
        @param:DrawableRes private val drawableRes: Int,
    ) : DrawableResolver {
        @Composable
        override fun resolve() = ImageVector.vectorResource(drawableRes)

        @Composable
        override fun asDrawable(): Drawable? {
            return AppCompatResources.getDrawable(LocalContext.current, drawableRes)
        }
    }

    @Stable
    class Empty : DrawableResolver {
        @Composable
        override fun resolve(): ImageVector = remember {
            ImageVector.Builder(
                defaultWidth = 0.dp,
                defaultHeight = 0.dp,
                viewportWidth = 0f,
                viewportHeight = 0f
            ).build()
        }

        @Composable
        override fun asDrawable(): Drawable? = null

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Empty) return false
            return true
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }

    }
}