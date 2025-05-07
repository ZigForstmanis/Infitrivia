package com.example.infitrivia.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.infitrivia.ui.navigation.TriviaApp
import com.example.infitrivia.ui.theme.InfitriviaTheme

/**
 * Preview composables for testing UI components in Android Studio's preview pane
 */
@Preview(
    showBackground = true, 
    showSystemUi = true,
    name = "Infitrivia Home Screen"
)
@Composable
fun InfitriviaAppPreview() {
    InfitriviaTheme {
        TriviaApp()
    }
}