package com.example.infitrivia.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.infitrivia.ui.theme.DarkBlue
import com.example.infitrivia.ui.theme.DarkerBlue

/**
 * A simple static gradient background for screens that don't need animations
 * Uses less resources than the animated GradientBackground
 * 
 * @param modifier Modifier to be applied to the background container
 * @param content Content to be displayed on top of the background
 */
@Composable
fun SimpleBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        DarkerBlue,
                        DarkBlue
                    )
                )
            ),
        content = content
    )
}
