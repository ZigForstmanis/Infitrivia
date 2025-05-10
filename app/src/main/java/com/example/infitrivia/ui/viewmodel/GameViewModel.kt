package com.example.infitrivia.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.infitrivia.model.TriviaQuiz
import com.example.infitrivia.service.GeminiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel to manage the trivia game state
 */
class GameViewModel(private val geminiService: GeminiService) : ViewModel() {

    // UI state
    private val _uiState = MutableStateFlow<GameUiState>(GameUiState.Loading)
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    
    // Game state
    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()
    
    private val _selectedAnswerIndex = MutableStateFlow<Int?>(null)
    val selectedAnswerIndex: StateFlow<Int?> = _selectedAnswerIndex.asStateFlow()
    
    private val _showAnswer = MutableStateFlow(false)
    val showAnswer: StateFlow<Boolean> = _showAnswer.asStateFlow()
    
    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()
    
    private val _gameComplete = MutableStateFlow(false)
    val gameComplete: StateFlow<Boolean> = _gameComplete.asStateFlow()

    /**
     * Loads a quiz for the specified topic
     */
    fun loadQuiz(topic: String) {
        // Reset game state
        _currentQuestionIndex.value = 0
        _selectedAnswerIndex.value = null
        _showAnswer.value = false
        _score.value = 0
        _gameComplete.value = false
        
        // Start loading
        _uiState.value = GameUiState.Loading
        
        viewModelScope.launch {
            try {
                val quizResult = geminiService.generateQuiz(topic)
                
                if (quizResult.isSuccess) {
                    _uiState.value = GameUiState.Success(quizResult.quiz!!)
                } else {
                    _uiState.value = GameUiState.Error(quizResult.errorMessage)
                }
            } catch (e: Exception) {
                _uiState.value = GameUiState.Error("Error loading quiz: ${e.message ?: "Unknown error"}")
            }
        }
    }

    /**
     * Retry loading the quiz after an error
     */
    fun retryLoading(topic: String) {
        loadQuiz(topic)
    }
    
    /**
     * Select an answer for the current question
     */
    fun selectAnswer(answerIndex: Int) {
        if (_selectedAnswerIndex.value == null && !_showAnswer.value) {
            _selectedAnswerIndex.value = answerIndex
            _showAnswer.value = true
            
            // Update score if correct
            val currentState = _uiState.value
            if (currentState is GameUiState.Success) {
                val currentQuestion = currentState.quiz.questions[_currentQuestionIndex.value]
                if (currentQuestion.isCorrectAnswer(answerIndex)) {
                    _score.value = _score.value + 1
                }
            }
        }
    }
    
    /**
     * Move to the next question or end the game if all questions have been answered
     */
    fun nextQuestion() {
        val currentState = _uiState.value
        if (currentState is GameUiState.Success) {
            val quiz = currentState.quiz
            if (_currentQuestionIndex.value < quiz.questions.size - 1) {
                // Move to next question
                _currentQuestionIndex.value = _currentQuestionIndex.value + 1
                _selectedAnswerIndex.value = null
                _showAnswer.value = false
            } else {
                // End of quiz
                _gameComplete.value = true
            }
        }
    }
    
    /**
     * Start a new game with the same quiz
     */
    fun restartQuiz() {
        _currentQuestionIndex.value = 0
        _selectedAnswerIndex.value = null
        _showAnswer.value = false
        _score.value = 0
        _gameComplete.value = false
    }
}

/**
 * UI states for the game screen
 */
sealed class GameUiState {
    /**
     * Loading state while quiz is being generated
     */
    data object Loading : GameUiState()
    
    /**
     * Success state with loaded quiz data
     */
    data class Success(val quiz: TriviaQuiz) : GameUiState()
    
    /**
     * Error state when quiz generation fails
     */
    data class Error(val message: String) : GameUiState()
}
