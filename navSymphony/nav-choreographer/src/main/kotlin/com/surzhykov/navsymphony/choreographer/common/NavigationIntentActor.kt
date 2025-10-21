package com.surzhykov.navsymphony.choreographer.common

import kotlinx.coroutines.flow.Flow

/**
 * Represents an actor that can handle and emit navigation intents.
 *
 * This interface defines the core functionalities required for an object
 * to act as a source of navigation requests.
 */
interface NavigationIntentActor {

    /**
     * Provides a way to send navigation intents.
     */
    val sender: IntentSender

    /**
     * Defines the default priority for intents handled by this actor.
     */
    val defaultPriority: Int

    /**
     * A stream of navigation intents emitted by this actor.
     */
    val intentFlow: Flow<NavigationIntent>
}