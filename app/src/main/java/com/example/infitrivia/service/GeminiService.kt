package com.example.infitrivia.service

import com.example.infitrivia.model.TriviaQuestion
import com.example.infitrivia.model.TriviaQuiz
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
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
        }    }
    
    /**
     * Generates a trivia quiz for the specified topic
     * 
     * @param topic The topic to generate questions about
     * @param numQuestions Number of questions to generate (default is 5)
     * @return QuizResult containing quiz data or error message
     */
    suspend fun generateQuiz(
        topic: String, 
        numQuestions: Int = TriviaQuiz.DEFAULT_QUIZ_SIZE
    ): QuizResult = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                Generate $numQuestions multiple-choice trivia questions about "$topic". 
                Each question should have exactly 5 answer options (labeled A through E), with only one correct answer.
                
                For each question, include:
                1. The question text
                2. 5 answer options (A through E)
                3. The correct answer (as the letter A-E)
                4. A brief factoid related to the question that will be shown after answering
                
                Format your response as valid JSON following this exact structure:
                {
                  "questions": [
                    {
                      "question": "Question text here?",
                      "options": ["Option A", "Option B", "Option C", "Option D", "Option E"],
                      "correctAnswer": "A", 
                      "factoid": "Interesting fact related to this question."
                    },
                    ...more questions...
                  ]
                }
                
                Keep the questions fun, interesting, and appropriate for all ages. Make sure the answer options are distinct from each other.
            """.trimIndent()
            
            val response = generativeModel.generateContent(prompt).text?.trim() ?: ""
            
            // Extract JSON from the response (remove any markdown code block markers if present)
            val jsonString = response.replace("```json", "")
                .replace("```", "")
                .trim()
            
            try {
                // Parse the JSON and convert to our model classes
                val jsonObject = JSONObject(jsonString)
                val questionsArray = jsonObject.getJSONArray("questions")
                val questions = mutableListOf<TriviaQuestion>()
                
                for (i in 0 until questionsArray.length()) {
                    val questionObj = questionsArray.getJSONObject(i)
                    val questionText = questionObj.getString("question")
                    val factoid = questionObj.getString("factoid")
                    
                    // Get answer options
                    val optionsArray = questionObj.getJSONArray("options") 
                    val options = mutableListOf<String>()
                    for (j in 0 until optionsArray.length()) {
                        options.add(optionsArray.getString(j))
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
                    
                    questions.add(
                        TriviaQuestion(
                            question = questionText,
                            options = options,
                            correctAnswerIndex = correctAnswerIndex,
                            factoid = factoid
                        )
                    )
                }
                
                val quiz = TriviaQuiz(topic, questions)
                if (quiz.isValid()) {
                    return@withContext QuizResult(quiz = quiz)
                } else {
                    return@withContext QuizResult(errorMessage = "Generated quiz data was incomplete or invalid")
                }
            } catch (e: Exception) {
                return@withContext QuizResult(errorMessage = "Failed to parse quiz data: ${e.message}")
            }
        } catch (e: Exception) {
            return@withContext QuizResult(errorMessage = "Error generating quiz: ${e.message ?: "Unknown error"}")
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
 * Result of quiz generation
 * @param quiz The generated trivia quiz (null if error occurred)
 * @param errorMessage Error message if quiz generation failed (empty if successful)
 */
data class QuizResult(
    val quiz: TriviaQuiz? = null,
    val errorMessage: String = ""
) {
    val isSuccess: Boolean
        get() = quiz != null && errorMessage.isEmpty()
}
