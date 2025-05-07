package com.example.infitrivia.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.infitrivia.ui.components.TriviaTopicInput

/**
 * Main home screen for the Infitrivia app
 */
@Composable
fun InfitriviaHomeScreen(
    onStartTrivia: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var topicText by rememberSaveable { mutableStateOf("") }
    
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top 40% with app title
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Infitrivia",
                style = MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Center,
                color = Color.White // Ensuring text is visible on gradient
            )
        }
        
        // Middle area with text input
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f),
            contentAlignment = Alignment.Center
        ) {
            TriviaTopicInput(
                value = topicText,
                onValueChange = { topicText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 50.dp)
            )
        }
        
        // Bottom area with button positioned for thumb access
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.2f)
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = { 
                    if (topicText.isNotBlank()) {
                        onStartTrivia(topicText)
                    }
                },
                shape = RoundedCornerShape(30.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF8C00)
                ),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Start",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}