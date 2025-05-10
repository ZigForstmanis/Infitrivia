package com.example.infitrivia.model

/**
 * Represents a multiple choice trivia question
 *
 * @property question The trivia question text
 * @property options List of possible answer options
 * @property correctAnswerIndex Index of the correct answer in the options list (0-based)
 * @property factoid A related fact that appears after answering
 */
data class TriviaQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val factoid: String
) {
    /**
     * Validates that the question has valid data
     * 
     * @return True if the question data is valid
     */
    fun isValid(): Boolean {
        return question.isNotBlank() &&
                options.size == 5 &&
                options.all { it.isNotBlank() } &&
                correctAnswerIndex in options.indices &&
                factoid.isNotBlank()
    }
    
    /**
     * Checks if the given answer index is correct
     *
     * @param answerIndex The index of the selected answer
     * @return True if the selected answer is correct
     */
    fun isCorrectAnswer(answerIndex: Int): Boolean {
        return answerIndex == correctAnswerIndex
    }
    
    /**
     * Gets the text of the correct answer option
     */
    fun getCorrectAnswerText(): String {
        return options[correctAnswerIndex]
    }
    
    companion object {
        /**
         * Number of answer options for each trivia question
         */
        const val NUM_OPTIONS = 5
    }
}
