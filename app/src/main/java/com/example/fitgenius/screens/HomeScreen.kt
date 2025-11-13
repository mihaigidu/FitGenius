
package com.example.fitgenius.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fitgenius.ScalewayAIService
import com.example.fitgenius.UserProfile
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(navController: NavController, userProfile: UserProfile?) {
    var routine by remember { mutableStateOf("Pulsa el botón para generar tu rutina personalizada con IA") }
    var diet by remember { mutableStateOf("Pulsa el botón para generar tu dieta personalizada con IA") }
    var isLoading by remember { mutableStateOf(false) }
    var showDiet by remember { mutableStateOf(false) }

    val aiService = remember { ScalewayAIService() }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(Modifier.height(32.dp))
        Text(
            "¡Hola ${userProfile?.name ?: "Usuario"}! 👋",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Tu plan personalizado con IA",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))

        // Tabs para rutina y dieta
        TabRow(selectedTabIndex = if (showDiet) 1 else 0) {
            Tab(
                selected = !showDiet,
                onClick = { showDiet = false },
                text = { Text("Rutina 💪") }
            )
            Tab(
                selected = showDiet,
                onClick = { showDiet = true },
                text = { Text("Dieta 🥗") }
            )
        }

        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text("✨ Generando tu plan personalizado con IA...")
            Spacer(Modifier.height(8.dp))
            Text(
                "Analizando tu perfil y creando un plan único para ti...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Esto puede tomar 10-20 segundos",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (showDiet) diet else routine,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (userProfile != null) {
                    isLoading = true
                    coroutineScope.launch {
                        val response = aiService.generateRoutineAndDiet(userProfile)
                        routine = response.routine
                        diet = response.diet
                        isLoading = false
                    }
                } else {
                    routine = "❌ Error: No hay perfil de usuario disponible"
                    diet = "Por favor, completa el registro primero"
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("✨ Generar Plan con IA (Scaleway)")
        }

        Spacer(Modifier.height(8.dp))

        if (userProfile != null) {
            Text(
                "Perfil: ${userProfile.goal ?: "No definido"} | " +
                        "Actividad: ${userProfile.activityLevel ?: "No definido"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}