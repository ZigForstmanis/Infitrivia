package com.example.infitrivia.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.infitrivia.ui.common.GradientBackground
import com.example.infitrivia.ui.screens.InfitriviaHomeScreen

/**
 * Main app navigation container
 */
@Composable
fun TriviaApp() {
    Scaffold { innerPadding ->
        GradientBackground(
            modifier = Modifier.padding(innerPadding)
        ) {
            // In a more complex app, this would handle navigation between multiple screens
            // For now, we just show the home screen
            InfitriviaHomeScreen()
        }
    }
}