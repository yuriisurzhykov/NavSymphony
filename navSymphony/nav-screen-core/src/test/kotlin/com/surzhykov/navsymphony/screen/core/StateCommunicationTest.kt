package com.surzhykov.navsymphony.screen.core

import kotlinx.coroutines.test.runTest
import org.junit.Test

class StateCommunicationTest {

    @Test
    fun `test state updates flow on update call`() = runTest {
        val communication = StateCommunication.Base(TestScreenState())
        val expected = TestScreenState("updated")
        communication.update { expected }
        val actual = communication.state().value
        assert(actual == expected)
    }

    @Test
    fun `test state flow on state call`() = runTest {
        val communication = StateCommunication.Base(TestScreenState())
        val expected = TestScreenState("initial")
        val actual = communication.state().value
        assert(actual == expected)
    }

    private data class TestScreenState(
        val value: String = "initial",
    ) : ScreenState
}