package com.example.fitgenius.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.fitgenius.data.*
import com.example.fitgenius.ui.theme.MutedGreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    userProfile: UserProfile?,
    aiResponse: AIResponse?,
    isLoading: Boolean,
    errorMessage: String?
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FitGenius", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { /* TODO: Implementar logout */ }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Salir")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MutedGreen)
            )
        },
    ) { paddingValues ->
        Column(modifier = Modifier
            .padding(paddingValues)
            .background(MutedGreen)) {
            PrimaryTabRow(selectedTabIndex = pagerState.currentPage, containerColor = MutedGreen) {
                Tab(selected = pagerState.currentPage == 0, onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } }, text = { Text("Inicio") })
                Tab(selected = pagerState.currentPage == 1, onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } }, text = { Text("Rutina") })
                Tab(selected = pagerState.currentPage == 2, onClick = { coroutineScope.launch { pagerState.animateScrollToPage(2) } }, text = { Text("Nutrición") })
                Tab(selected = pagerState.currentPage == 3, onClick = { coroutineScope.launch { pagerState.animateScrollToPage(3) } }, text = { Text("Progreso") })
            }

            HorizontalPager(state = pagerState) {
                page ->
                when (page) {
                    0 -> GeneralScreen(
                        userProfile = userProfile,
                        aiResponse = aiResponse,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        onNavigateToTab = { tabIndex ->
                            coroutineScope.launch { pagerState.animateScrollToPage(tabIndex) }
                        }
                    )
                    1 -> RoutineScreen(aiResponse = aiResponse)
                    2 -> DietScreen(aiResponse = aiResponse)
                    3 -> ProgressScreen()
                }
            }
        }
    }
}

@Composable
fun GeneralScreen(
    userProfile: UserProfile?,
    aiResponse: AIResponse?,
    isLoading: Boolean,
    errorMessage: String?,
    onNavigateToTab: (Int) -> Unit
) {
    when {
        isLoading -> LoadingState()
        errorMessage != null -> ErrorState(errorMessage) { /* TODO: Retry logic */ }
        aiResponse != null && userProfile != null -> {
            val weeklyWorkout = remember(aiResponse.routine) { parseWorkout(aiResponse.routine) }
            val weeklyNutrition = remember(aiResponse.diet) { parseNutrition(aiResponse.diet) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MutedGreen)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProfileSummaryCard(userProfile = userProfile)
                weeklyWorkout?.let {
                    TrainingSummaryCard(it) { onNavigateToTab(1) }
                }
                weeklyNutrition?.let {
                    NutritionSummaryCard(it) { onNavigateToTab(2) }
                }
                ProgressSummaryCard { onNavigateToTab(3) }
            }
        }
        else -> NoPlanState { /* TODO: Navigate to plan creation */ }
    }
}

@Composable
fun ProfileSummaryCard(userProfile: UserProfile) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AccountCircle, contentDescription = "Tu Perfil", tint = MaterialTheme.colorScheme.primary)
                Text("Tu Perfil", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileInfoChip("Objetivo", userProfile.goal, Icons.Default.CheckCircle, Modifier.weight(1f))
                ProfileInfoChip("Nivel", userProfile.activityLevel, Icons.AutoMirrored.Filled.TrendingUp, Modifier.weight(1f))
            }
            Row {
                ProfileInfoChip("Peso", "${userProfile.weight} kg", Icons.Default.FitnessCenter, Modifier.fillMaxWidth(0.5f).padding(end=6.dp))
            }
        }
    }
}

@Composable
fun ProfileInfoChip(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, MutedGreen), modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingSummaryCard(weeklyWorkout: WeeklyWorkout, onNavigate: () -> Unit) {
    val todayWorkout = weeklyWorkout.week.firstOrNull { it.day.equals("Lunes", ignoreCase = true) } // TODO: Get today's workout dynamically

    Card(onClick = onNavigate, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.FitnessCenter, contentDescription = "Entrenamiento", tint = MaterialTheme.colorScheme.primary)
                    Text("Entrenamiento de Hoy", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                if (todayWorkout != null && todayWorkout.exercises.isNotEmpty()) {
                    Text(todayWorkout.name, fontWeight = FontWeight.Bold)
                    Text("${todayWorkout.exercises.size} ejercicios • ${todayWorkout.duration} min", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    FilterChip(selected = false, onClick = {}, label = { Text("¡A por ello!") })
                } else {
                    Text("Día de descanso", style = MaterialTheme.typography.bodyMedium)
                    Text("Aprovecha para recuperar energía", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

@Composable
fun NutritionSummaryCard(weeklyNutrition: WeeklyNutrition, onNavigate: () -> Unit) {
    val todayNutrition = weeklyNutrition.week.firstOrNull { it.day.equals("Lunes", ignoreCase = true) } // TODO: Get today's nutrition dynamically

    Card(onClick = onNavigate, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Fastfood, contentDescription = "Nutrición", tint = Color.Red.copy(alpha = 0.7f))
                    Text("Nutrición de Hoy", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                todayNutrition?.summary?.let {
                    Text("${it.totalCalories} kcal", fontWeight = FontWeight.Bold)
                    LinearProgressIndicator(progress = { 1f }, modifier = Modifier.fillMaxWidth()) // Placeholder, no tenemos datos de consumo
                    Text("P: ${it.protein}g C: ${it.carbs}g G: ${it.fats}g", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressSummaryCard(onNavigate: () -> Unit) {
    Card(onClick = onNavigate, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.TrendingUp, contentDescription = "Progreso", tint = Color(0xFFFFA000))
                    Text("Tu Progreso", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Text("Registra tu peso y sigue tu evolución", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                FilterChip(selected = false, onClick = {}, label = { Text("Ver estadísticas") })
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

@Composable
fun NoPlanState(onCreatePlan: () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxSize().background(MutedGreen),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Dns, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text(
            "Aún no tienes un plan",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            "Cuando tu plan esté listo, aparecerá aquí.",
            style = MaterialTheme.typography.bodyLarge, color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onCreatePlan) {
            Icon(Icons.Default.Refresh, contentDescription = "Generar Plan")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generar Plan Ahora")
        }
    }
}

@Composable
fun LoadingState() {
    Column(
        modifier = Modifier.fillMaxSize().background(MutedGreen),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(16.dp))
        Text("Creando tu plan personalizado...", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text("Esto puede tardar un minuto.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
    }
}

@Composable
fun ErrorState(msg: String, onRetry: () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxSize().background(MutedGreen),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.CloudOff, contentDescription = null, tint = Color.Red, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text(
            "¡Ups! Algo ha ido mal",
            style = MaterialTheme.typography.headlineSmall.copy(color = Color.Red)
        )
        Text(msg, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Reintentar") }
    }
}
