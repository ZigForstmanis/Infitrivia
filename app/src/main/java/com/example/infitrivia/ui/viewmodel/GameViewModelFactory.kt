package com.example.infitrivia.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.infitrivia.config.ApiConfig
import com.example.infitrivia.service.GeminiService

/**
 * Factory for creating GameViewModel instances with the required dependencies
 */
class GameViewModelFactory : ViewModelProvider.Factory {
    
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            val geminiService = GeminiService(ApiConfig.GEMINI_API_KEY)
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(geminiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
