package com.surzhykov.navsymphony.choreographer.presentation

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.referentialEqualityPolicy

val LocalSystemIntentActor =
    compositionLocalOf<SystemComponentIntentActor>(referentialEqualityPolicy()) {
        error("CompositionLocal LocalSystemIntentActor not present")
    }