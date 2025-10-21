package com.surzhykov.navsymphony.choreographer.common

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class IntentValidationChainBaseTest {

    @Test
    fun `check throws invalid as soon as it occurred`() = runTest {
        val invalidResult = ValidationResult.Invalid("Invalid result")
        val invalidChain = mockk<IntentValidationChain>(relaxed = true) {
            coEvery { validate(any(), any()) } returns invalidResult
        }
        val validChain = mockk<IntentValidationChain>(relaxed = true) {
            coEvery { validate(any(), any()) } returns ValidationResult.Valid
        }
        val chain = IntentValidationChain.Base(
            setOf(invalidChain, validChain)
        )
        val actual = chain.validate(mockk(), mockk())
        assertEquals(invalidResult, actual)
        coVerify(exactly = 0) {
            validChain.validate(any(), any())
        }
    }

    @Test
    fun `check returns valid if all chains are valid`() = runTest {
        val validResult = ValidationResult.Valid
        val validChain1 = mockk<IntentValidationChain>(relaxed = true) {
            coEvery { validate(any(), any()) } returns validResult
        }

        val validChain2 = mockk<IntentValidationChain>(relaxed = true) {
            coEvery { validate(any(), any()) } returns validResult
        }
        val chain = IntentValidationChain.Base(
            setOf(validChain1, validChain2)
        )
        val actual = chain.validate(mockk(), mockk())
        assertEquals(validResult, actual)
    }

    @Test
    fun `check combines redirect results into single redirect`() = runTest {
        val originalIntent = mockk<NavigationIntent>(relaxed = true)
        val mockIntent1 = mockk<NavigationIntent>(relaxed = true)
        val redirect1 = ValidationResult.Redirect(
            originalIntent,
            RedirectChainBuilder(originalIntent).apply {
                addIntent(mockIntent1)
            }
        )
        val mockIntent2 = mockk<NavigationIntent>(relaxed = true)
        val redirect2 = ValidationResult.Redirect(
            originalIntent,
            RedirectChainBuilder(originalIntent).apply {
                addIntent(mockIntent2)
            }
        )
        val chain1 = mockk<IntentValidationChain>(relaxed = true) {
            coEvery { validate(any(), any()) } returns redirect1
        }
        val chain2 = mockk<IntentValidationChain>(relaxed = true) {
            coEvery { validate(any(), any()) } returns redirect2
        }
        val chain = IntentValidationChain.Base(
            setOf(chain1, chain2)
        )
        val actual = chain.validate(originalIntent, mockk())
        val expected = ValidationResult.Redirect(
            originalIntent,
            RedirectChainBuilder(originalIntent).apply {
                addIntents(setOf(mockIntent1, mockIntent2))
            }
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `check returns redirect if one chain returns redirect`() = runTest {
        val originalIntent = mockk<NavigationIntent>(relaxed = true)
        val mockIntent1 = mockk<NavigationIntent>(relaxed = true)

        val redirect1 = ValidationResult.Redirect(
            originalIntent,
            RedirectChainBuilder(originalIntent).apply {
                addIntent(mockIntent1)
            }
        )
        val chain1 = mockk<IntentValidationChain>(relaxed = true) {
            coEvery { validate(any(), any()) } returns redirect1
        }
        val chain2 = mockk<IntentValidationChain>(relaxed = true) {
            coEvery { validate(any(), any()) } returns ValidationResult.Valid
        }
        val chain = IntentValidationChain.Base(
            setOf(chain1, chain2)
        )
        val actual = chain.validate(originalIntent, mockk())
        assertEquals(redirect1, actual)
    }
}