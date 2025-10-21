package com.surzhykov.navsymphony.screen.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

@Composable
fun <I : ScreenIntent> rememberIntentSender(
    viewModel: AbstractViewModel<*, I>,
    intentProducer: () -> I,
): () -> Unit {
    val currentViewModel by rememberUpdatedState(viewModel)
    return remember { { currentViewModel.onIntent(intentProducer()) } }
}

@Composable
fun <I : ScreenIntent, P1> rememberIntentSender1(
    viewModel: AbstractViewModel<*, I>,
    intentProducer: (P1) -> I,
): (P1) -> Unit {
    val currentViewModel by rememberUpdatedState(viewModel)
    return remember { { p1: P1 -> currentViewModel.onIntent(intentProducer(p1)) } }
}

@Composable
fun <I : ScreenIntent, P1, P2> rememberIntentSender2(
    viewModel: AbstractViewModel<*, I>,
    intentProducer: (P1, P2) -> I,
): (P1, P2) -> Unit {
    val currentViewModel by rememberUpdatedState(viewModel)
    return remember { { p1: P1, p2: P2 -> currentViewModel.onIntent(intentProducer(p1, p2)) } }
}