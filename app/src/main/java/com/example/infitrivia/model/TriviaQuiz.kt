package com.example.infitrivia.model

/**
 * Represents a complete trivia quiz for a specific topic
 * 
 * @property topic The topic of the quiz
 * @property questions List of trivia questions
 */
data class TriviaQuiz(
    val topic: String,
    val questions: List<TriviaQuestion>
) {
    /**
     * Checks if the quiz has valid data
     * 
     * @return True if the quiz is valid and can be presented to users
     */
    fun isValid(): Boolean {
        return topic.isNotBlank() &&
                questions.isNotEmpty() &&
                questions.all { it.isValid() }
    }
    
    companion object {
        /**
         * Default number of questions in a quiz
         */
        const val DEFAULT_QUIZ_SIZE = 5
    }
}
