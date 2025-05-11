package com.example.infitrivia.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.infitrivia.ui.theme.OrangeButton
import com.example.infitrivia.ui.theme.TealAccent

/**
 * Different states for an answer option
 */
enum class AnswerState {
    UNANSWERED,
    SELECTED,
    CORRECT,
    INCORRECT
}

/**
 * Displays a single answer option in the trivia game
 */
@Composable
fun AnswerOption(
    answerText: String,
    optionIndex: Int,
    state: AnswerState = AnswerState.UNANSWERED,
    onOptionSelected: (Int) -> Unit = {}
) {
    // Determine colors based on state
    val (backgroundColor, textColor, borderColor) = when (state) {
        AnswerState.UNANSWERED -> Triple(
            Color.Transparent,
            MaterialTheme.colorScheme.onBackground,
            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
        )
        AnswerState.SELECTED -> Triple(
            OrangeButton.copy(alpha = 0.8f),
            MaterialTheme.colorScheme.onPrimary,
            OrangeButton
        )
        AnswerState.CORRECT -> Triple(
            TealAccent.copy(alpha = 0.8f),
            MaterialTheme.colorScheme.onPrimary,
            TealAccent
        )
        AnswerState.INCORRECT -> Triple(
            MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
            MaterialTheme.colorScheme.onError,
            MaterialTheme.colorScheme.error
        )
    }

    // Option letters for prefixing the answers
    val optionLetters = listOf("A", "B", "C", "D", "E")
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(
                enabled = state == AnswerState.UNANSWERED,
                onClick = { onOptionSelected(optionIndex) }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor)
    ) {        Text(
            text = "${optionLetters[optionIndex]}: $answerText",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Start, // Changed to left justified
            color = textColor,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
        )
    }
}
