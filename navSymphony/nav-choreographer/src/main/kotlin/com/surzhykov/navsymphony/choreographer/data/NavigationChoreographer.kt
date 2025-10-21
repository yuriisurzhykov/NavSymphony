package com.surzhykov.navsymphony.choreographer.data

import com.surzhykov.navsymphony.choreographer.R
import com.surzhykov.navsymphony.choreographer.common.IntentSender
import com.surzhykov.navsymphony.choreographer.common.IntentValidationChain
import com.surzhykov.navsymphony.choreographer.common.NavigationCommand
import com.surzhykov.navsymphony.choreographer.common.NavigationIntent
import com.surzhykov.navsymphony.choreographer.common.NavigationIntentActor
import com.surzhykov.navsymphony.choreographer.common.ValidationResult
import com.surzhykov.navsymphony.choreographer.common.navigationLogger
import com.surzhykov.navsymphony.choreographer.utils.Priorities
import com.surzhykov.navsymphony.core.presentation.DrawableResolver
import com.surzhykov.navsymphony.core.presentation.StringResolver
import com.surzhykov.navsymphony.domain.Logger
import com.surzhykov.navsymphony.domain.debounceDistinctWithin
import com.surzhykov.navsymphony.graph.core.graph.NavigationGraph
import com.surzhykov.navsymphony.graph.core.node.NavigationNode
import com.surzhykov.navsymphony.window.dialogs.PrimaryMessageDialog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retryWhen
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * The NavigationChoreographer interface is responsible for coordinating navigation actions
 * based on received navigation intents. It orchestrates the flow of navigation commands
 * to be executed by a navigation component.
 */
interface NavigationChoreographer {

    /**
     * A [Flow] of [NavigationCommand]s that the UI should execute.
     *
     * This flow emits navigation commands that are triggered by user interactions or internal
     * state changes within the application's logic. The UI observes this flow and performs the
     * corresponding navigation actions, such as navigating to a different screen or displaying
     * a dialog.
     *
     * The flow is designed to be a single source of truth for navigation requests, ensuring that
     * navigation logic is centralized and consistent.
     *
     * Example usage in the UI:
     *
     * ```kotlin
     * val navigationContext = rememberNavigationContext()
     * lifecycleScope.launchWhenStarted {
     *     viewModel.navigationCommands.collect { command ->
     *         command.execute(navigationContext)
     *     }
     * }
     * ```
     *
     * It's important to note that the flow should typically be collected in a
     * lifecycle-aware scope (e.g., `lifecycleScope.launchWhenStarted`) to avoid
     * memory leaks and unnecessary processing when the UI is not active.
     */
    val navigationCommands: Flow<NavigationCommand>

    /**
     * Initializes the navigation choreographer. This method should be called when the choreographer
     * should start processing navigation intents.
     * */
    fun initialize()

    class Base @Inject constructor(
        private val coroutineScope: CoroutineScope,
        private val navigationStateHandler: NavigationStateHandler,
        private val validationChain: IntentValidationChain,
        private val navigationGraph: NavigationGraph,
        private val transactionManager: NavigationTransactionManager,
        actors: Set<NavigationIntentActor>,
        private val logger: Logger = navigationLogger(),
        private val maxIntentRetries: Int = 3,
        debounceDuration: Duration = 70.milliseconds,
    ) : NavigationChoreographer {

        private val mutableNavigationCommandsFlow = MutableSharedFlow<NavigationCommand>(
            replay = 0,
            extraBufferCapacity = 64,
            onBufferOverflow = BufferOverflow.SUSPEND
        )

        @Volatile
        private var initializationJob: Job? = null
        private val localIntentSource =
            MutableSharedFlow<NavigationIntent>(extraBufferCapacity = 16)
        private val combinedIntentSource =
            merge(*(actors.map { it.intentFlow } + localIntentSource).toTypedArray())

        private val combinedIntentFlow = combinedIntentSource
            .debounceDistinctWithin(debounceDuration)
            .onEach { intent ->
                logger.d(TAG, "Choreographer received: $intent")
                try {
                    processIntent(intent)
                } catch (e: Exception) {
                    logger.e(TAG, "Error occurred during intent processing: ${e.message}", e)
                }
            }
            .retryWhen { cause, attempt ->
                cause is IllegalStateException && attempt < maxIntentRetries
            }
            .catch { throwable ->
                if (throwable !is CancellationException) {
                    logger.e(TAG, "Choreographer received error: ${throwable.message}", throwable)
                } else throw throwable
            }

        override val navigationCommands: SharedFlow<NavigationCommand> =
            mutableNavigationCommandsFlow.asSharedFlow()

        @Synchronized
        override fun initialize() {
            if (initializationJob == null) {
                initializationJob = combinedIntentFlow.launchIn(coroutineScope)
            }
        }

        private suspend fun processIntent(intent: NavigationIntent) {
            when (intent) {
                is NavigationIntent.NavigateTo -> proceedWithNavigationTo(intent)
                is NavigationIntent.PopUpTo -> proceedWithPopUpTo(intent)
                is NavigationIntent.Back -> proceedWithBack(intent)

                is NavigationIntent.ClearBackStack,
                is NavigationIntent.InteractionTimeout,
                    -> proceedWithClearBackStack(intent)

                is NavigationIntent.DisplayDialog -> proceedWithDisplayDialog(intent)
                is NavigationIntent.DismissOverlay -> proceedWithOverlayDismissal(intent)
                is NavigationIntent.CompleteNavTransaction -> completeNavTransaction(intent)
            }
        }

        private suspend fun proceedWithDisplayDialog(intent: NavigationIntent.DisplayDialog) {
            val node = navigationStateHandler.currentNodeFlow.value
            val validationResult = validationChain.validate(intent, node)
            processValidationResult(validationResult, intent, node)
            logger.i(TAG, "Dialog validation result: $validationResult")
        }

        private suspend fun proceedWithOverlayDismissal(intent: NavigationIntent.DismissOverlay) {
            val node = navigationStateHandler.currentNodeFlow.value
            val validationResult = validationChain.validate(intent, node)
            processValidationResult(validationResult, intent, node)
            logger.i(TAG, "Overlay dismissal validation result: $validationResult")
        }

        private suspend fun proceedWithClearBackStack(intent: NavigationIntent) {
            navigationStateHandler.clear()
            val node = navigationStateHandler.currentNodeFlow.value
            val validationResult = validationChain.validate(intent, node)
            processValidationResult(validationResult, intent, node)
            cancelPendingTransaction()
            logger.i(TAG, "Clear back stack validation result: $validationResult")
        }

        private suspend fun proceedWithBack(intent: NavigationIntent.Back) {
            val node = navigationStateHandler.pop()
            val validationResult = validationChain.validate(intent, node)
            processValidationResult(validationResult, intent, node)
            cancelPendingTransaction()
            logger.i(TAG, "Back validation result: $validationResult. Current node: $node")
        }

        private suspend fun proceedWithPopUpTo(intent: NavigationIntent.PopUpTo) {
            if (!navigationStateHandler.popUntil(intent.route::class)) {
                logger.i(TAG, "Navigation stack is already empty!")
                return
            }
            val node = navigationStateHandler.currentNodeFlow.value
            val validationResult = validationChain.validate(intent, node)
            processValidationResult(validationResult, intent, node)
            cancelPendingTransaction()
            logger.i(TAG, "Pop up to validation result: $validationResult")
        }

        private suspend fun proceedWithNavigationTo(intent: NavigationIntent.NavigateTo) {
            val node =
                navigationGraph[intent.route::class] ?: throw IllegalStateException(
                    "Requested route not found in navigation graph: ${intent.route::class}! " +
                            "Please add it to the graph to be able to proceed!"
                )
            val validationResult = validationChain.validate(intent, node)
            processValidationResult(validationResult, intent, node)
            logger.i(TAG, "Navigation to validation result: $validationResult")
        }

        private suspend fun completeNavTransaction(intent: NavigationIntent.CompleteNavTransaction) {
            if (transactionManager.isActive()) {
                try {
                    val nextIntent = transactionManager.next()
                    if (nextIntent is TransactionInfo.Continue) {
                        localIntentSource.emit(nextIntent.intent)
                    } else {
                        // TODO: This is just a temporary fix because MW has a bug with updating
                        //  the PanelUiAccess. After MW fixed, we just emits original intent to
                        //  the localIntentSource.
                        val originalIntent = nextIntent.intent
                        if (originalIntent is NavigationIntent.NavigateTo) {
                            // Add to back stack only nodes that belongs to the actual navigation
                            // between different screens.
                            val screenRoute = originalIntent.route
                            val node =
                                navigationGraph[screenRoute::class] ?: throw IllegalStateException(
                                    "Requested route not found in navigation graph: ${screenRoute::class}! " +
                                            "Please add it to the graph to be able to proceed!"
                                )
                            navigationStateHandler.append(
                                node,
                                originalIntent.navOptions.addToBackStack
                            )
                        }
                        mutableNavigationCommandsFlow.tryEmit(originalIntent.transformToCommand())
                    }
                } catch (e: Throwable) {
                    transactionManager.cancel()
                    logger.e(TAG, "Error occurred during transaction completion: ${e.message}", e)
                }
            } else {
                throw IllegalStateException(
                    "Navigation transaction is not active! Unable to finish transaction " +
                            "for route: ${intent.route} published by ${intent.sender}"
                )
            }
        }

        private fun cancelPendingTransaction() {
            if (transactionManager.isActive()) {
                transactionManager.cancel()
            }
        }

        private suspend fun processValidationResult(
            validationResult: ValidationResult,
            intent: NavigationIntent,
            node: NavigationNode,
        ) {
            when (validationResult) {
                is ValidationResult.Valid -> {
                    // Add to back stack only nodes that belongs to the actual navigation
                    // between different screens.
                    if (intent is NavigationIntent.NavigateTo) {
                        navigationStateHandler.append(node, intent.navOptions.addToBackStack)
                    }
                    mutableNavigationCommandsFlow.tryEmit(intent.transformToCommand())
                }

                is ValidationResult.Invalid -> {
                    logger.e(TAG, "Navigation validation failed: $validationResult")
                    // If any kind of error occurred, we can create a dialog and display an error
                    // message.
                    val dialog = PrimaryMessageDialog(
                        iconResolver = DrawableResolver.Resource(R.drawable.ic_dialog_error),
                        titleResolver = StringResolver.from(R.string.error_message_unknown),
                        messageResolver = StringResolver.from(validationResult.message)
                    )
                    val errorDialogIntent = NavigationIntent.DisplayDialog(
                        dialog = dialog,
                        sender = IntentSender.System,
                        priority = Priorities.PRIORITY_SYSTEM_DEFAULT
                    )
                    localIntentSource.emit(errorDialogIntent)
                }

                is ValidationResult.Redirect -> {
                    logger.w(TAG, "Navigation validation redirect: $validationResult")
                    transactionManager.cancel()
                    transactionManager.applyTransaction(validationResult.redirectBuilder.build())
                    localIntentSource.emit(transactionManager.next().intent)
                }

                is ValidationResult.Ignore -> {
                    logger.i(TAG, "Navigation validation ignored: $intent")
                }
            }
        }

        companion object {
            private val TAG = NavigationChoreographer::class.java.simpleName
        }
    }
}