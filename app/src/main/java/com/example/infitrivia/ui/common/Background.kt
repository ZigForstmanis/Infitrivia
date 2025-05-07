package com.example.infitrivia.ui.common

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Animated gradient background component with ripple effect
 */
@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF14122f),
    rippleColor: Color = Color(0xFF0d9488),
    edgeColor: Color = Color(0xFF134e4a),
    content: @Composable () -> Unit
) {
    // Simple ripple animation
    val infiniteTransition = rememberInfiniteTransition(label = "ripple")
    
    // Animate the radius of the ripple with custom easing
    val rippleSize by infiniteTransition.animateFloat(
        initialValue = 1750f,
        targetValue = 1850f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 20000,
                // Using DecelerateAccelerateEasing to slow down at the extremes
                easing = CubicBezierEasing(0.7f, 0.0f, 0.3f, 1.0f)
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ripple-size"
    )
    
    // Solid background with the requested color
    Box(
        modifier = modifier.background(backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colorStops = arrayOf(
                            0.0f to backgroundColor,
                            0.5f to backgroundColor,
                            0.65f to edgeColor,
                            0.65f to rippleColor,
                            1f to backgroundColor
                        ),
                        center = Offset(x = 100f, y = 300f),
                        radius = rippleSize
                    )
                )
        ) {
            content()
        }
    }
}