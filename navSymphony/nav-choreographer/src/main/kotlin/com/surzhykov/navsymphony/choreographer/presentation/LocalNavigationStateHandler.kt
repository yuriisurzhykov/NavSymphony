package com.surzhykov.navsymphony.choreographer.presentation

import androidx.compose.runtime.staticCompositionLocalOf
import com.surzhykov.navsymphony.choreographer.data.NavigationStateHandler

val LocalNavigationStateHandler = staticCompositionLocalOf<NavigationStateHandler> {
    error("CompositionLocal LocalNavigationStateHandler not present")
}