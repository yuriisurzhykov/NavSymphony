package com.surzhykov.navsymphony.domain

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * [Dispatcher] interface is designed for [androidx.lifecycle.ViewModel]s to better maintain
 * coroutines and all UI and background work to be executed there.
 * ```
 *  class ExampleViewModel(
 *       private val dispatcher: Dispatcher
 *  ) : ViewModel() {
 *      fun userSelectedSomething() {
 *          dispatcher.launchBackground(viewModelScope) {
 *              // run background logic
 *              dispatcher.switchUi {
 *                  // now update your views on UI thread
 *              }
 *          }
 *      }
 *  }
 * ```
 * */
interface Dispatcher {

    fun launchUi(scope: CoroutineScope, block: suspend CoroutineScope.() -> Unit): Job
    fun launchBackground(scope: CoroutineScope, block: suspend CoroutineScope.() -> Unit): Job
    suspend fun switchToUi(block: suspend CoroutineScope.() -> Unit)

    abstract class Abstract(
        private val ui: CoroutineDispatcher,
        private val background: CoroutineDispatcher
    ) : Dispatcher {
        override fun launchUi(
            scope: CoroutineScope,
            block: suspend CoroutineScope.() -> Unit
        ): Job = scope.launch(ui, block = block)

        override fun launchBackground(
            scope: CoroutineScope,
            block: suspend CoroutineScope.() -> Unit
        ): Job = scope.launch(background, block = block)

        override suspend fun switchToUi(block: suspend CoroutineScope.() -> Unit) =
            withContext(ui, block)
    }

    class Base : Abstract(Dispatchers.Main, Dispatchers.IO)
}