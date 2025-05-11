package com.example.infitrivia.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.infitrivia.model.TriviaQuestion
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
      // Current topic
    private val _currentTopic = MutableStateFlow("")
    val currentTopic: StateFlow<String> = _currentTopic.asStateFlow()
    
    // Game state
    private val _questionsAnswered = MutableStateFlow(0)
    val questionsAnswered: StateFlow<Int> = _questionsAnswered.asStateFlow()
    
    private val _selectedAnswerIndex = MutableStateFlow<Int?>(null)
    val selectedAnswerIndex: StateFlow<Int?> = _selectedAnswerIndex.asStateFlow()
    
    private val _showAnswer = MutableStateFlow(false)
    val showAnswer: StateFlow<Boolean> = _showAnswer.asStateFlow()
    
    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()
    
    private val _isLoadingNextQuestion = MutableStateFlow(false)
    val isLoadingNextQuestion: StateFlow<Boolean> = _isLoadingNextQuestion.asStateFlow()
      // Track previously asked questions to avoid repetition
    private val previousQuestions = mutableListOf<String>()
      /**
     * Loads the first question for a new game on the specified topic
     */
    fun loadQuiz(topic: String) {
        // Reset game state
        _currentTopic.value = topic
        _questionsAnswered.value = 0
        _selectedAnswerIndex.value = null
        _showAnswer.value = false
        _score.value = 0
        _isLoadingNextQuestion.value = false
        
        // Clear question history when starting a new topic
        previousQuestions.clear()
        
        // Start loading
        _uiState.value = GameUiState.Loading
        
        // First validate the topic, then load questions
        viewModelScope.launch {
            try {
                val validationResult = geminiService.validateTopic(topic)
                
                if (validationResult.isValid) {
                    // Topic is valid, load the first question
                    loadNextQuestion()
                } else {
                    // Topic is invalid, show error
                    _uiState.value = GameUiState.Error(
                        validationResult.message.ifEmpty { 
                            "This topic isn't suitable for trivia questions. Please try a different topic." 
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = GameUiState.Error("Error validating topic: ${e.message ?: "Unknown error"}")
            }
        }
    }
    
    /**
     * Load the next question for the current topic
     */
    private fun loadNextQuestion() {
        if (_isLoadingNextQuestion.value) return
        
        _isLoadingNextQuestion.value = true
        
        viewModelScope.launch {
            try {
                // Pass previously asked questions to avoid repetition
                val questionResult = geminiService.generateSingleQuestion(
                    topic = _currentTopic.value,
                    previousQuestions = previousQuestions.toList()
                )
                
                if (questionResult.isSuccess) {
                    val question = questionResult.question!!
                    
                    // Add this question to our history to avoid repetition
                    previousQuestions.add(question.question)
                    
                    // Keep the history at a reasonable size to avoid token limit issues
                    if (previousQuestions.size > 20) {
                        // Keep only the 15 most recent questions if we exceed 20
                        while (previousQuestions.size > 15) {
                            previousQuestions.removeAt(0)
                        }
                    }
                    
                    // Create or update the quiz state with the new question
                    val currentState = _uiState.value
                    if (currentState is GameUiState.Success) {
                        // Add the new question to the existing quiz
                        val updatedQuestions = currentState.currentQuestion?.let {
                            listOf(question)
                        } ?: listOf(question)
                        
                        _uiState.value = GameUiState.Success(
                            currentQuestion = question,
                            questionsAnswered = _questionsAnswered.value
                        )
                    } else {
                        // First question
                        _uiState.value = GameUiState.Success(
                            currentQuestion = question,
                            questionsAnswered = _questionsAnswered.value
                        )
                    }
                } else {
                    _uiState.value = GameUiState.Error(questionResult.errorMessage)
                }
            } catch (e: Exception) {
                _uiState.value = GameUiState.Error("Error loading question: ${e.message ?: "Unknown error"}")
            } finally {
                _isLoadingNextQuestion.value = false
            }
        }
    }

    /**
     * Retry loading after an error
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
                val currentQuestion = currentState.currentQuestion
                if (currentQuestion.isCorrectAnswer(answerIndex)) {
                    _score.value = _score.value + 1
                }
            }
        }
    }
    
    /**
     * Move to the next question by generating a new one
     */
    fun nextQuestion() {
        // Increment questions answered counter
        _questionsAnswered.value = _questionsAnswered.value + 1
        
        // Reset selection state
        _selectedAnswerIndex.value = null
        _showAnswer.value = false
        
        // Load next question
        loadNextQuestion()
    }
      /**
     * Start a new game with the same topic
     */
    fun restartQuiz() {
        // Clear question history when restarting
        previousQuestions.clear()
        loadQuiz(_currentTopic.value)
    }
}

/**
 * UI states for the game screen
 */
sealed class GameUiState {
    /**
     * Loading state while question is being generated
     */
    data object Loading : GameUiState()
    
    /**
     * Success state with current question data
     */
    data class Success(
        val currentQuestion: TriviaQuestion,
        val questionsAnswered: Int
    ) : GameUiState()
    
    /**
     * Error state when question generation fails
     */
    data class Error(val message: String) : GameUiState()
}
