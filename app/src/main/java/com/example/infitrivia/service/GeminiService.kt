package com.example.infitrivia.service

import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
