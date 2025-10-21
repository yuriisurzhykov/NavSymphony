package com.surzhykov.navsymphony.choreographer.presentation

import androidx.compose.runtime.Immutable
import com.surzhykov.navsymphony.choreographer.common.IntentSender
import com.surzhykov.navsymphony.choreographer.common.NavigationIntent
import com.surzhykov.navsymphony.choreographer.common.NavigationIntentActor
import com.surzhykov.navsymphony.choreographer.common.navigationLogger
import com.surzhykov.navsymphony.domain.Logger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * [AppComponentIntentActor] is an interface responsible for handling navigation intents within
 * the application. It defines the contract for actors that can publish and process various types
 * of navigation intents.
 */
@Immutable
interface AppComponentIntentActor : NavigationIntentActor {

    /**
     * Publishes an [AppNavigationIntent].
     *
     * @param intent The [AppNavigationIntent] to publish.
     */
    fun publishIntent(intent: AppNavigationIntent): Boolean

    /**
     * [Abstract] provides a base implementation for [AppComponentIntentActor].
     * It manages the flow of navigation intents and provides utility methods for intent publishing.
     *
     * @param defaultPriority The default priority of this actor.
     * @param sender The sender of the intents.
     * @param logger The logger for logging messages.
     */
    abstract class Abstract(
        override val defaultPriority: Int,
        override val sender: IntentSender,
        private val logger: Logger = navigationLogger()
    ) : AppComponentIntentActor {
        // Channel to handle NavigationIntent
        private val mIntentFlow = Channel<NavigationIntent>(Channel.BUFFERED)

        /**
         * A flow of [NavigationIntent] that allows listening for navigation requests.
         */
        override val intentFlow: Flow<NavigationIntent> = mIntentFlow.receiveAsFlow()

        /**
         * Publishes an [AppNavigationIntent] by mapping it to a [NavigationIntent] and sending it
         * to the intent flow.
         *
         * @param intent The [AppNavigationIntent] to publish.
         */
        override fun publishIntent(intent: AppNavigationIntent): Boolean {
            return mIntentFlow.trySend(intent.map(this)).isSuccess
        }

        /**
         * Publishes a raw [NavigationIntent] directly to the intent flow.
         *
         * @param intent The [NavigationIntent] to publish.
         */
        protected fun publishIntent(intent: NavigationIntent) {
            mIntentFlow.trySend(intent)
        }

        companion object {
            private val TAG = AppComponentIntentActor::class.simpleName
        }
    }
}