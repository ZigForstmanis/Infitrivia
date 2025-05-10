package com.example.infitrivia.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.infitrivia.model.TriviaQuiz
import com.example.infitrivia.ui.components.QuestionScreen
import com.example.infitrivia.ui.components.ResultsScreen
import com.example.infitrivia.ui.navigation.TriviaDestinations
import com.example.infitrivia.ui.theme.TealAccent
import com.example.infitrivia.ui.viewmodel.GameUiState
import com.example.infitrivia.ui.viewmodel.GameViewModel
import com.example.infitrivia.ui.viewmodel.GameViewModelFactory

/**
 * Game screen where users play the trivia game
 * @param topic The selected trivia topic
 */
@Composable
fun GameScreen(
    topic: String,
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = viewModel(factory = GameViewModelFactory()),
    navController: NavController = rememberNavController()
) {
    // Load quiz when screen is first displayed
    LaunchedEffect(topic) {
        viewModel.loadQuiz(topic)
    }
    
    // Observe state
    val uiState by viewModel.uiState.collectAsState()
    val currentQuestionIndex by viewModel.currentQuestionIndex.collectAsState()
    val selectedAnswerIndex by viewModel.selectedAnswerIndex.collectAsState()
    val showAnswer by viewModel.showAnswer.collectAsState()
    val score by viewModel.score.collectAsState()
    val gameComplete by viewModel.gameComplete.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with topic and score (only if quiz is loaded)
        if (uiState is GameUiState.Success) {
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
                
                // Score
                Text(
                    text = "Score: $score",
                    style = MaterialTheme.typography.titleMedium,
                    color = TealAccent,
                    fontWeight = FontWeight.Bold
                )
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
        
        // Content based on UI state
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                is GameUiState.Loading -> {
                    LoadingState()
                }
                is GameUiState.Success -> {
                    val quiz = (uiState as GameUiState.Success).quiz
                    
                    if (gameComplete) {
                        // Show results screen
                        ResultsScreen(
                            topic = quiz.topic,
                            score = score,
                            totalQuestions = quiz.questions.size,
                            onPlayAgain = { viewModel.restartQuiz() },
                            onNewTopic = { 
                                // Navigate back to home screen
                                navController.navigate(TriviaDestinations.HOME_ROUTE) {
                                    // Clear the back stack so user can't go back to this game
                                    popUpTo(TriviaDestinations.HOME_ROUTE) { inclusive = true }
                                }
                            }
                        )
                    } else {
                        // Show current question
                        QuestionScreen(
                            question = quiz.questions[currentQuestionIndex],
                            questionNumber = currentQuestionIndex,
                            totalQuestions = quiz.questions.size,
                            selectedAnswerIndex = selectedAnswerIndex,
                            showAnswer = showAnswer,
                            onAnswerSelected = { viewModel.selectAnswer(it) },
                            onNextQuestion = { viewModel.nextQuestion() }
                        )
                    }
                }
                is GameUiState.Error -> {
                    ErrorState(
                        errorMessage = (uiState as GameUiState.Error).message,
                        onRetry = { viewModel.retryLoading(topic) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        CircularProgressIndicator(
            color = TealAccent,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = "Generating trivia questions...",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
private fun ErrorState(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Error loading quiz: $errorMessage",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(
            onClick = onRetry,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(text = "Retry")
        }
    }
}
