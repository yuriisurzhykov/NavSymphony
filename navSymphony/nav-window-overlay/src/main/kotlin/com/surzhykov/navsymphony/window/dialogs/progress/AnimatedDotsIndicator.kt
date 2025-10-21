package com.surzhykov.navsymphony.window.dialogs.progress

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.StartOffsetType
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp

/**
 * Displays an animated row of vertical-traveling dots used to indicate progress.
 *
 * Each dot follows a phase-shifted infinite animation cycle with vertical travel,
 * size scaling, and alpha pulsing. The animation is synchronized using start offsets.
 *
 * @param modifier Modifier applied to the entire indicator.
 * @param dotCount Number of animated dots.
 * @param spaceBetween Horizontal spacing between dots.
 * @param dotColor Color of each dot.
 * @param dotSize Diameter of each dot.
 * @param travelDistance Vertical distance each dot travels during animation.
 * @param animationDuration Duration of the animation cycle for each dot.
 */
@Composable
fun AnimatedDotsIndicator(
    modifier: Modifier = Modifier,
    dotCount: Int = 5,
    spaceBetween: Dp = 20.dp,
    dotColor: Color = MaterialTheme.colorScheme.onSurface,
    dotSize: Dp = 18.dp,
    travelDistance: Dp = 20.dp,
    animationDuration: Int = 600,
) {
    val transition = rememberInfiniteTransition(label = "DotsLoaderTransition")
    val density = LocalDensity.current

    val anchors = List(dotCount) { index ->
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = animationDuration),
                repeatMode = RepeatMode.Reverse,
                initialStartOffset = StartOffset(
                    offsetMillis = index * animationDuration / dotCount,
                    offsetType = StartOffsetType.Delay
                )
            ),
            label = "AnimationDot$index"
        )
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spaceBetween),
        verticalAlignment = Alignment.CenterVertically
    ) {
        anchors.forEach { animatedAnchor ->
            Box(
                modifier = Modifier
                    .dotBehavior(
                        progress = animatedAnchor.value,
                        travelDistance = travelDistance,
                        density = density
                    )
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
    }
}

/**
 * Applies animation-driven vertical translation, scaling, and alpha to a dot.
 *
 * @param progress Animation progress from 0f to 1f.
 * @param travelDistance Maximum vertical offset for animation.
 * @param density Current Compose density for pixel conversion.
 * @return [Modifier] with applied visual effects.
 */
internal fun Modifier.dotBehavior(
    progress: Float,
    travelDistance: Dp,
    density: Density,
): Modifier = this
    .graphicsLayer {
        val offsetPx = with(density) { travelDistance.toPx() }
        val sizeLerp = lerp(1f, 1.25f, progress)
        val alphaLerp = lerp(0.5f, 1f, progress)

        translationY = -offsetPx * progress
        scaleX = sizeLerp
        scaleY = sizeLerp
        alpha = alphaLerp
    }
