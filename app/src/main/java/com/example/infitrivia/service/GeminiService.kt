package com.example.infitrivia.service

import com.example.infitrivia.model.TriviaQuestion
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Service class to handle interactions with Gemini API
 */
class GeminiService(private val apiKey: String) {
    
    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-pro",
            apiKey = apiKey
        )
    }
    
    /**
     * Validates if a topic is suitable for generating multiple choice trivia questions
     * @param topic The topic to validate
     * @return ValidationResult containing validation status and message
     */
    suspend fun validateTopic(topic: String): ValidationResult = withContext(Dispatchers.IO) {
        try {
            if (topic.isBlank()) {
                return@withContext ValidationResult(
                    isValid = false, 
                    message = "Topic cannot be empty"
                )
            }
            
            val prompt = """
                Is "$topic" a good topic for generating multiple choice trivia questions?
                Respond with ONLY "YES" if it's a good topic, or "NO" followed by a brief reason if it's not suitable.
                A good topic should be specific enough to generate interesting questions but broad enough to create at least 5 different questions.
            """.trimIndent()
            
            val response = generativeModel.generateContent(prompt).text?.trim() ?: ""
            
            return@withContext if (response.startsWith("YES", ignoreCase = true)) {
                ValidationResult(isValid = true)
            } else {
                val reason = if (response.length > 3) {
                    response.substring(2).trim()
                } else {
                    "Topic is not specific enough for creating trivia questions"
                }
                ValidationResult(isValid = false, message = reason)
            }
        } catch (e: Exception) {
            ValidationResult(
                isValid = false, 
                message = "Error validating topic: ${e.message ?: "Unknown error"}"
            )
        }
    }    /**
     * Generates a single trivia question for the specified topic
     * 
     * @param topic The topic to generate a question about
     * @param previousQuestions List of previously asked question texts to avoid repetition
     * @return QuestionResult containing question data or error message
     */
    suspend fun generateSingleQuestion(
        topic: String,
        previousQuestions: List<String> = emptyList()
    ): QuestionResult = withContext(Dispatchers.IO) {
        try {
            val previousQuestionsText = if (previousQuestions.isEmpty()) {
                ""
            } else {
                "\n\nPreviously asked questions (DO NOT ask these or similar questions again):\n" +
                previousQuestions.joinToString("\n") { "- $it" }
            }
            
            val prompt = """
                Generate a single multiple-choice trivia question about "$topic".
                The question should have exactly 5 answer options (labeled A through E), with only one correct answer.
                
                Include:
                1. The question text
                2. 5 answer options (A through E)
                3. The correct answer (as the letter A-E)
                4. A brief factoid related to the question that will be shown after answering
                
                Format your response as valid JSON following this exact structure:
                {
                  "question": "Question text here?",
                  "options": ["Option A", "Option B", "Option C", "Option D", "Option E"],
                  "correctAnswer": "A",
                  "factoid": "Interesting fact related to this question."
                }
                
                Create a question that is DIFFERENT from any previously asked questions.
                Generate a unique, creative question that explores different aspects of the topic.
                Keep the question fun, interesting, and appropriate for all ages.
                Make sure the answer options are distinct from each other.$previousQuestionsText
            """.trimIndent()
              // Add a timeout to prevent indefinite waiting
            val response = try {
                generativeModel.generateContent(prompt).text?.trim() ?: ""
            } catch (e: Exception) {
                return@withContext QuestionResult(
                    errorMessage = "Failed to generate question: ${e.message ?: "Connection error"}"
                )
            }
            
            if (response.isBlank()) {
                return@withContext QuestionResult(errorMessage = "No response received from AI model")
            }
            
            // Extract JSON from the response (remove any markdown code block markers if present)
            val jsonString = response.replace("```json", "")
                .replace("```", "")
                .trim()
            
            try {
                // Parse the JSON and convert to our model class
                val questionObj = JSONObject(jsonString)
                
                // Verify all required fields are present
                if (!questionObj.has("question") || !questionObj.has("options") || 
                    !questionObj.has("correctAnswer") || !questionObj.has("factoid")) {
                    return@withContext QuestionResult(errorMessage = "Incomplete question data received")
                }
                
                val questionText = questionObj.getString("question")
                val factoid = questionObj.getString("factoid")
                  // Get answer options
                val optionsArray = questionObj.getJSONArray("options") 
                val options = mutableListOf<String>()
                
                // Ensure we have exactly 5 options
                if (optionsArray.length() != 5) {
                    return@withContext QuestionResult(
                        errorMessage = "Expected 5 answer options, but received ${optionsArray.length()}"
                    )
                }
                
                for (j in 0 until optionsArray.length()) {
                    val option = optionsArray.getString(j)
                    if (option.isBlank()) {
                        return@withContext QuestionResult(errorMessage = "Answer option cannot be blank")
                    }
                    options.add(option)
                }
                
                // Get correct answer and convert to index
                val correctAnswerLetter = questionObj.getString("correctAnswer")
                val correctAnswerIndex = when (correctAnswerLetter.uppercase()) {
                    "A" -> 0
                    "B" -> 1
                    "C" -> 2
                    "D" -> 3 
                    "E" -> 4
                    else -> 0 // Default to first answer if invalid
                }
                
                val question = TriviaQuestion(
                    question = questionText,
                    options = options,
                    correctAnswerIndex = correctAnswerIndex,
                    factoid = factoid
                )
                
                if (question.isValid()) {
                    return@withContext QuestionResult(question = question)
                } else {
                    return@withContext QuestionResult(errorMessage = "Generated question data was incomplete or invalid")
                }
            } catch (e: Exception) {
                return@withContext QuestionResult(errorMessage = "Failed to parse question data: ${e.message}")
            }
        } catch (e: Exception) {
            return@withContext QuestionResult(errorMessage = "Error generating question: ${e.message ?: "Unknown error"}")
        }
    }
}

/**
 * Result of topic validation
 * @param isValid Whether the topic is valid for generating trivia questions
 * @param message Feedback message (empty if valid, explanation if invalid)
 */
data class ValidationResult(
    val isValid: Boolean,
    val message: String = ""
)

/**
 * Result of single question generation
 * @param question The generated trivia question (null if error occurred)
 * @param errorMessage Error message if question generation failed (empty if successful)
 */
data class QuestionResult(
    val question: TriviaQuestion? = null,
    val errorMessage: String = ""
) {
    val isSuccess: Boolean
        get() = question != null && errorMessage.isEmpty()
}
