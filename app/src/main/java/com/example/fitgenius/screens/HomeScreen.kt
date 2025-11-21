package com.example.fitgenius.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.fitgenius.data.*
import com.example.fitgenius.ui.theme.*
import com.example.fitgenius.utils.ExcelExportService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    val context = LocalContext.current

    val weeklyWorkout = remember(aiResponse?.routine) { aiResponse?.routine?.let { parseWorkout(it) } }
    val weeklyNutrition = remember(aiResponse?.diet) { aiResponse?.diet?.let { parseNutrition(it) } }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri ->
        uri?.let {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        ExcelExportService().generateExcel(weeklyWorkout, weeklyNutrition, outputStream)
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Plan exportado con éxito", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error al exportar: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FitGenius", fontWeight = FontWeight.Bold, color = Color.White) },
                actions = {
                    if (aiResponse != null) {
                        IconButton(onClick = { exportLauncher.launch("Plan_FitGenius.xlsx") }) {
                            Icon(Icons.Default.FileDownload, contentDescription = "Exportar Excel", tint = Color.White)
                        }
                    }
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Perfil", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PetrolGreen)
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .background(DarkBackground)
        ) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = PetrolGreen,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = BrightBlue
                    )
                }
            ) {
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
                    .background(DarkBackground)
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
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AccountCircle, contentDescription = "Tu Perfil", tint = PetrolGreen)
                Text("Tu Perfil", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
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
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkBackground),
        border = BorderStroke(1.dp, PetrolGreen.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, contentDescription = title, tint = PetrolGreen)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
            Text(title, style = MaterialTheme.typography.labelMedium, color = MediumGrey)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingSummaryCard(weeklyWorkout: WeeklyWorkout, onNavigate: () -> Unit) {
    val todayWorkout = weeklyWorkout.week.firstOrNull { it.day.equals("Lunes", ignoreCase = true) } // TODO: Get today's workout dynamically

    Card(
        onClick = onNavigate,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.FitnessCenter, contentDescription = "Entrenamiento", tint = PetrolGreen)
                    Text("Entrenamiento de Hoy", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                }
                if (todayWorkout != null && todayWorkout.exercises.isNotEmpty()) {
                    Text(todayWorkout.name, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("${todayWorkout.exercises.size} ejercicios • ${todayWorkout.duration} min", style = MaterialTheme.typography.bodyMedium, color = MediumGrey)
                    FilterChip(selected = false, onClick = {}, label = { Text("¡A por ello!", color = Color.White) }, colors = FilterChipDefaults.filterChipColors(containerColor = BrightBlue))
                } else {
                    Text("Día de descanso", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    Text("Aprovecha para recuperar energía", style = MaterialTheme.typography.bodySmall, color = MediumGrey)
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

@Composable
fun NutritionSummaryCard(weeklyNutrition: WeeklyNutrition, onNavigate: () -> Unit) {
    val todayNutrition = weeklyNutrition.week.firstOrNull { it.day.equals("Lunes", ignoreCase = true) } // TODO: Get today's nutrition dynamically

    Card(
        onClick = onNavigate,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Fastfood, contentDescription = "Nutrición", tint = Color(0xFFFF7043)) // Orange-ish
                    Text("Nutrición de Hoy", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                }
                todayNutrition?.summary?.let {
                    Text("${it.totalCalories} kcal", fontWeight = FontWeight.Bold, color = Color.White)
                    LinearProgressIndicator(progress = { 1f }, modifier = Modifier.fillMaxWidth(), color = PetrolGreen, trackColor = DarkBackground)
                    Text("P: ${it.protein}g C: ${it.carbs}g G: ${it.fats}g", style = MaterialTheme.typography.bodyMedium, color = MediumGrey)
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressSummaryCard(onNavigate: () -> Unit) {
    Card(
        onClick = onNavigate,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.TrendingUp, contentDescription = "Progreso", tint = BrightBlue)
                    Text("Tu Progreso", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Text("Registra tu peso y sigue tu evolución", style = MaterialTheme.typography.bodyMedium, color = MediumGrey)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

@Composable
fun NoPlanState(onCreatePlan: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Dns, contentDescription = null, tint = MediumGrey, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text(
            "Aún no tienes un plan",
            style = MaterialTheme.typography.headlineMedium, color = Color.White
        )
        Text(
            "Cuando tu plan esté listo, aparecerá aquí.",
            style = MaterialTheme.typography.bodyLarge, color = MediumGrey,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onCreatePlan, colors = ButtonDefaults.buttonColors(containerColor = BrightBlue)) {
            Icon(Icons.Default.Refresh, contentDescription = "Generar Plan", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generar Plan Ahora", color = Color.White)
        }
    }
}

@Composable
fun LoadingState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = BrightBlue)
        Spacer(Modifier.height(16.dp))
        Text("Creando tu plan personalizado...", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, color = Color.White)
        Text("Esto puede tardar un minuto.", style = MaterialTheme.typography.bodyMedium, color = MediumGrey)
    }
}

@Composable
fun ErrorState(msg: String, onRetry: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.CloudOff, contentDescription = null, tint = Color.Red, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text(
            "¡Ups! Algo ha ido mal",
            style = MaterialTheme.typography.headlineSmall.copy(color = Color.Red)
        )
        Text(msg, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp), color = MediumGrey)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Reintentar") }
    }
}
