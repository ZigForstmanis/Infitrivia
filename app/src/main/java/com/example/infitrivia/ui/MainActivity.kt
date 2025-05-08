package com.example.infitrivia.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.infitrivia.ui.navigation.TriviaApp
import com.example.infitrivia.ui.theme.InfitriviaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InfitriviaTheme {
                TriviaApp()
            }
        }
    }
}