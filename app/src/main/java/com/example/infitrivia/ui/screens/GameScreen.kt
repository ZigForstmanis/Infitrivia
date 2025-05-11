package com.example.infitrivia.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.infitrivia.ui.components.QuestionScreen
import com.example.infitrivia.ui.navigation.TriviaDestinations
import com.example.infitrivia.ui.theme.OrangeButton
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
    val questionsAnswered by viewModel.questionsAnswered.collectAsState()
    val selectedAnswerIndex by viewModel.selectedAnswerIndex.collectAsState()
    val showAnswer by viewModel.showAnswer.collectAsState()
    val score by viewModel.score.collectAsState()
    val isLoadingNextQuestion by viewModel.isLoadingNextQuestion.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with topic and score (only if question is loaded)
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
        
        // Content based on UI state
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp),
            contentAlignment = Alignment.Center
        ) {            when (uiState) {
                is GameUiState.Loading -> {
                    LoadingState(isFirstLoad = questionsAnswered == 0)
                }
                is GameUiState.Success -> {
                    val gameState = uiState as GameUiState.Success
                    val currentQuestion = gameState.currentQuestion
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Show current question
                        QuestionScreen(
                            question = currentQuestion,
                            questionNumber = questionsAnswered + 1, // Add 1 for human-readable question number
                            totalQuestions = 0, // We don't have a total since it's indefinite
                            selectedAnswerIndex = selectedAnswerIndex,
                            showAnswer = showAnswer,
                            onAnswerSelected = { viewModel.selectAnswer(it) },
                            onNextQuestion = { 
                                if (!isLoadingNextQuestion) {
                                    viewModel.nextQuestion() 
                                }
                            },
                            isLoadingNextQuestion = isLoadingNextQuestion && showAnswer
                        )
                        
                        // Home button
                        Button(
                            onClick = { 
                                navController.navigate(TriviaDestinations.HOME_ROUTE) {
                                    popUpTo(TriviaDestinations.HOME_ROUTE) { inclusive = true }
                                }
                            },
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
                      // Show loading indicator when generating next question
                    if (isLoadingNextQuestion) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = TealAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                                
                                if (showAnswer) {
                                    Text(
                                        text = "Generating next question...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }                is GameUiState.Error -> {
                    ErrorState(
                        errorMessage = (uiState as GameUiState.Error).message,
                        onRetry = { viewModel.retryLoading(topic) },
                        navController = navController
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingState(isFirstLoad: Boolean = true) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        CircularProgressIndicator(
            color = TealAccent,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = if (isFirstLoad) "Generating trivia questions..." else "Creating next question...",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 16.dp)
        )
        
        if (isFirstLoad) {
            Text(
                text = "This may take a few moments",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun ErrorState(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController()
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
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
            textAlign = TextAlign.Center,
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
