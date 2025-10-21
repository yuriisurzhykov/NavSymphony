package com.surzhykov.navsymphony.choreographer.data

import com.surzhykov.navsymphony.choreographer.common.IntentSender
import com.surzhykov.navsymphony.choreographer.common.IntentValidationChain
import com.surzhykov.navsymphony.choreographer.common.NavigationCommand
import com.surzhykov.navsymphony.choreographer.common.NavigationIntent
import com.surzhykov.navsymphony.choreographer.common.NavigationIntentActor
import com.surzhykov.navsymphony.choreographer.common.NavigationOptions
import com.surzhykov.navsymphony.choreographer.common.ValidationResult
import com.surzhykov.navsymphony.choreographer.common.navigationLogger
import com.surzhykov.navsymphony.choreographer.presentation.NavigationContext
import com.surzhykov.navsymphony.core.presentation.DrawableResolver
import com.surzhykov.navsymphony.core.presentation.StringResolver
import com.surzhykov.navsymphony.domain.Logger
import com.surzhykov.navsymphony.graph.core.graph.NavigationGraph
import com.surzhykov.navsymphony.graph.core.node.NavigationNode
import com.surzhykov.navsymphony.screen.core.ScreenRoute
import com.surzhykov.navsymphony.window.core.AbstractOverlayWindow
import com.surzhykov.navsymphony.window.core.OverlayManager
import com.surzhykov.navsymphony.window.dialogs.PrimaryMessageDialog
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.reflect.KClass

@OptIn(ExperimentalCoroutinesApi::class)
class NavigationChoreographerTest {

    private lateinit var logger: Logger

    @Before
    fun setUp() {
        logger = spyk(Logger.Console(), recordPrivateCalls = true)
        mockkStatic(::navigationLogger)
        every { navigationLogger() } returns logger
    }

    @Test
    fun `verify initialization happens only once`() = runTest {
        val choreographer = spyk(
            objToCopy = NavigationChoreographer.Base(
                coroutineScope = CoroutineScope(UnconfinedTestDispatcher()),
                navigationStateHandler = mockk(),
                validationChain = mockk(),
                navigationGraph = mockk(),
                transactionManager = mockk(),
                actors = emptySet(),
                logger = logger
            ),
            recordPrivateCalls = true
        )

        // when
        choreographer.initialize()
        val firstJob = getInternalJob(choreographer)
        choreographer.initialize()
        val secondJob = getInternalJob(choreographer)

        // then
        assertNotNull("First job must be initialized", firstJob)
        assertSame("Initialization must be idempotent — same job reused", firstJob, secondJob)
    }

    @Test
    fun `verify NavigateTo command is emitted when intent is valid`() = runTest {
        val localIntentSource = MutableSharedFlow<NavigationIntent>(extraBufferCapacity = 64)
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val testScope = CoroutineScope(testDispatcher)
        val localIntentActor: NavigationIntentActor = mockk<NavigationIntentActor>(relaxed = true) {
            coEvery { intentFlow } returns localIntentSource
        }
        val choreographer = mockChoreographer(setOf(localIntentActor), testScope)
        choreographer.initialize()

        val intentSender = IntentSender.System
        val priority = 10
        val navOptions = NavigationOptions()

        val intent = NavigationIntent.NavigateTo(
            route = mockk<ScreenRoute>(),
            navOptions = navOptions,
            sender = intentSender,
            priority = priority
        )

        val navigationCommands = mutableListOf<NavigationCommand>()
        backgroundScope.launch(testDispatcher) {
            choreographer.navigationCommands.collect { navigationCommands.add(it) }
        }
        localIntentSource.emit(intent)
        assertEquals(1, navigationCommands.size)
        assertEquals(intent.transformToCommand(), navigationCommands[0])
    }

    @Test
    fun `verify throws exception when navigation graph doesn't have route`() = runTest {
        val localIntentSource = MutableSharedFlow<NavigationIntent>(extraBufferCapacity = 64)
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val testScope = CoroutineScope(testDispatcher)
        val localIntentActor: NavigationIntentActor = mockk<NavigationIntentActor>(relaxed = true) {
            coEvery { intentFlow } returns localIntentSource
        }
        val navigationStateHandler = mockk<NavigationStateHandler>(relaxed = true)
        val navigationGraph = mockk<NavigationGraph>(relaxed = true) {
            every { rootClass } returns mockk(relaxed = true)
            coEvery { this@mockk[any<KClass<out ScreenRoute>>()] } returns null
        }
        val choreographer = mockChoreographer(
            actors = setOf(localIntentActor),
            scope = testScope,
            navigationGraph = navigationGraph,
            navigationStateHandler = navigationStateHandler
        )
        choreographer.initialize()

        val commands = mutableListOf<NavigationCommand>()
        backgroundScope.launch(testDispatcher) {
            choreographer.navigationCommands.collect { commands.add(it) }
        }
        advanceUntilIdle()

        val intentSender = IntentSender.System
        val priority = 10
        val intent = NavigationIntent.NavigateTo(
            route = mockk<ScreenRoute>(),
            navOptions = NavigationOptions(),
            sender = intentSender,
            priority = priority
        )
        localIntentSource.emit(intent)
        advanceUntilIdle()
        assertEquals(0, commands.size)
        val intentRoute = intent.route::class.toString()
        verify(exactly = 1) {
            logger.e(
                "NavigationChoreographer",
                "Error occurred during intent processing: Requested route not found in navigation graph: $intentRoute! Please add it to the graph to be able to proceed!",
                any()
            )
        }
    }

    @Test
    fun `verify Back command is emitted when intent is valid`() = runTest {
        val localIntentSource = MutableSharedFlow<NavigationIntent>(extraBufferCapacity = 64)
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val testScope = CoroutineScope(testDispatcher)
        val localIntentActor: NavigationIntentActor = mockk<NavigationIntentActor>(relaxed = true) {
            coEvery { intentFlow } returns localIntentSource
        }
        val mockNode = mockk<NavigationNode>(relaxed = true)
        val navigationGraph = mockk<NavigationGraph>(relaxed = true) {
            coEvery { this@mockk[any<KClass<out ScreenRoute>>()] } returns mockNode
        }
        val navigationBackStack = mockk<NavigationBackStack<NavigationNode>>(relaxed = true) {
            every { pop() } returns mockNode
        }
        val navigationStateHandler = spyk(
            NavigationStateHandler.Base(
                navigationGraph,
                testScope,
                navigationBackStack,
                logger
            )
        ) {
            every { pop() } returns mockNode
        }
        val choreographer = mockChoreographer(
            actors = setOf(localIntentActor),
            scope = testScope,
            navigationGraph = navigationGraph,
            navigationBackStack = navigationBackStack,
            navigationStateHandler = navigationStateHandler
        )
        choreographer.initialize()

        val commands = mutableListOf<NavigationCommand>()
        backgroundScope.launch(testDispatcher) {
            choreographer.navigationCommands.collect { commands.add(it) }
        }
        advanceUntilIdle()

        val intentSender = IntentSender.System
        val priority = 10
        val intent = NavigationIntent.Back(
            sender = intentSender,
            priority = priority
        )
        localIntentSource.emit(intent)
        advanceUntilIdle()

        assertEquals("At least one command should be emitted", 1, commands.size)
        assertEquals(intent.transformToCommand(), commands[0])
    }

    @Test
    fun `verify throws exception when trying to complete transaction on CancelNavTransaction intent`() =
        runTest {
            val localIntentSource = MutableSharedFlow<NavigationIntent>(extraBufferCapacity = 64)
            val testDispatcher = UnconfinedTestDispatcher(testScheduler)
            val testScope = CoroutineScope(testDispatcher)
            val transactionManager = mockk<NavigationTransactionManager>(relaxed = true) {
                every { isActive() } returns false
            }
            val localIntentActor: NavigationIntentActor =
                mockk<NavigationIntentActor>(relaxed = true) {
                    coEvery { intentFlow } returns localIntentSource
                }
            val choreographer =
                mockChoreographer(setOf(localIntentActor), testScope, transactionManager)
            choreographer.initialize()
            val intent = NavigationIntent.CompleteNavTransaction(
                route = mockk<ScreenRoute>(relaxed = true)
            )
            localIntentSource.emit(intent)

            advanceUntilIdle()

            verify(exactly = 1) { transactionManager.isActive() }
            val intentRoute = intent.route.toString()
            val intentSender = intent.sender.toString()
            verify(exactly = 1) {
                logger.e(
                    "NavigationChoreographer",
                    "Error occurred during intent processing: Navigation transaction is not active! Unable to finish transaction for route: $intentRoute published by $intentSender",
                    any()
                )
            }
        }

    @Test
    fun `verify emits next intent when complete transaction on CompleteNavTransaction intent`() =
        runTest {
            val localIntentSource = MutableSharedFlow<NavigationIntent>(extraBufferCapacity = 64)
            val testDispatcher = UnconfinedTestDispatcher(testScheduler)
            val testScope = CoroutineScope(testDispatcher)
            // Create intent for transaction manager
            val mockTransformCommand = mockk<NavigationCommand>(relaxed = true)
            val mockTransactionIntent = mockk<NavigationIntent>(relaxed = true) {
                every { transformToCommand() } returns mockTransformCommand
            }
            // Create transaction manager with mocked intents
            val transactionManager = mockk<NavigationTransactionManager>(relaxed = true) {
                every { isActive() } returns true
                every { next() } returns TransactionInfo.Continue(mockTransactionIntent)
            }
            val localIntentActor = mockk<NavigationIntentActor>(relaxed = true) {
                coEvery { intentFlow } returns localIntentSource
            }
            val choreographer =
                mockChoreographer(setOf(localIntentActor), testScope, transactionManager)
            choreographer.initialize()

            // Initialize the background scope and collect navigation commands
            // before publishing intents.
            val commands = mutableListOf<NavigationCommand>()
            backgroundScope.launch(testDispatcher) {
                choreographer.navigationCommands.collect {
                    commands.add(it)
                }
            }
            // Finish the scope launching
            advanceUntilIdle()

            // Verify the behavior
            val intent = NavigationIntent.CompleteNavTransaction(
                route = mockk<ScreenRoute>(relaxed = true)
            )
            localIntentSource.emit(intent)

            // Wait until all tasks are completed
            advanceUntilIdle()

            // The whole point is in that after TransactionManager returns Continue, the intent
            // is processed by NavigationChoreographer with all validation loop, and it happens,
            // that NavigationChoreographer must emit a single command but it should check two
            // times that transaction manager is active.
            verify(exactly = 2) { transactionManager.isActive() }
            assertEquals(1, commands.size)
            assertEquals(mockTransformCommand, commands[0])
        }

    @Test
    fun `verify executes intent when complete transaction on CompleteNavTransaction intent with BackToOriginal result`() =
        runTest {
            val localIntentSource = MutableSharedFlow<NavigationIntent>(extraBufferCapacity = 64)
            val testDispatcher = UnconfinedTestDispatcher(testScheduler)
            val testScope = CoroutineScope(testDispatcher)
            // Create intent for transaction manager
            val mockTransformCommand = mockk<NavigationCommand>(relaxed = true)
            val mockTransactionIntent = mockk<NavigationIntent>(relaxed = true) {
                every { transformToCommand() } returns mockTransformCommand
            }
            // Create transaction manager with mocked intents
            val transactionManager = mockk<NavigationTransactionManager>(relaxed = true) {
                every { isActive() } returns true
                every { next() } returns TransactionInfo.BackToOriginal(mockTransactionIntent)
            }
            val localIntentActor = mockk<NavigationIntentActor>(relaxed = true) {
                coEvery { intentFlow } returns localIntentSource
            }
            val choreographer =
                mockChoreographer(setOf(localIntentActor), testScope, transactionManager)
            choreographer.initialize()

            // Initialize the background scope and collect navigation commands
            // before publishing intents.
            val commands = mutableListOf<NavigationCommand>()
            backgroundScope.launch(testDispatcher) {
                choreographer.navigationCommands.collect {
                    commands.add(it)
                }
            }
            // Finish the scope launching
            advanceUntilIdle()

            // Verify the behavior
            val intent = NavigationIntent.CompleteNavTransaction(
                route = mockk<ScreenRoute>(relaxed = true)
            )
            localIntentSource.emit(intent)

            // Wait until all tasks are completed
            advanceUntilIdle()

            // The whole point is in that after TransactionManager returns BackToOriginal intent,
            // no more validation performed by NavigationChoreographer. It proceeds to the intent's
            // command immediately, without another loop of transactions.
            verify(exactly = 1) { transactionManager.isActive() }
            assertEquals(1, commands.size)
            assertEquals(mockTransformCommand, commands[0])
        }

    @Test
    fun `verify cancel transaction when fail to CompleteNavTransaction`() = runTest {
        val localIntentSource = MutableSharedFlow<NavigationIntent>(extraBufferCapacity = 64)
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val testScope = CoroutineScope(testDispatcher)
        val localIntentActor: NavigationIntentActor = mockk<NavigationIntentActor>(relaxed = true) {
            coEvery { intentFlow } returns localIntentSource
        }
        val transactionManager = mockk<NavigationTransactionManager>(relaxed = true) {
            every { isActive() } returns true
            every { next() } throws RuntimeException()
        }
        val choreographer = mockChoreographer(
            actors = setOf(localIntentActor),
            scope = testScope,
            transactionManager = transactionManager
        )
        choreographer.initialize()
        val commands = mutableListOf<NavigationCommand>()
        backgroundScope.launch(testDispatcher) {
            choreographer.navigationCommands.collect { commands.add(it) }
        }
        advanceUntilIdle()
        val intent = NavigationIntent.CompleteNavTransaction(
            route = mockk<ScreenRoute>(relaxed = true)
        )
        localIntentSource.emit(intent)
        advanceUntilIdle()
        assertEquals(0, commands.size)
        verify(exactly = 1) { transactionManager.isActive() }
        verify(exactly = 1) { transactionManager.cancel() }
    }

    @Test
    fun `verify InteractionTimeout clears back stack`() = runTest {
        val localIntentSource = MutableSharedFlow<NavigationIntent>(extraBufferCapacity = 64)
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val testScope = CoroutineScope(testDispatcher)
        val localIntentActor: NavigationIntentActor = mockk<NavigationIntentActor>(relaxed = true) {
            coEvery { intentFlow } returns localIntentSource
        }

        // Create all resources required for NavigationChoreographer to be verified
        val transactionManager = mockk<NavigationTransactionManager>(relaxed = true) {
            every { isActive() } returns true
        }
        val navigationGraph = mockk<NavigationGraph>(relaxed = true) {
            coEvery { this@mockk[any<KClass<out ScreenRoute>>()] } returns mockk<NavigationNode>(
                relaxed = true
            )
        }
        val navigationBackStack = spyk(NavigationBackStack<NavigationNode>()) {
            every { pop() } returns mockk(relaxed = true)
        }
        val stateHandler = spyk(
            NavigationStateHandler.Base(
                navigationGraph,
                testScope,
                navigationBackStack,
                logger
            )
        )
        val choreographer = mockChoreographer(
            actors = setOf(localIntentActor),
            scope = testScope,
            transactionManager = transactionManager,
            navigationStateHandler = stateHandler
        )
        choreographer.initialize()

        // Initialize the background scope and collect navigation commands
        val navigationCommands = mutableListOf<NavigationCommand>()
        backgroundScope.launch(testDispatcher) {
            choreographer.navigationCommands.collect { navigationCommands.add(it) }
        }
        advanceUntilIdle()

        // Publish intent and verify the behavior
        val intent = NavigationIntent.InteractionTimeout(
            priority = 10,
        )
        localIntentSource.emit(intent)
        assertEquals(1, navigationCommands.size)
        // Verify clear back stack command emitted
        assertEquals(intent.transformToCommand(), navigationCommands[0])
        // Verify that state handler and transaction manager are called and cancelled
        verify(exactly = 1) { stateHandler.clear() }
        verify(exactly = 1) { transactionManager.cancel() }
    }

    @Test
    fun `verify ClearBackStack intent actually clears back stack`() = runTest {
        val localIntentSource = MutableSharedFlow<NavigationIntent>(extraBufferCapacity = 64)
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val testScope = CoroutineScope(testDispatcher)

        val localIntentActor: NavigationIntentActor = mockk<NavigationIntentActor>(relaxed = true) {
            coEvery { intentFlow } returns localIntentSource
        }

        val choreographer = mockChoreographer(actors = setOf(localIntentActor), scope = testScope)
        choreographer.initialize()

        val commands = mutableListOf<NavigationCommand>()
        backgroundScope.launch(testDispatcher) {
            choreographer.navigationCommands.collect { commands.add(it) }
        }
        advanceUntilIdle()

        val intent = NavigationIntent.ClearBackStack(
            sender = IntentSender.User,
            priority = 10,
            logger = logger
        )
        localIntentSource.emit(intent)
        advanceUntilIdle()

        assertEquals("Expected at least one command being emitted", 1, commands.size)
        assertEquals(intent.transformToCommand(), commands[0])
    }

    @Test
    fun `verify PopUpTo do nothing if back stack is empty or route not found`() = runTest {
        val localIntentSource = MutableSharedFlow<NavigationIntent>(extraBufferCapacity = 64)
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val testScope = CoroutineScope(testDispatcher)

        val localIntentActor: NavigationIntentActor = mockk<NavigationIntentActor>(relaxed = true) {
            coEvery { intentFlow } returns localIntentSource
        }
        val navigationGraph = mockk<NavigationGraph>(relaxed = true) {
            coEvery { this@mockk[any<KClass<out ScreenRoute>>()] } returns mockk<NavigationNode>(
                relaxed = true
            )
        }
        val navigationStateHandler = spyk(
            NavigationStateHandler.Base(
                navigationGraph,
                testScope,
                NavigationBackStack(),
                logger
            )
        ) {
            every { popUntil(any()) } returns false
        }

        val choreographer = mockChoreographer(
            actors = setOf(localIntentActor),
            scope = testScope,
            navigationGraph = navigationGraph,
            navigationStateHandler = navigationStateHandler
        )
        choreographer.initialize()
        val intent = NavigationIntent.PopUpTo(
            route = mockk<ScreenRoute>(),
            inclusive = true,
            priority = 10,
            sender = IntentSender.User
        )

        val commands = mutableListOf<NavigationCommand>()
        backgroundScope.launch(testDispatcher) {
            choreographer.navigationCommands.collect { commands.add(it) }
        }
        advanceUntilIdle()

        localIntentSource.emit(intent)
        advanceUntilIdle()
        assertEquals(0, commands.size)
        verify(exactly = 1) { navigationStateHandler.popUntil(any()) }
        verify(exactly = 0) { navigationStateHandler.currentNodeFlow }
    }

    @Test
    fun `verify PopUpTo clears back stack`() = runTest {
        val localIntentSource = MutableSharedFlow<NavigationIntent>(extraBufferCapacity = 64)
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val testScope = CoroutineScope(testDispatcher)

        val localIntentActor: NavigationIntentActor = mockk<NavigationIntentActor>(relaxed = true) {
            coEvery { intentFlow } returns localIntentSource
        }
        val navigationGraph = mockk<NavigationGraph>(relaxed = true) {
            coEvery { this@mockk[any<KClass<out ScreenRoute>>()] } returns mockk<NavigationNode>(
                relaxed = true
            )
        }
        val mockNode = mockk<NavigationNode>(relaxed = true)
        val mockStateFlow = spyk(MutableStateFlow(mockNode)) {
            every { value } returns mockNode
        }
        val navigationStateHandler = spyk(
            NavigationStateHandler.Base(
                navigationGraph,
                testScope,
                NavigationBackStack(),
                logger
            )
        ) {
            every { popUntil(any()) } returns true
            every { currentNodeFlow } returns mockStateFlow
        }

        val choreographer = mockChoreographer(
            actors = setOf(localIntentActor),
            scope = testScope,
            navigationGraph = navigationGraph,
            navigationStateHandler = navigationStateHandler
        )
        choreographer.initialize()
        val intent = NavigationIntent.PopUpTo(
            route = mockk<ScreenRoute>(),
            inclusive = true,
            priority = 10,
            sender = IntentSender.User
        )

        val commands = mutableListOf<NavigationCommand>()
        backgroundScope.launch(testDispatcher) {
            choreographer.navigationCommands.collect { commands.add(it) }
        }
        advanceUntilIdle()

        localIntentSource.emit(intent)
        advanceUntilIdle()

        verify(exactly = 1) { navigationStateHandler.popUntil(any()) }
        verify(exactly = 1) { navigationStateHandler.currentNodeFlow }
        assertEquals(1, commands.size)
        assertEquals(intent.transformToCommand(), commands[0])
    }

    @Test
    fun `verify DisplayDialog intent sends dialog command`() = runTest {
        val localIntentSource = MutableSharedFlow<NavigationIntent>(extraBufferCapacity = 64)
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val testScope = CoroutineScope(testDispatcher)
        val localIntentActor: NavigationIntentActor = mockk<NavigationIntentActor>(relaxed = true) {
            coEvery { intentFlow } returns localIntentSource
        }

        val choreographer = mockChoreographer(setOf(localIntentActor), testScope)
        choreographer.initialize()

        val commands = mutableListOf<NavigationCommand>()
        backgroundScope.launch(testDispatcher) {
            choreographer.navigationCommands.collect { commands.add(it) }
        }
        advanceUntilIdle()

        val dialogWindow = PrimaryMessageDialog(
            iconResolver = DrawableResolver.Empty(),
            titleResolver = StringResolver.from("Test message"),
            messageResolver = null
        )

        val intent = NavigationIntent.DisplayDialog(
            dialog = dialogWindow,
            sender = IntentSender.System,
            priority = 10
        )
        localIntentSource.emit(intent)
        advanceUntilIdle()

        assertEquals(1, commands.size)
        assertEquals(intent.transformToCommand(), commands[0])
    }

    @Test
    fun `verify displays overlay on Invalid intent validation result`() = runTest {
        val localIntentSource = MutableSharedFlow<NavigationIntent>(extraBufferCapacity = 64)
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val testScope = CoroutineScope(testDispatcher)

        val invalidResultMock = ValidationResult.Invalid("Invalid Result message")
        val intentValidationChain = mockk<IntentValidationChain>(relaxed = true) {
            coEvery { validate(any(), any()) } returns ValidationResult.Valid
            coEvery {
                validate(
                    ofType(NavigationIntent.InteractionTimeout::class),
                    any()
                )
            } returns invalidResultMock
        }
        val localIntentActor: NavigationIntentActor = mockk<NavigationIntentActor>(relaxed = true) {
            coEvery { intentFlow } returns localIntentSource
        }
        val choreographer = mockChoreographer(
            actors = setOf(localIntentActor),
            scope = testScope,
            validationChain = intentValidationChain
        )

        val commands = mutableListOf<NavigationCommand>()
        backgroundScope.launch(testDispatcher) {
            choreographer.navigationCommands.collect { commands.add(it) }
        }
        advanceUntilIdle()

        choreographer.initialize()

        val intent = NavigationIntent.InteractionTimeout(1)
        localIntentSource.emit(intent)
        advanceUntilIdle()

        // Verify that the error dialog is displayed

        val mockOverlayManager = mockk<OverlayManager>(relaxed = true)
        val mockContext = mockk<NavigationContext>(relaxed = true) {
            every { overlayManager } returns mockOverlayManager
        }
        assertTrue("Commands list should not be empty", commands.isNotEmpty())
        commands[0].execute(mockContext)
        val capturedDialog = slot<AbstractOverlayWindow>()
        verify(exactly = 1) { mockOverlayManager.showOverlay(capture(capturedDialog)) }

        val expectedDialog = PrimaryMessageDialog(
            iconResolver = DrawableResolver.Empty(),
            titleResolver = StringResolver.from("Unknown Error"),
            messageResolver = StringResolver.from("Invalid Result message")
        )

        assertTrue(
            "Dialog content should match",
            (capturedDialog.captured as PrimaryMessageDialog).contentEquals(expectedDialog)
        )
    }

    @Test
    fun `verify applies transaction on Redirect intent validation result`() = runTest {
        val localIntentSource = MutableSharedFlow<NavigationIntent>(extraBufferCapacity = 64)
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val testScope = CoroutineScope(testDispatcher)
        val transactionManager = spyk(NavigationTransactionManager.Base())
        val backStack = spyk(NavigationBackStack<NavigationNode>())
        val localIntentActor: NavigationIntentActor = mockk<NavigationIntentActor>(relaxed = true) {
            coEvery { intentFlow } returns localIntentSource
        }

        val intentValidationChain = mockk<IntentValidationChain>(relaxed = true) {
            coEvery { validate(any(), any()) } returns ValidationResult.Valid
            coEvery {
                validate(
                    ofType(NavigationIntent.NavigateTo::class),
                    any()
                )
            } returns ValidationResult.Redirect(mockk(relaxed = true), mockk(relaxed = true))
        }
        val choreographer = mockChoreographer(
            actors = setOf(localIntentActor),
            scope = testScope,
            navigationBackStack = backStack,
            transactionManager = transactionManager,
            validationChain = intentValidationChain
        )
        choreographer.initialize()
        val intent = NavigationIntent.NavigateTo(
            route = mockk<ScreenRoute>(),
            navOptions = NavigationOptions(),
            sender = IntentSender.System,
            priority = 10
        )
        localIntentSource.emit(intent)
        advanceUntilIdle()
        verify(exactly = 1) { transactionManager.cancel() }
        verify(exactly = 1) { transactionManager.applyTransaction(any()) }
    }

    private fun getInternalJob(choreographer: NavigationChoreographer.Base): Job? {
        val field = NavigationChoreographer.Base::class.java.getDeclaredField("initializationJob")
        field.isAccessible = true
        return field.get(choreographer) as? Job
    }

    private fun mockChoreographer(
        actors: Set<NavigationIntentActor>,
        scope: CoroutineScope,
        transactionManager: NavigationTransactionManager = mockk<NavigationTransactionManager>(
            relaxed = true
        ),
        validationChain: IntentValidationChain = mockk<IntentValidationChain>(relaxed = true) {
            coEvery { validate(any(), any()) } returns ValidationResult.Valid
        },
        navigationGraph: NavigationGraph = mockk<NavigationGraph>(relaxed = true) {
            coEvery { this@mockk[any<KClass<out ScreenRoute>>()] } returns mockk<NavigationNode>(
                relaxed = true
            )
        },
        navigationBackStack: NavigationBackStack<NavigationNode> = spyk(NavigationBackStack()) {
            every { pop() } returns mockk(relaxed = true)
        },
        navigationStateHandler: NavigationStateHandler =
            spyk(NavigationStateHandler.Base(navigationGraph, scope, navigationBackStack, logger)),
    ): NavigationChoreographer {
        return spyk(
            objToCopy = NavigationChoreographer.Base(
                coroutineScope = scope,
                navigationStateHandler = navigationStateHandler,
                validationChain = validationChain,
                navigationGraph = navigationGraph,
                transactionManager = transactionManager,
                actors = actors,
                logger = logger
            ),
            recordPrivateCalls = true
        )
    }

    private fun PrimaryMessageDialog.contentEquals(other: PrimaryMessageDialog): Boolean {
        val iconField = PrimaryMessageDialog::class.java.getDeclaredField("iconResolver")
        iconField.isAccessible = true
        val titleField = PrimaryMessageDialog::class.java.getDeclaredField("titleResolver")
        titleField.isAccessible = true
        val messageField = PrimaryMessageDialog::class.java.getDeclaredField("messageResolver")
        messageField.isAccessible = true

        return iconField.get(this) == iconField.get(other) &&
                titleField.get(this) == titleField.get(other) &&
                messageField.get(this) == messageField.get(other)
    }
}