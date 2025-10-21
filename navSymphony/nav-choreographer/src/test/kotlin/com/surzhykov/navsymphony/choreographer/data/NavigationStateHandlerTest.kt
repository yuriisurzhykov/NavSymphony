package com.surzhykov.navsymphony.choreographer.data

import com.surzhykov.navsymphony.domain.Logger
import com.surzhykov.navsymphony.graph.core.graph.NavigationGraph
import com.surzhykov.navsymphony.graph.core.node.NavigationNode
import com.surzhykov.navsymphony.screen.core.ScreenRoute
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NavigationStateHandlerTest {

    private lateinit var rootNode: NavigationNode
    private lateinit var graph: NavigationGraph
    private lateinit var logger: Logger
    private lateinit var stateHandler: NavigationStateHandler

    @Before
    fun setUp() {
        rootNode = mockk(relaxed = true)
        graph = mockk(relaxed = true)
        logger = mockk(relaxed = true)
        every { graph.rootClass } returns ScreenRoute::class
        every { graph[ScreenRoute::class] } returns rootNode
        every { graph.rootNode } returns rootNode
        stateHandler = NavigationStateHandler.Base(
            graph,
            CoroutineScope(UnconfinedTestDispatcher()),
            NavigationBackStack(),
            logger
        )
    }

    @Test(expected = IllegalStateException::class)
    fun `init throws if root node is null`() {
        every { graph[ScreenRoute::class] } returns null
        NavigationStateHandler.Base(
            graph,
            CoroutineScope(UnconfinedTestDispatcher()),
            NavigationBackStack(),
            logger
        )
    }

    @Test
    fun `append adds node and updates flow`() = runTest {
        val node = mockk<NavigationNode>(relaxed = true)
        val result = stateHandler.append(node, true)
        assertTrue(result)
        assertEquals(node, stateHandler.currentNodeFlow.value)
    }

    @Test
    fun `append with keepInStack false still updates currentNodeFlow`() = runTest {
        val node = mockk<NavigationNode>(relaxed = true)
        val result = stateHandler.append(node, false)
        assertTrue(result)
        assertEquals(node, stateHandler.currentNodeFlow.value)
    }

    @Test
    fun `pop removes top and updates currentNodeFlow`() = runTest {
        val node1 = mockk<NavigationNode>(relaxed = true)
        val node2 = mockk<NavigationNode>(relaxed = true)
        stateHandler.append(node1, true)
        stateHandler.append(node2, true)
        val popped = stateHandler.pop()
        assertEquals(node1, popped)
        assertEquals(node1, stateHandler.currentNodeFlow.value)
    }

    @Test
    fun `popUntil emits target if found`() = runTest {
        val node1 = mockk<NavigationNode>(relaxed = true)
        val node2 = mockk<NavigationNode>(relaxed = true)
        every { node1.nodeMetaData.routeKClass } returns ScreenRoute::class
        every { node2.nodeMetaData.routeKClass } returns AnotherScreenRoute::class
        stateHandler.append(node1, true)
        stateHandler.append(node2, true)
        val result = stateHandler.popUntil(ScreenRoute::class)
        assertTrue(result)
        assertEquals(node1, stateHandler.currentNodeFlow.value)
    }

    @Test
    fun `popUntil appends root if no match found`() = runTest {
        val node = mockk<NavigationNode>(relaxed = true)
        every { node.nodeMetaData.routeKClass } returns AnotherScreenRoute::class
        stateHandler.append(node, true)
        val result = stateHandler.popUntil(YetAnotherScreenRoute::class)
        assertTrue(result)
        assertEquals(rootNode, stateHandler.currentNodeFlow.value)
    }

    @Test
    fun `clear removes everything and appends root`() = runTest {
        val node = mockk<NavigationNode>(relaxed = true)
        stateHandler.append(node, true)
        val result = stateHandler.clear()
        assertTrue(result)
        assertEquals(rootNode, stateHandler.currentNodeFlow.value)
    }

    @Test
    fun `backStackCount reflects correct size`() = runTest {
        assertEquals(1, stateHandler.backStackCount.value)
        val node = mockk<NavigationNode>(relaxed = true)
        stateHandler.append(node, true)
        assertEquals(2, stateHandler.backStackCount.value)
        stateHandler.pop()
        assertEquals(1, stateHandler.backStackCount.value)
        stateHandler.clear()
        assertEquals(1, stateHandler.backStackCount.value)
    }

    // Mock screen routes for testing
    abstract class AnotherScreenRoute : ScreenRoute
    abstract class YetAnotherScreenRoute : ScreenRoute
}
