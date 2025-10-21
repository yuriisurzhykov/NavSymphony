package com.surzhykov.navsymphony.choreographer.data

import com.surzhykov.navsymphony.choreographer.common.NavigationIntent
import com.surzhykov.navsymphony.choreographer.common.Transaction
import com.surzhykov.navsymphony.domain.Logger
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NavigationTransactionManagerTest {

    private lateinit var manager: NavigationTransactionManager.Base
    private lateinit var logger: Logger
    private lateinit var intent1: NavigationIntent
    private lateinit var intent2: NavigationIntent
    private lateinit var mockOriginalIntent: NavigationIntent
    private lateinit var transaction: Transaction

    @Before
    fun setUp() {
        logger = mockk(relaxed = true)
        manager = NavigationTransactionManager.Base(logger)
        intent1 = mockk()
        intent2 = mockk()
        mockOriginalIntent = mockk()
        transaction = mockk {
            every { requiredIntents } returns setOf(intent1, intent2)
            every { originalIntent } returns mockOriginalIntent
        }
    }

    @Test
    fun `applyTransaction sets transaction if no current one`() {
        manager.applyTransaction(transaction)
        assertTrue("Transaction manager should be active, but it is not", manager.isActive())
    }

    @Test(expected = IllegalStateException::class)
    fun `applyTransaction throws if current transaction is active`() {
        manager.applyTransaction(transaction)
        manager.next()
        manager.applyTransaction(transaction)
    }

    @Test
    fun `next returns first intent`() {
        manager.applyTransaction(transaction)
        val result = manager.next().intent
        assertEquals(intent1, result)
    }

    @Test
    fun `next returns second intent`() {
        manager.applyTransaction(transaction)
        manager.next()
        val result = manager.next().intent
        assertEquals(intent2, result)
    }

    @Test
    fun `next returns original intent and clears after last`() {
        manager.applyTransaction(transaction)
        manager.next()
        manager.next()
        val result = manager.next().intent
        assertEquals(mockOriginalIntent, result)
        assertFalse(manager.isActive())
    }

    @Test(expected = IllegalAccessException::class)
    fun `next throws if transaction is null`() {
        manager.next()
    }

    @Test(expected = IllegalStateException::class)
    fun `next throws on inconsistent state - null transaction with non-null iterator`() {
        manager.applyTransaction(transaction)
        val transactionField =
            NavigationTransactionManager.Base::class.java.getDeclaredField("transaction")
        transactionField.isAccessible = true
        transactionField.set(manager, null)
        manager.next()
    }

    @Test(expected = IllegalStateException::class)
    fun `next throws on inconsistent state - non-null transaction with null iterator`() {
        manager.applyTransaction(transaction)
        val iteratorField =
            NavigationTransactionManager.Base::class.java.getDeclaredField("intentsIterator")
        iteratorField.isAccessible = true
        iteratorField.set(manager, null)
        manager.next()
    }

    @Test
    fun `cancel resets state`() {
        manager.applyTransaction(transaction)
        manager.cancel()
        assertFalse(manager.isActive())
        assertNull(manager.current())
    }

    @Test
    fun `current returns null before next`() {
        manager.applyTransaction(transaction)
        assertNull(manager.current())
    }

    @Test
    fun `current returns last intent after next`() {
        manager.applyTransaction(transaction)
        val next = manager.next().intent
        assertEquals(next, manager.current())
    }

    @Test
    fun `isActive returns true if hasNext`() {
        manager.applyTransaction(transaction)
        assertTrue(
            "NavigationTransactionManager.isActive() should return true, but it returned false",
            manager.isActive()
        )
    }

    @Test
    fun `isActive returns false after final intent`() {
        manager.applyTransaction(transaction)
        manager.next()
        manager.next()
        manager.next()
        assertFalse(manager.isActive())
    }

    @Test
    fun `verify the transaction flow always return valid isActive result`() {
        manager.applyTransaction(transaction)
        assertTrue(
            "Transaction not finished yet, active should be true",
            manager.isActive()
        )

        manager.next()
        assertTrue(
            "Transaction not finished yet, active should be true",
            manager.isActive()
        )

        manager.next()
        assertTrue(
            "Transaction not finished yet, because original intent left in transaction process!",
            manager.isActive()
        )
        val currentIntent = manager.next().intent
        assertEquals(mockOriginalIntent, currentIntent)
        assertFalse(
            "Transaction should be finished now, active should be false",
            manager.isActive()
        )
    }

    @Test
    fun `verify that manager returns Continue result if there more redirect intents`() {
        manager.applyTransaction(transaction)
        val next = manager.next()
        assertTrue("The result must be Continue class!", next is TransactionInfo.Continue)
    }

    @Test
    fun `verify that manager returns BackToOriginal result if there no more redirect intents`() {
        // Apply transaction to manager.
        manager.applyTransaction(transaction)
        assertNull("The current intent must be null before next call!", manager.current())

        // Forwarding to the end of the transaction.
        manager.next()
        manager.next()

        // After proceeding with required intents from transaction, the manager should return
        // the original intent. An intent must be wrapped with BackToOriginal class.
        val next = manager.next()
        assertTrue(
            "The result must be BackToOriginal class!",
            next is TransactionInfo.BackToOriginal
        )
    }
}
