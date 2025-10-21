package com.surzhykov.navsymphony.choreographer.presentation

import com.surzhykov.navsymphony.choreographer.common.NavigationIntent
import com.surzhykov.navsymphony.choreographer.common.navigationLogger
import com.surzhykov.navsymphony.choreographer.data.NavigationStateHandler
import com.surzhykov.navsymphony.domain.Logger
import com.surzhykov.navsymphony.graph.core.node.NavigationNode
import com.surzhykov.navsymphony.graph.core.node.NodeMetaData
import com.surzhykov.navsymphony.screen.core.ScreenRoute
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
class SystemComponentIntentActorTest {

    private lateinit var logger: Logger

    @Before
    fun setUp() {
        logger = spyk(Logger.Console(), recordPrivateCalls = true)
        mockkStatic(::navigationLogger)
        every { navigationLogger() } returns logger
    }

    @Test
    fun `should emit timeout after inactivity`() = runTest {
        val defaultTimeout = 2.minutes
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val testScope = CoroutineScope(testDispatcher)
        val stateFlow = MutableStateFlow(mockk<NavigationNode>(relaxed = true) {
            every { nodeMetaData } returns mockk<NodeMetaData> {
                every { screenTimeoutDuration } returns defaultTimeout
            }
        })
        val actor = SystemComponentIntentActor(
            navigationStateHandler = object : NavigationStateHandlerStub(stateFlow) {},
            coroutineScope = testScope,
            lockAcquirer = mockk(relaxed = true),
            dispatcher = testDispatcher
        )

        val emitted = mutableListOf<NavigationIntent>()
        val job = backgroundScope.launch {
            actor.intentFlow.collect { emitted.add(it) }
        }

        advanceTimeBy(defaultTimeout)
        runCurrent()

        assertTrue(
            "At least one InteractionTimeout intent should be emitted at this point",
            emitted.any { it is NavigationIntent.InteractionTimeout }
        )
        job.cancel()
    }

    @Test
    fun `should reset timer on interaction`() = runTest {
        val defaultTimeout = 2.minutes
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val testScope = CoroutineScope(testDispatcher)
        val stateFlow = MutableStateFlow(mockk<NavigationNode>(relaxed = true) {
            every { nodeMetaData } returns mockk<NodeMetaData>() {
                every { screenTimeoutDuration } returns defaultTimeout
            }
        })
        val actor = SystemComponentIntentActor(
            navigationStateHandler = object : NavigationStateHandlerStub(stateFlow) {},
            coroutineScope = testScope,
            lockAcquirer = mockk(relaxed = true),
            dispatcher = testDispatcher
        )

        val emitted = mutableListOf<NavigationIntent>()
        val job = backgroundScope.launch(testDispatcher) {
            actor.intentFlow.collect { emitted.add(it) }
        }

        advanceTimeBy(1.minutes)
        actor.publishIntent(SystemNavigationIntent.UserInteraction)
        advanceTimeBy(1.minutes)
        runCurrent()
        // No timeout yet
        assertTrue(
            "None of the intents should be emitted at this point",
            emitted.none { it is NavigationIntent.InteractionTimeout }
        )

        advanceTimeBy(1.minutes)
        runCurrent()
        // Now timeout should trigger
        assertTrue(
            "At least one InteractionTimeout intent should be emitted at this point",
            emitted.any { it is NavigationIntent.InteractionTimeout }
        )
        job.cancel()
    }

    @Test
    fun `should update timeout on screen change`() = runTest {
        val defaultTimeout = 2.minutes
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val testScope = CoroutineScope(testDispatcher)
        val mockNavigationNode = mockk<NavigationNode>(relaxed = true) {
            every { nodeMetaData } returns mockk<NodeMetaData> {
                every { screenTimeoutDuration } returns defaultTimeout
            }
        }
        val stateFlow = MutableStateFlow(mockNavigationNode)
        val actor = SystemComponentIntentActor(
            navigationStateHandler = object : NavigationStateHandlerStub(stateFlow) {},
            coroutineScope = testScope,
            lockAcquirer = mockk(relaxed = true),
            dispatcher = testDispatcher,
            logger = mockk(relaxed = true)
        )

        val emitted = mutableListOf<NavigationIntent>()
        val job = backgroundScope.launch(testDispatcher) {
            actor.intentFlow.collect { emitted.add(it) }
        }

        advanceTimeBy(1.minutes)
        // Change to a screen with longer timeout
        stateFlow.value = mockk<NavigationNode>(relaxed = true) {
            every { nodeMetaData } returns mockk<NodeMetaData> {
                every { screenTimeoutDuration } returns 3.minutes
            }
        }
        advanceTimeBy(2.minutes)
        runCurrent()
        // Still shouldn't timeout
        assertFalse(
            "None of the intents should be emitted at this point",
            emitted.any { it is NavigationIntent.InteractionTimeout }
        )

        advanceTimeBy(1.minutes)
        runCurrent()
        // Now it should
        assertTrue(
            "At least one InteractionTimeout intent should be emitted at this point",
            emitted.any { it is NavigationIntent.InteractionTimeout }
        )
        job.cancel()
    }
}

private abstract class NavigationStateHandlerStub(
    override val currentNodeFlow: MutableStateFlow<NavigationNode>,
) : NavigationStateHandler {
    override val backStackCount = MutableStateFlow(0)
    override fun append(navigationNode: NavigationNode, keepInStack: Boolean) = true
    override fun popUntil(targetRoute: KClass<out ScreenRoute>) =
        true

    override fun pop(): NavigationNode = currentNodeFlow.value
    override fun clear() = true
}
