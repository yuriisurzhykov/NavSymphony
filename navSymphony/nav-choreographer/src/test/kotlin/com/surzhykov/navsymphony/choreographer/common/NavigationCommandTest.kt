package com.surzhykov.navsymphony.choreographer.common

import com.surzhykov.navsymphony.choreographer.presentation.NavigationContext
import com.surzhykov.navsymphony.choreographer.presentation.NavigationController
import com.surzhykov.navsymphony.domain.Logger
import com.surzhykov.navsymphony.screen.core.ScreenRoute
import com.surzhykov.navsymphony.window.core.AbstractOverlayWindow
import com.surzhykov.navsymphony.window.core.OverlayManager
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class NavigationCommandTest {

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
    fun `check NavigateTo command requests navigation from navigation controller`() = runTest {
        val route = mockk<ScreenRoute>()
        val navOptions = mockk<NavigationOptions>()
        val navigateToCommand = NavigationCommand.NavigateTo(route, navOptions, Logger.Console())
        navigateToCommand.execute(navigationContext)
        verify(exactly = 1) { mockNavigationController.navigate(route, navOptions) }
    }

    @Test
    fun `check PopUpTo command requests navigation back to the specified route`() = runTest {
        val route = mockk<ScreenRoute>()
        val popUpToCommand = NavigationCommand.PopUpTo(route, true, Logger.Console())
        popUpToCommand.execute(navigationContext)
        verify(exactly = 1) { mockNavigationController.navigateBackTo(route, true) }
    }

    @Test
    fun `check Back command request navigation back`() = runTest {
        val backCommand = NavigationCommand.Back(Logger.Console())
        backCommand.execute(navigationContext)
        verify(exactly = 1) { mockNavigationController.navigateBack() }
    }

    @Test
    fun `check ClearBackStack command clears the back stack`() = runTest {
        val clearBackStackCommand = NavigationCommand.ClearBackStack(Logger.Console())
        clearBackStackCommand.execute(navigationContext)
        verify(exactly = 1) { mockNavigationController.clearBackStack() }
    }

    @Test
    fun `check Dialog command requests dialog to be opened`() = runTest {
        val overlayWindow = mockk<AbstractOverlayWindow>()
        val dialogCommand = NavigationCommand.Dialog(overlayWindow, mockk(relaxed = true), Logger.Console())
        dialogCommand.execute(navigationContext)
        verify(exactly = 1) { mockOverlayManager.showOverlay(overlayWindow) }
    }
}