package com.surzhykov.navsymphony.screen.core

import com.surzhykov.navsymphony.domain.Dispatcher
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ScreenStateViewModelTest {

    @Test
    fun `verify initial state emitted by viewModel`() = runTest {
        val dispatcher = TestDispatcher()
        val viewModel = TestViewModel(dispatcher)
        val expected = TestScreenState()
        val actual = viewModel.screenState.first()
        assertEquals(expected, actual)
    }

    @Test
    fun `verify intent processing`() = runTest {
        val dispatcher = TestDispatcher()
        val viewModel = TestViewModel(dispatcher)
        val intent = TestScreenIntent.TestIntent2("test")
        viewModel.onIntent(intent)
        val actual = viewModel.receivedIntent
        assertEquals(intent, actual)
    }

    @Test
    fun `verify backpressure`() = runTest {
        val dispatcher = TestDispatcher()
        val viewModel = TestViewModel(dispatcher)
        repeat(1_000_000) {
            viewModel.onIntent(TestScreenIntent.TestIntent1)
        }
        assertEquals(1_000_000, viewModel.processIntentCalls)
    }

    @Test
    fun `verify state update`() = runTest {
        val dispatcher = TestDispatcher()
        val viewModel = TestViewModel(dispatcher)
        viewModel.setNewState(TestScreenState("updated"))
        val actual = viewModel.screenState.value
        assertEquals("updated", actual.value)
    }

    private data class TestScreenState(
        val value: String = "initial",
        val isLoading: Boolean = false,
    ) : ScreenState

    private sealed class TestScreenIntent : ScreenIntent {
        data object TestIntent1 : TestScreenIntent()
        data class TestIntent2(val value: String) : TestScreenIntent()
    }

    private class TestViewModel(
        dispatcher: Dispatcher,
        communication: StateCommunication.Mutable<TestScreenState> = StateCommunication.Base(
            TestScreenState()
        ),
    ) : ScreenStateViewModel<TestScreenState, TestScreenIntent>(communication, dispatcher) {

        var processIntentCalls: Int = 0
        lateinit var receivedIntent: TestScreenIntent

        override fun processIntent(intent: TestScreenIntent) {
            processIntentCalls++
            receivedIntent = intent
        }

        fun setNewState(state: TestScreenState) = updateState { state }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private class TestDispatcher :
        Dispatcher.Abstract(UnconfinedTestDispatcher(), UnconfinedTestDispatcher())
}