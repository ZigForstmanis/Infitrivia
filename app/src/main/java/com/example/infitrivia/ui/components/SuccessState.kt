package com.example.infitrivia.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.infitrivia.model.TriviaQuestion
import com.example.infitrivia.ui.theme.OrangeButton

/**
 * Displays the success state of the game when a question is loaded
 */
@Composable
fun SuccessState(
    currentQuestion: TriviaQuestion,
    questionNumber: Int,
    selectedAnswerIndex: Int?,
    showAnswer: Boolean,
    isLoadingNextQuestion: Boolean,
    onAnswerSelected: (Int) -> Unit,
    onNextQuestion: () -> Unit,
    onEndGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(16.dp)
    ) {
        // Show current question
        QuestionScreen(
            question = currentQuestion,
            questionNumber = questionNumber,
            totalQuestions = 0, // We don't have a total since it's indefinite
            selectedAnswerIndex = selectedAnswerIndex,
            showAnswer = showAnswer,
            onAnswerSelected = onAnswerSelected,
            onNextQuestion = onNextQuestion,
            isLoadingNextQuestion = isLoadingNextQuestion && showAnswer
        )
        
        // Home button
        Button(
            onClick = onEndGame,
            colors = ButtonDefaults.buttonColors(
                containerColor = OrangeButton
            ),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(
                text = "End Game & Choose New Topic",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
