package com.example.infitrivia.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.infitrivia.ui.theme.TealAccent

/**
 * Displays a loading state with progress indicator
 * @param isFirstLoad Whether this is the first load or a subsequent question load
 */
@Composable
fun LoadingState(isFirstLoad: Boolean = true) {    
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.padding(16.dp)
    ) {
        CircularProgressIndicator(
            color = TealAccent,
            modifier = Modifier.size(48.dp)
        )
        
        Text(
            text = if (isFirstLoad) "Generating trivia questions..." else "Creating next question...",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Start,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 16.dp)
        )
        
        if (isFirstLoad) {
            Text(
                text = "This may take a few moments",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
