package com.example.fitgenius

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.fitgenius.data.AIResponse
import com.example.fitgenius.data.UserProfile
import com.example.fitgenius.ui.theme.FitGeniusTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitGeniusTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    var currentUserProfile by remember { mutableStateOf<UserProfile?>(null) }
                    var aiResponse by remember { mutableStateOf<AIResponse?>(null) }
                    var isLoading by remember { mutableStateOf(false) }
                    var errorMessage by remember { mutableStateOf<String?>(null) }
                    val scalewayAIService = remember { ScalewayAIService() }

                    fun generatePlan(profile: UserProfile, isRegenerating: Boolean = false) {
                        if (isRegenerating) {
                            aiResponse = null // Limpia la respuesta anterior para mostrar el estado de carga
                        }
                        lifecycleScope.launch {
                            isLoading = true
                            errorMessage = null
                            try {
                                val newResponse = scalewayAIService.generateRoutineAndDiet(profile)
                                aiResponse = newResponse
                            } catch (e: Exception) {
                                errorMessage = e.message
                            } finally {
                                isLoading = false
                            }
                        }
                    }

                    NavGraph(
                        navController = navController,
                        currentUserProfile = currentUserProfile,
                        aiResponse = aiResponse,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        onUserRegistered = { profile ->
                            currentUserProfile = profile
                            // Navega al formulario de perfil, pero no genera el plan aquí
                        },
                        onProfileComplete = { profile, isEditing, imageUri ->
                            val updatedProfile = profile.copy(
                                profileImageUri = imageUri?.toString() ?: currentUserProfile?.profileImageUri
                            )
                            currentUserProfile = updatedProfile

                            // Genera el plan en segundo plano
                            generatePlan(updatedProfile, isRegenerating = !isEditing)

                            // Navega a la pantalla de inicio inmediatamente
                            if (!isEditing) {
                                navController.navigate("home") { 
                                    // Limpia el backstack para que el usuario no pueda volver al formulario
                                    popUpTo("auth") { inclusive = true } 
                                }
                            } else {
                                navController.popBackStack()
                            }
                        },
                        onGenerateNewPlan = {
                            currentUserProfile?.let { generatePlan(it, isRegenerating = true) }
                        }
                    )
                }
            }
        }
    }
}
