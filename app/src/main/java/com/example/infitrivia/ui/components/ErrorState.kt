package com.example.infitrivia.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.infitrivia.ui.navigation.TriviaDestinations
import com.example.infitrivia.ui.theme.OrangeButton
import com.example.infitrivia.ui.theme.TealAccent

/**
 * Displays an error state with retry options
 * @param errorMessage Error message to display
 * @param onRetry Function to call when retry button is clicked
 */
@Composable
fun ErrorState(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController()
) { 
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .padding(24.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = "Sorry!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Start,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TealAccent
                ),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(text = "Try Again")
            }
            
            Button(
                onClick = { 
                    navController.navigate(TriviaDestinations.HOME_ROUTE) {
                        popUpTo(TriviaDestinations.HOME_ROUTE) { inclusive = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangeButton
                ),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(text = "New Topic")
            }
        }
    }
}
