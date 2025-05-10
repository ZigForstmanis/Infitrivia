package com.example.infitrivia.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.infitrivia.config.ApiConfig
import com.example.infitrivia.service.GeminiService
import com.example.infitrivia.ui.components.TriviaTopicInput
import kotlinx.coroutines.launch

/**
 * Main home screen for the Infitrivia app
 */
@Composable
fun InfitriviaHomeScreen(
    onStartTrivia: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var topicText by rememberSaveable { mutableStateOf("") }
    var isValidating by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val geminiService = remember { GeminiService(ApiConfig.GEMINI_API_KEY) }
    
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
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
                color = MaterialTheme.colorScheme.onBackground
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
                        // Start validation with Gemini
                        isValidating = true
                        coroutineScope.launch {
                            val result = geminiService.validateTopic(topicText)
                            isValidating = false

                            if (result.isValid) {
                                onStartTrivia(topicText)
                            } else {
                                // Show error message if topic is invalid
                                snackbarHostState.showSnackbar(
                                    message = result.message.ifEmpty {
                                        "Please choose a different topic"
                                    }
                                )
                            }
                        }
                    }
                },
                enabled = !isValidating && topicText.isNotBlank(),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .padding(horizontal = 16.dp)
            ) {
                if (isValidating) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    Text(
                        text = "Start",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
        }

        // Snackbar host for showing validation messages
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
        )
    }
}