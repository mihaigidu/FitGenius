package com.example.fitgenius.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                    errorMessage = e.message
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Tu Plan FitGenius", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (userProfile == null) {
                // ... (No user profile state)
            } else if (isLoading) {
                LoadingState()
            } else if (errorMessage != null) {
                ErrorState(errorMessage, onRetry = { /* Add retry logic if needed */ })
            } else if (aiResponse != null) {
                PlanContent(aiResponse!!)
            }
        }
    }
}

@Composable
fun LoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(24.dp))
        Text("Generando tu plan personalizado...", style = MaterialTheme.typography.titleMedium)
        Text("Esto puede tomar hasta un minuto.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}

@Composable
fun ErrorState(errorMessage: String?, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Ups! Algo salió mal", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(8.dp))
        Text(errorMessage ?: "Error desconocido", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Reintentar")
        }
    }
}

@Composable
fun PlanContent(aiResponse: AIResponse) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Rutina" to Icons.Filled.FitnessCenter, "Dieta" to Icons.Filled.RestaurantMenu)

    Column(modifier = Modifier.padding(16.dp)) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { /* No indicator for a cleaner look */ },
            divider = { /* No divider */ }
        ) {
            tabs.forEachIndexed { index, (title, icon) ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    modifier = Modifier
                        .padding(8.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            if (selectedTab == index) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = icon, contentDescription = title, tint = if(selectedTab == index) Color.White else MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        if (selectedTab == index) {
                            Text(title, color = Color.White)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Box(modifier = Modifier.animateContentSize()) {
            when (selectedTab) {
                0 -> ContentCard(aiResponse.routine)
                1 -> ContentCard(aiResponse.diet)
            }
        }
    }
}

@Composable
fun ContentCard(text: String) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(text, style = MaterialTheme.typography.bodyLarge, lineHeight = 28.sp)
        }
    }
}
