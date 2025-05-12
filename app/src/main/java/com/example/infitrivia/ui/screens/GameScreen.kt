package com.example.infitrivia.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.infitrivia.ui.components.ErrorState
import com.example.infitrivia.ui.components.GameHeader
import com.example.infitrivia.ui.components.LoadingState
import com.example.infitrivia.ui.components.NextQuestionLoadingIndicator
import com.example.infitrivia.ui.components.SuccessState
import com.example.infitrivia.ui.navigation.TriviaDestinations
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
        // Header
        GameHeader(
            topic = topic,
            uiState = uiState,
            score = score,
            questionsAnswered = questionsAnswered
        )
        
        // Content based on UI state
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                is GameUiState.Loading -> {
                    LoadingState(isFirstLoad = questionsAnswered == 0)
                }
                is GameUiState.Success -> {
                    val gameState = uiState as GameUiState.Success
                    val currentQuestion = gameState.currentQuestion
                    
                    SuccessState(
                        currentQuestion = currentQuestion,
                        questionNumber = questionsAnswered + 1, // Add 1 for human-readable question number
                        selectedAnswerIndex = selectedAnswerIndex,
                        showAnswer = showAnswer,
                        isLoadingNextQuestion = isLoadingNextQuestion,
                        onAnswerSelected = { viewModel.selectAnswer(it) },
                        onNextQuestion = { 
                            if (!isLoadingNextQuestion) {
                                viewModel.nextQuestion() 
                            }
                        },
                        onEndGame = { 
                            navController.navigate(TriviaDestinations.HOME_ROUTE) {
                                popUpTo(TriviaDestinations.HOME_ROUTE) { inclusive = true }
                            }
                        }
                    )
                    
                    // Show loading indicator when generating next question
                    if (isLoadingNextQuestion) {
                        NextQuestionLoadingIndicator(
                            showAnswer = showAnswer
                        )
                    }
                }
                is GameUiState.Error -> {
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


