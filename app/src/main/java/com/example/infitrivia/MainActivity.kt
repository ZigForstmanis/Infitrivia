package com.example.infitrivia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.infitrivia.ui.theme.InfitriviaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InfitriviaTheme {
                TriviaApp()
            }
        }
    }
}

@Composable
fun TriviaApp() {
    Scaffold { innerPadding ->
        GradientBackground(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            InfitriviaHomeScreen()
        }
    }
}

@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Use the requested hex colors
    val backgroundColor = Color(0xFF14122f)
    val rippleColor = Color(0xFF0d9488)  
    val edgeColor = Color(0xFF134e4a)
    
    // Simple ripple animation
    val infiniteTransition = rememberInfiniteTransition(label = "ripple")
    
    // Animate the radius of the ripple with custom easing
    val rippleSize by infiniteTransition.animateFloat(
        initialValue = 1750f,
        targetValue = 1850f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 20000,
                // Using DecelerateAccelerateEasing to slow down at the extremes
                easing = CubicBezierEasing(0.7f, 0.0f, 0.3f, 1.0f)
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ripple-size"
    )
    
    // Solid background with the requested color
    Box(
        modifier = modifier.background(backgroundColor)
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colorStops = arrayOf(
                            0.0f to backgroundColor,
                            0.5f to backgroundColor,
                            0.65f to edgeColor,
                            0.65f to rippleColor,
                            1f to backgroundColor
                        ),
                        center = Offset(x = 100f, y = 300f),
                        radius = rippleSize
                    )
                )
        ) {
            content()
        }
    }
}

@Composable
fun InfitriviaHomeScreen(modifier: Modifier = Modifier) {
    var topicText by rememberSaveable { mutableStateOf("") }
    
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top 40% with app title
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Infitrivia",
                style = MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Center,
                color = Color.White // Ensuring text is visible on gradient
            )
        }
        
        // Middle area with text input
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f),
            contentAlignment = Alignment.Center
        ) {
            TriviaTopicInput(
                value = topicText,
                onValueChange = { topicText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            )
        }
        
        // Bottom area with button positioned for thumb access
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.2f)
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = { /* Will handle topic validation */ },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Start",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun TriviaTopicInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    
    // Create a stacked layout with Box
    Box(
        modifier = modifier.zIndex(100f)
    ) {
        // Background with blur effect
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Color.Black.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(10.dp)
                )
                .blur(20.dp)
        )
        
        // Text field on top (without blur effect)
        TextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("Enter trivia topic") },
            modifier = Modifier
                .fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            textStyle = MaterialTheme.typography.bodyLarge,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
            )
        )
    }

}

@Preview(
    showBackground = true, 
    showSystemUi = true,
    name = "Infitrivia Home Screen"
)
@Composable
fun InfitriviaHomeScreenPreview() {
    InfitriviaTheme {
        TriviaApp()
    }
}