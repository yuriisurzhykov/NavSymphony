package com.surzhykov.navsymphony.choreographer.presentation

import com.surzhykov.navsymphony.choreographer.common.IntentSender
import com.surzhykov.navsymphony.choreographer.common.NavigationIntent
import com.surzhykov.navsymphony.choreographer.common.NavigationOptions
import com.surzhykov.navsymphony.choreographer.utils.Priorities
import com.surzhykov.navsymphony.screen.core.ScreenRoute
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserComponentIntentActorTest {

    @Test
    fun `verify navigateTo method`() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val actor = UserComponentIntentActor()
        val intents = mutableListOf<NavigationIntent>()
        backgroundScope.launch(testDispatcher) {
            actor.intentFlow.collect { intents.add(it) }
        }
        advanceUntilIdle()

        val route = mockk<ScreenRoute>()
        actor.navigate(route)

        val expectedIntent = NavigationIntent.NavigateTo(
            route,
            NavigationOptions(),
            IntentSender.User,
            Priorities.PRIORITY_USER_DEFAULT
        )
        assertTrue("Intents suppose to be not empty", intents.isNotEmpty())
        assertEquals(expectedIntent, intents.first())
    }

    @Test
    fun `verify navigateBack method publishes Back intent`() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val actor = UserComponentIntentActor()
        val intents = mutableListOf<NavigationIntent>()
        backgroundScope.launch(testDispatcher) {
            actor.intentFlow.collect { intents.add(it) }
        }
        advanceUntilIdle()

        actor.navigateBack()

        val expectedIntent = NavigationIntent.Back(
            IntentSender.User,
            Priorities.PRIORITY_USER_DEFAULT
        )
        assertTrue("Intents suppose to be not empty", intents.isNotEmpty())
        assertTrue("Intents suppose to be Back", intents.first() is NavigationIntent.Back)
        assertEquals(expectedIntent.sender, intents.first().sender)
        assertEquals(expectedIntent.priority, intents.first().priority)
    }

    @Test
    fun `verify clearBackStack publishes ClearBackStack intent`() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val actor = UserComponentIntentActor()
        val intents = mutableListOf<NavigationIntent>()
        backgroundScope.launch(testDispatcher) {
            actor.intentFlow.collect { intents.add(it) }
        }
        advanceUntilIdle()

        actor.clearBackStack()

        val expectedIntent = NavigationIntent.ClearBackStack(
            IntentSender.User,
            Priorities.PRIORITY_USER_DEFAULT
        )
        assertTrue("Intents suppose to be not empty", intents.isNotEmpty())
        assertTrue(
            "Intents suppose to be ClearBackStack",
            intents.first() is NavigationIntent.ClearBackStack
        )
        assertEquals(expectedIntent.sender, intents.first().sender)
        assertEquals(expectedIntent.priority, intents.first().priority)
    }

    @Test
    fun `verify navigateBackTo method publishes PopUpTo intent`() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val actor = UserComponentIntentActor()
        val intents = mutableListOf<NavigationIntent>()

        backgroundScope.launch(testDispatcher) {
            actor.intentFlow.collect { intents.add(it) }
        }
        advanceUntilIdle()
        val route = mockk<ScreenRoute>()
        val inclusive = true
        actor.navigateBackTo(route, inclusive)
        val expectedIntent = NavigationIntent.PopUpTo(
            route,
            inclusive,
            IntentSender.User,
            Priorities.PRIORITY_USER_DEFAULT
        )
        assertTrue("Intents suppose to be not empty", intents.isNotEmpty())
        assertEquals(expectedIntent, intents.first())
    }

    @Test
    @OptIn(SensitiveNavigationApi::class)
    fun `verify completeNavTransactionStep method publishes CompleteNavTransaction intent`() =
        runTest {
            val testDispatcher = UnconfinedTestDispatcher(testScheduler)
            val actor = UserComponentIntentActor()
            val intents = mutableListOf<NavigationIntent>()

            backgroundScope.launch(testDispatcher) {
                actor.intentFlow.collect { intents.add(it) }
            }
            advanceUntilIdle()
            val route = mockk<ScreenRoute>()
            actor.completeNavTransactionStep(route)
            val expectedIntent = NavigationIntent.CompleteNavTransaction(route)
            assertTrue("Intents suppose to be not empty", intents.isNotEmpty())
            assertEquals(expectedIntent, intents.first())
        }
}