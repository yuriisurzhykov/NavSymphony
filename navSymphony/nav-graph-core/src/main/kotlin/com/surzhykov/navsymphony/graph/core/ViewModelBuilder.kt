package com.surzhykov.navsymphony.graph.core

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlin.reflect.KClass

/**
 * A builder class for creating and retrieving `ViewModel` instances within Compose.
 *
 * This class simplifies the process of obtaining `ViewModel`s by providing a `viewModel` function
 * that leverages the underlying `ViewModelProvider.Factory` and integrates seamlessly with Compose.
 * It delegates to a given `ViewModelProvider.Factory` for creating the `ViewModel`s.
 *
 * @property viewModelFactory The `ViewModelProvider.Factory` responsible for creating `ViewModel`
 * instances. This factory is used when the requested `ViewModel` doesn't exist or needs to be
 * recreated.
 */
open class ViewModelBuilder(
    private val viewModelFactory: ViewModelProvider.Factory,
) : ViewModelProvider.Factory by viewModelFactory {

    @Composable
    fun <T : ViewModel> viewModel(
        modelClass: KClass<T>,
        key: String? = modelClass.simpleName,
    ): T {
        return androidx.lifecycle.viewmodel.compose.viewModel(
            modelClass = modelClass,
            factory = viewModelFactory,
            key = key
        )
    }
}

