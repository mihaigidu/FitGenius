package com.example.fitgenius.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fitgenius.ScalewayAIService
import com.example.fitgenius.data.AIResponse
import com.example.fitgenius.data.UserProfile
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, userProfile: UserProfile?) {
    val scope = rememberCoroutineScope()
    val scalewayAIService = remember { ScalewayAIService() }

    var aiResponse by remember { mutableStateOf<AIResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userProfile) {
        if (userProfile != null) {
            scope.launch {
                isLoading = true
                errorMessage = null
                try {
                    aiResponse = scalewayAIService.generateRoutineAndDiet(userProfile)
                } catch (e: Exception) {
                    errorMessage = "Error: ${e.message}"
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tu Plan Personalizado") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (userProfile == null) {
                Text("No se ha proporcionado un perfil de usuario.")
                Spacer(Modifier.height(16.dp))
                Button(onClick = { navController.navigate("register") { popUpTo("login") } }) {
                    Text("Crear un Perfil")
                }
            } else if (isLoading) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("Generando tu plan con IA...", style = MaterialTheme.typography.bodyLarge)
                Text("Esto puede tardar hasta un minuto.", style = MaterialTheme.typography.bodySmall)
            } else if (errorMessage != null) {
                Text("Ha ocurrido un error:", color = MaterialTheme.colorScheme.error)
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
            } else if (aiResponse != null) {
                Tabs(aiResponse!!)
            }
        }
    }
}

@Composable
fun Tabs(aiResponse: AIResponse) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Rutina", "Dieta")

    Column {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        when (selectedTab) {
            0 -> Content(aiResponse.routine)
            1 -> Content(aiResponse.diet)
        }
    }
}

@Composable
fun Content(text: String) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}
