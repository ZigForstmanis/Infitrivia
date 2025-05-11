package com.example.infitrivia.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
 * Displays a trivia question with answer options and factoid
 */
@Composable
fun QuestionScreen(
    modifier: Modifier = Modifier,
    question: TriviaQuestion,
    questionNumber: Int,
    totalQuestions: Int,
    selectedAnswerIndex: Int?,
    showAnswer: Boolean,
    onAnswerSelected: (Int) -> Unit,
    onNextQuestion: () -> Unit,
    isLoadingNextQuestion: Boolean = false

) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {        // Question card
        TriviaQuestionCard(
            question = question,
            questionNumber = questionNumber,
            totalQuestions = totalQuestions,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Answer options
        question.options.forEachIndexed { index, option ->
            val answerState = when {
                // Show correct/incorrect state after selecting an answer
                showAnswer && index == question.correctAnswerIndex -> AnswerState.CORRECT
                showAnswer && index == selectedAnswerIndex && index != question.correctAnswerIndex -> AnswerState.INCORRECT
                selectedAnswerIndex == index -> AnswerState.SELECTED
                else -> AnswerState.UNANSWERED
            }
            
            AnswerOption(
                answerText = option,
                optionIndex = index,
                state = answerState,
                onOptionSelected = { onAnswerSelected(it) }
            )
        }
        
        // Factoid (shown after answer is selected)
        if (showAnswer) {
            Spacer(modifier = Modifier.height(8.dp))
            
            FactoidCard(
                factoid = question.factoid,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
              Button(
                onClick = onNextQuestion,
                enabled = !isLoadingNextQuestion,
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangeButton
                ),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                if (isLoadingNextQuestion) {
                    // Show loading indicator in the button
                    androidx.compose.material3.CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Next Question",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}
