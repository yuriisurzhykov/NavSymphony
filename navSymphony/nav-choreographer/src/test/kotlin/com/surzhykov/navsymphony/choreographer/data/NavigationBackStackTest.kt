package com.surzhykov.navsymphony.choreographer.data

import com.surzhykov.navsymphony.choreographer.common.NavigationOptions
import com.surzhykov.navsymphony.domain.Logger
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class NavigationBackStackTest {

    private lateinit var logger: Logger
    private lateinit var backStack: NavigationBackStack<String>

    @Before
    fun setUp() {
        logger = mockk(relaxed = true)
        backStack = NavigationBackStack(logger)
    }

    @Test
    fun `add() with addToBackStack true adds to main stack`() {
        backStack.add("A", NavigationOptions(addToBackStack = true))
        assertEquals("A", backStack.last())
        assertEquals(1, backStack.size)
    }

    @Test
    fun `add() with addToBackStack false adds to non-retained stack`() {
        backStack.add("A", NavigationOptions(addToBackStack = false))
        assertEquals("A", backStack.last())
        assertEquals(1, backStack.size)
    }

    @Test
    fun `add() with singleTop true avoids duplicate in main`() {
        backStack.add("A", NavigationOptions(addToBackStack = true))
        backStack.add("A", NavigationOptions(addToBackStack = true, singleTop = true))
        assertEquals(1, backStack.size)
    }

    @Test
    fun `add() with clearBackStack clears everything before adding`() {
        backStack.add("A", NavigationOptions(addToBackStack = true))
        backStack.add("B", NavigationOptions(clearBackStack = true, addToBackStack = true))
        assertEquals("B", backStack.last())
        assertEquals(1, backStack.size)
    }

    @Test
    fun `pop() clears non-retained entries and returns last of main`() {
        backStack.add("A", NavigationOptions(addToBackStack = true))
        backStack.add("B", NavigationOptions(addToBackStack = false))
        val result = backStack.pop()
        assertEquals("A", result)
        assertEquals(1, backStack.size)
    }

    @Test
    fun `pop() removes top of main and returns new top`() {
        backStack.add("A", NavigationOptions(addToBackStack = true))
        backStack.add("B", NavigationOptions(addToBackStack = true))
        val result = backStack.pop()
        assertEquals("A", result)
        assertEquals(1, backStack.size)
    }

    @Test(expected = NoSuchElementException::class)
    fun `pop() on empty stack throws exception`() {
        backStack.pop()
    }

    @Test
    fun `popUntil inclusive false keeps matched entry`() {
        backStack.add("A", NavigationOptions(addToBackStack = true))
        backStack.add("B", NavigationOptions(addToBackStack = true))
        val result = backStack.popUntil(inclusive = false) { it == "A" }
        assertEquals("A", result)
        assertEquals(1, backStack.size)
    }

    @Test
    fun `popUntil inclusive true removes matched entry`() {
        backStack.add("A", NavigationOptions(addToBackStack = true))
        backStack.add("B", NavigationOptions(addToBackStack = true))
        val result = backStack.popUntil(inclusive = true) { it == "A" }
        assertEquals("A", result)
        assertEquals(0, backStack.size)
    }

    @Test(expected = IllegalStateException::class)
    fun `popUntil with empty stack throws IllegalStateException`() {
        backStack.popUntil { it == "X" }
    }

    @Test(expected = NoSuchElementException::class)
    fun `popUntil with no match clears and throws`() {
        backStack.add("A", NavigationOptions(addToBackStack = true))
        backStack.popUntil { it == "X" }
    }

    @Test
    fun `last returns from non-retained if not empty`() {
        backStack.add("A", NavigationOptions(addToBackStack = false))
        assertEquals("A", backStack.last())
    }

    @Test
    fun `last returns from main stack if non-retained is empty`() {
        backStack.add("A", NavigationOptions(addToBackStack = true))
        assertEquals("A", backStack.last())
    }

    @Test
    fun `clear removes all entries`() {
        backStack.add("A", NavigationOptions(addToBackStack = true))
        backStack.add("B", NavigationOptions(addToBackStack = false))
        backStack.clear()
        assertEquals(0, backStack.size)
    }

    @Test
    fun `singleTop avoids duplicates in non-retained stack`() {
        backStack.add("A", NavigationOptions(addToBackStack = false))
        backStack.add("A", NavigationOptions(addToBackStack = false, singleTop = true))
        assertEquals(1, backStack.size)
    }

    @Test
    fun `popUntil with single entry behaves correctly`() {
        backStack.add("A", NavigationOptions(addToBackStack = true))
        val result = backStack.popUntil(inclusive = true) { it == "A" }
        assertEquals("A", result)
        assertEquals(0, backStack.size)
    }

    @Test
    fun `size returns sum of both stacks`() {
        backStack.add("A", NavigationOptions(addToBackStack = true))
        backStack.add("B", NavigationOptions(addToBackStack = false))
        assertEquals(2, backStack.size)
    }

    @Test
    fun `clear then add() results in clean state`() {
        backStack.add("A", NavigationOptions(addToBackStack = true))
        backStack.clear()
        backStack.add("B", NavigationOptions(addToBackStack = true))
        assertEquals("B", backStack.last())
        assertEquals(1, backStack.size)
    }

    @Test
    fun `add() non-retained stack adds when singleTop is false and last differs`() {
        backStack.add("A", NavigationOptions(addToBackStack = false))
        backStack.add("B", NavigationOptions(addToBackStack = false, singleTop = true))
        assertEquals(2, backStack.size)
        assertEquals("B", backStack.last())
    }
}
