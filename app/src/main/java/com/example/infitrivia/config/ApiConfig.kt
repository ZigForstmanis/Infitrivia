package com.example.infitrivia.config

import com.example.infitrivia.BuildConfig

/**
 * Configuration constants for API keys and other settings
 * This uses BuildConfig values that come from local.properties
 */
object ApiConfig {
    /**
     * Gemini API key loaded from BuildConfig
     * The actual key is stored in local.properties which is git-ignored
     */
    val GEMINI_API_KEY: String = BuildConfig.GEMINI_API_KEY
}
