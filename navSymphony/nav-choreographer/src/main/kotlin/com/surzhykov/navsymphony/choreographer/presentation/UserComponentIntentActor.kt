package com.surzhykov.navsymphony.choreographer.presentation

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.surzhykov.navsymphony.choreographer.common.IntentSender
import com.surzhykov.navsymphony.choreographer.common.NavigationIntent
import com.surzhykov.navsymphony.choreographer.common.NavigationOptions
import com.surzhykov.navsymphony.choreographer.utils.Priorities
import com.surzhykov.navsymphony.screen.core.ScreenRoute
import javax.inject.Inject

@Immutable
class UserComponentIntentActor @Inject constructor() : AppComponentIntentActor.Abstract(
    Priorities.PRIORITY_USER_DEFAULT,
    IntentSender.User
), NavigationController {

    override fun <T : ScreenRoute> navigate(
        route: T,
        navOptionsBuilder: NavigationOptions.() -> Unit,
    ) = navigate(route, NavigationOptions().apply(navOptionsBuilder))

    /**
     * Navigates to a specific [ScreenRoute].
     *
     * @param route The [ScreenRoute] to navigate to.
     */
    override fun <T : ScreenRoute> navigate(route: T, navOptions: NavigationOptions) {
        val intent = AppNavigationIntent.NavigateTo(route, navOptions)
        publishIntent(intent)
    }

    /**
     * Navigates back to the previous screen.
     */
    @Stable
    override fun navigateBack() {
        publishIntent(AppNavigationIntent.Back)
    }

    /**
     * Clears the entire back stack, effectively resetting the navigation history.
     */
    @Stable
    override fun clearBackStack() {
        publishIntent(AppNavigationIntent.ClearBackStack)
    }

    /**
     * Navigates back to a specific [ScreenRoute] in the back stack.
     *
     * @param route The [ScreenRoute] to navigate back to.
     */
    override fun navigateBackTo(route: ScreenRoute, inclusive: Boolean) {
        publishIntent(AppNavigationIntent.PopUpTo(route, inclusive))
    }

    @SensitiveNavigationApi
    override fun completeNavTransactionStep(currentRoute: ScreenRoute) {
        publishIntent(NavigationIntent.CompleteNavTransaction(currentRoute))
    }

    companion object {
        private val TAG = UserComponentIntentActor::class.java.simpleName
    }
}