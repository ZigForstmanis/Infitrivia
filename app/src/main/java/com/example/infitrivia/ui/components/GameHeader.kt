package com.example.infitrivia.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.infitrivia.ui.theme.TealAccent
import com.example.infitrivia.ui.viewmodel.GameUiState

/**
 * Displays the game header with topic and score information
 */
@Composable
fun GameHeader(
    topic: String,
    uiState: GameUiState,
    score: Int = 0,
    questionsAnswered: Int = 0
) {
    if (uiState is GameUiState.Success) {
        // Success state header with score
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Topic
            Text(
                text = "Topic: $topic",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
              
            // Score with percentage for endless mode
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Score: $score / $questionsAnswered",
                    style = MaterialTheme.typography.titleMedium,
                    color = TealAccent,
                    fontWeight = FontWeight.Bold
                )
                  
                if (questionsAnswered > 0) {
                    val percentage = (score * 100 / questionsAnswered.coerceAtLeast(1))
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (percentage >= 70) TealAccent else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                }
            }
        }
        
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            thickness = 1.dp,
            color = TealAccent.copy(alpha = 0.5f)
        )
    } else {
        // Simple header for loading/error states
        Text(
            text = "Trivia: $topic",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp)
        )

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 32.dp),
            thickness = 2.dp,
            color = TealAccent.copy(alpha = 0.5f)
        )
    }
}
