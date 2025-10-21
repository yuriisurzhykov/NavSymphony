package com.surzhykov.navsymphony.choreographer.presentation

import androidx.compose.runtime.Immutable
import com.surzhykov.navsymphony.choreographer.common.IntentSender
import com.surzhykov.navsymphony.choreographer.common.NavigationIntent
import com.surzhykov.navsymphony.choreographer.common.NavigationIntentActor
import com.surzhykov.navsymphony.choreographer.common.navigationLogger
import com.surzhykov.navsymphony.choreographer.data.NavigationStateHandler
import com.surzhykov.navsymphony.choreographer.utils.Priorities
import com.surzhykov.navsymphony.domain.Logger
import com.surzhykov.navsymphony.graph.core.node.NavigationNode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * [SystemComponentIntentActor] is responsible for handling system-level navigation intents,
 * such as interaction timeouts. It monitors user interactions and screen changes to determine
 * when to trigger an inactivity timeout.
 *
 * Listens for user interaction events and publishes an InteractionTimeout intent if no
 * interaction occurs within the configured duration.
 *
 * Implements the "inactivity timer" as a pure Flow pipeline running on a background dispatcher.
 *
 * @param navigationStateHandler Provides a Flow of the current navigation node metadata, which
 * includes a per‑screen timeout.
 * @param coroutineScope The scope in which the Flow pipeline is launched and collected. Should
 * use a background dispatcher.
 * @param dispatcher The dispatcher on which debounce/delay operations run (defaults to
 * [Dispatchers.Default]).
 * @param logger Used for debug logging.
 * @param defaultTimeout Fallback timeout if the metadata stream hasn’t emitted yet.
 */
@Immutable
@OptIn(SensitiveNavigationApi::class)
class SystemComponentIntentActor @Inject constructor(
    navigationStateHandler: NavigationStateHandler,
    private val coroutineScope: CoroutineScope,
    private val lockAcquirer: SystemComponentLockAcquirer,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val logger: Logger = navigationLogger(),
    private val defaultTimeout: Duration = 2.minutes,
) : NavigationIntentActor {

    // Channel to handle NavigationIntent
    private val mIntentFlow = Channel<NavigationIntent>(Channel.BUFFERED)
    override val sender: IntentSender = IntentSender.System
    override val defaultPriority: Int = Priorities.PRIORITY_SYSTEM_DEFAULT
    override val intentFlow: Flow<NavigationIntent> = mIntentFlow.receiveAsFlow()

    /** Emits the current screen’s timeout duration as it changes. */
    private val currentTimeout: StateFlow<Duration> =
        navigationStateHandler.currentNodeFlow
            .map { it.nodeMetaData.screenTimeoutDuration }
            .onEach { logger.d(TAG, "New timeout duration: $it") }
            .stateIn(coroutineScope, SharingStarted.Eagerly, defaultTimeout)

    /**
     * A flow that handles all user interactions (either buttons clicks, swipes, or just clicks
     * on the screen). After the interaction received, it starts the timer to determine if
     * user should be redirected to either home screen or screen saver.
     */
    private val interactionFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    init {
        launchInactivityTimerPipeline(navigationStateHandler.currentNodeFlow)
    }

    /**
     * Receives all navigation intents. On UserInteraction, simply
     * emit a signal to reset the inactivity timer.
     *
     * @param intent The incoming AppNavigationIntent to handle.
     */
    fun publishIntent(intent: SystemNavigationIntent): Boolean {
        return when (intent) {
            is SystemNavigationIntent.UserInteraction -> {
                logger.d(TAG, "User interaction received; resetting timer.")
                interactionFlow.tryEmit(Unit)
            }

            is SystemNavigationIntent.AcquireTimeoutLock -> {
                logger.d(TAG, "Acquiring timeout lock: ${intent.lockReason}")
                lockAcquirer.acquireLock(intent.lockReason)
            }

            is SystemNavigationIntent.ReleaseLock -> {
                logger.d(TAG, "Releasing timeout lock")
                lockAcquirer.releaseLock()
            }
        }
    }

    private fun publishIntent(intent: NavigationIntent) {
        mIntentFlow.trySend(intent)
    }

    /**
     * Launches the inactivity timer pipeline. This pipeline listens for user interactions and
     * screen changes, then starts a timer. If the timer expires without any interaction, it
     * publishes an [NavigationIntent.InteractionTimeout] intent.
     *
     * @param screenFlow A flow of the current [NavigationNode].
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun launchInactivityTimerPipeline(screenFlow: StateFlow<NavigationNode>) {
        merge(
            // Any user interaction
            interactionFlow,
            // Signal when the screen changes
            screenFlow
                .drop(1)          // Drops the first emission to prevent the timer
                .map { },                // map to Unit,
            lockAcquirer.lockReasonState
        )
            // Emit on start to initialize flow
            .onStart { emit(Unit) }
            .flatMapLatest {
                flow {
                    val lock = lockAcquirer.lockReasonState.value
                    if (lock == null) {
                        logger.d(TAG, "Starting inactivity timer: ${currentTimeout.value}")
                        delay(currentTimeout.value)
                        emit(Unit)
                    } else {
                        logger.d(TAG, "Inactivity timeout lock acquired: $lock")
                    }
                }
            }
            .onEach {
                logger.d(TAG, "Inactivity timeout fired after ${currentTimeout.value}")
                publishIntent(NavigationIntent.InteractionTimeout(defaultPriority))
            }
            .flowOn(dispatcher)
            .launchIn(coroutineScope)
    }

    companion object {
        private val TAG = SystemComponentIntentActor::class.java.simpleName
    }
}