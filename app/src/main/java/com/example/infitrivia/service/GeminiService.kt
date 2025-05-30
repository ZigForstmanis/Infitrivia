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
    }    
    
    /**
     * Generates a single trivia question for the specified topic
     * 
     * @param topic The topic to generate a question about
     * @param previousQuestions List of previously asked question texts to avoid repetition
     * @param retryCount Number of retries attempted for invalid questions
     * @return QuestionResult containing question data or error message
     */
    suspend fun generateSingleQuestion(
        topic: String,
        previousQuestions: List<String> = emptyList(),
        retryCount: Int = 0
    ): QuestionResult = withContext(Dispatchers.IO) {
        try {
            // Maximum number of retries if questions fail validation
            val maxRetries = 2
            
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
                
                IMPORTANT RULES FOR ANSWER OPTIONS:
                - All 5 answer options must be SEMANTICALLY DISTINCT with fundamentally different meanings
                - Never include variations of the same name or concept as different options (e.g. "Franklin D. Roosevelt" and "Roosevelt" should not both be options)
                - Each answer option should represent a completely different choice - not just alternate spellings, synonyms, or expressions of the same concept
                - For people's names, use full names consistently (not just first name in one option and full name in another)
                - For dates or numbers, ensure they are significantly different from each other
                
                Create a question that is DIFFERENT from any previously asked questions.
                Generate a unique, creative question that explores different aspects of the topic.
                Keep the question fun, interesting, and appropriate for all ages.$previousQuestionsText
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
                
                // Validate that the options are semantically distinct
                val validationError = validateAnswerOptions(options)
                if (validationError != null) {
                    // If we've reached the max retries, give up and return the error
                    if (retryCount >= maxRetries) {
                        return@withContext QuestionResult(errorMessage = "$validationError (after $maxRetries retries)")
                    }
                    
                    // Otherwise, retry with an incremented retry count
                    return@withContext generateSingleQuestion(
                        topic = topic,
                        previousQuestions = previousQuestions + listOf(questionText), // Add this question text to avoid repeating it
                        retryCount = retryCount + 1
                    )
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
                    // If we've reached the max retries, give up and return the error
                    if (retryCount >= maxRetries) {
                        return@withContext QuestionResult(errorMessage = "Generated question data was incomplete or invalid (after $maxRetries retries)")
                    }
                    
                    // Otherwise, retry with an incremented retry count
                    return@withContext generateSingleQuestion(
                        topic = topic,
                        previousQuestions = previousQuestions + listOf(questionText),
                        retryCount = retryCount + 1
                    )
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
 * Validates that the answer options are semantically distinct
 * @param options List of answer options to validate
 * @return Error message if validation fails, null if options are valid
 */
private fun validateAnswerOptions(options: List<String>): String? {
    // Check for near-duplicate options
    for (i in options.indices) {
        for (j in i + 1 until options.size) {
            val optionA = options[i].lowercase().trim()
            val optionB = options[j].lowercase().trim()
            
            // Check for exact duplicates (should never happen but just in case)
            if (optionA == optionB) {
                return "Duplicate answer options found: '$optionA' appears multiple times"
            }
            
            // Check for options that are substrings of each other (likely variations of the same answer)
            if (optionA.contains(optionB) || optionB.contains(optionA)) {
                // Only flag if the shorter string is at least 4 characters (to avoid false positives with very short strings)
                val shorter = if (optionA.length < optionB.length) optionA else optionB
                if (shorter.length >= 4) {
                    return "Answer options contain similar entries: '${options[i]}' and '${options[j]}' appear to be variations of the same answer"
                }
            }
            
            // Check for names with partial overlapping components
            val wordsA = optionA.split(Regex("\\s+"))
            val wordsB = optionB.split(Regex("\\s+"))
            
            // Check if one is a partial version of a multi-word name (like "Roosevelt" vs "Franklin D. Roosevelt")
            val nameOverlap = wordsA.intersect(wordsB.toSet())
            if (nameOverlap.isNotEmpty() && 
                nameOverlap.any { it.length > 4 } && // Only consider significant words
                (wordsA.size > 1 || wordsB.size > 1)) { // At least one option should be multi-word
                
                // Check if the overlap is meaningful (like a surname)
                // We focus on the last words which are often surnames
                if (wordsA.lastOrNull() == wordsB.lastOrNull() && wordsA.lastOrNull()?.length ?: 0 > 3) {
                    return "Answer options contain similar names: '${options[i]}' and '${options[j]}' appear to be variations of the same person or entity"
                }
            }
        }
    }
    
    return null
}

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
