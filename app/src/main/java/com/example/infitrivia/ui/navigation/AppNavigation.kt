package com.example.infitrivia.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.infitrivia.ui.common.GradientBackground
import com.example.infitrivia.ui.common.SimpleBackground
import com.example.infitrivia.ui.screens.GameScreen
import com.example.infitrivia.ui.screens.InfitriviaHomeScreen
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Available navigation routes in the app
 */
object TriviaDestinations {
    const val HOME_ROUTE = "home"
    const val GAME_ROUTE = "game/{topic}"
    
    // Helper functions to create destination with arguments
    fun createGameRoute(topic: String): String {
        val encodedTopic = URLEncoder.encode(topic, StandardCharsets.UTF_8.toString())
        return "game/$encodedTopic"
    }
}

/**
 * Main app navigation container
 */
@Composable
fun TriviaApp() {
    val navController = rememberNavController()
    
    Scaffold { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TriviaDestinations.HOME_ROUTE
        ) {
            composable(TriviaDestinations.HOME_ROUTE) {
                GradientBackground(
                    modifier = Modifier.padding(innerPadding)
                ) {
                    InfitriviaHomeScreen(
                        onStartTrivia = { topic ->
                            navController.navigate(TriviaDestinations.createGameRoute(topic))
                        }
                    )
                }
            }
            
            composable(
                route = TriviaDestinations.GAME_ROUTE,
                arguments = listOf(
                    navArgument("topic") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val encodedTopic = backStackEntry.arguments?.getString("topic") ?: ""
                val topic = URLDecoder.decode(encodedTopic, StandardCharsets.UTF_8.toString())
                SimpleBackground(
                    modifier = Modifier.padding(innerPadding)
                ) {
                    GameScreen(topic = topic)
                }
            }
        }
    }
}