package com.surzhykov.navsymphony.choreographer.common

import com.surzhykov.navsymphony.choreographer.R
import com.surzhykov.navsymphony.choreographer.presentation.NavigationContext
import com.surzhykov.navsymphony.choreographer.presentation.NavigationController
import com.surzhykov.navsymphony.core.presentation.DrawableResolver
import com.surzhykov.navsymphony.core.presentation.StringResolver
import com.surzhykov.navsymphony.domain.Logger
import com.surzhykov.navsymphony.screen.core.ScreenRoute
import com.surzhykov.navsymphony.window.core.AbstractOverlayWindow
import com.surzhykov.navsymphony.window.core.OverlayManager
import com.surzhykov.navsymphony.window.dialogs.PrimaryMessageDialog
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class NavigationIntentTest {

    // Mock ScreenRoute for testing
    private class TestScreenRoute : ScreenRoute

    private lateinit var mockNavigationController: NavigationController
    private lateinit var mockOverlayManager: OverlayManager
    private lateinit var navigationContext: NavigationContext

    @Before
    fun initialize() {
        mockNavigationController = mockk<NavigationController>(relaxed = true)
        mockOverlayManager = mockk<OverlayManager>(relaxed = true)
        navigationContext = spyk(NavigationContext(mockNavigationController, mockOverlayManager))
    }

    @Test
    fun `Back transformToCommand returns NavigationCommand Back`() {
        val intent = NavigationIntent.Back(IntentSender.User, 1)
        val command = intent.transformToCommand()
        assertTrue(command is NavigationCommand.Back)
    }

    @Test
    fun `PopUpTo transformToCommand returns NavigationCommand PopUpTo`() {
        val route = TestScreenRoute()
        val intent = NavigationIntent.PopUpTo(route, true, IntentSender.User, 2)
        val command = intent.transformToCommand()
        assertTrue(command is NavigationCommand.PopUpTo)
        command as NavigationCommand.PopUpTo
        assertEquals(route, command.route)
        assertEquals(true, command.inclusive)
    }

    @Test
    fun `NavigateTo transformToCommand returns NavigationCommand NavigateTo`() {
        val route = TestScreenRoute()
        val navOptions = NavigationOptions()
        val intent =
            NavigationIntent.NavigateTo(route, navOptions, IntentSender.User, 3, Logger.Console())
        val command = intent.transformToCommand()
        assertTrue(command is NavigationCommand.NavigateTo)
        command as NavigationCommand.NavigateTo

        command.execute(navigationContext)

        verify(exactly = 1) { mockNavigationController.navigate(route, navOptions) }
    }

    @Test
    fun `DisplayDialog transformToCommand returns NavigationCommand Dialog`() {
        val dialog = mockk<AbstractOverlayWindow>(relaxed = true)
        val intent =
            NavigationIntent.DisplayDialog(dialog, IntentSender.User, 4, null, Logger.Console())
        val command = intent.transformToCommand()
        assertTrue(command is NavigationCommand.Dialog)
        command as NavigationCommand.Dialog
        command.execute(navigationContext)

        verify(exactly = 1) { mockOverlayManager.showOverlay(dialog) }
    }

    @Test
    fun `DisplayDialog constructor with message and severity creates PrimaryMessageDialog`() {
        val message = "Test Message"
        val sender = IntentSender.User
        val priority = 5
        val severity = mockk<DrawableResolver>(relaxed = true)
        val title = StringResolver.from(R.string.error_message_unknown)
        val messageResolver = StringResolver.from(message)
        val dialog = PrimaryMessageDialog(severity, title, messageResolver)
        val intent = NavigationIntent.DisplayDialog(dialog, sender, priority)
        assertTrue(intent.dialog is PrimaryMessageDialog)
    }

    @Test
    fun `InteractionTimeout transformToCommand returns NavigationCommand ClearBackStack`() {
        val intent = NavigationIntent.InteractionTimeout(6)
        val command = intent.transformToCommand()
        assertTrue(command is NavigationCommand.ClearBackStack)
    }

    @Test
    fun `ClearBackStack transformToCommand returns NavigationCommand ClearBackStack`() {
        val intent = NavigationIntent.ClearBackStack(IntentSender.User, 7)
        val command = intent.transformToCommand()
        assertTrue(command is NavigationCommand.ClearBackStack)
    }

    @Test(expected = IllegalAccessException::class)
    fun `CompleteNavTransaction transformToCommand throws IllegalAccessException`() {
        val route = TestScreenRoute()
        val intent = NavigationIntent.CompleteNavTransaction(route)
        intent.transformToCommand()
    }
}