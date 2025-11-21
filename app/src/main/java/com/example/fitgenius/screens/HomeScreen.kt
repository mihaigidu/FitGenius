package com.example.fitgenius.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.fitgenius.data.*
import com.example.fitgenius.ui.theme.*
import com.example.fitgenius.utils.ExcelExportService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

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
                title = { Text("FitGenius", fontWeight = FontWeight.Bold) },
                actions = {
                    if (aiResponse != null) {
                        IconButton(onClick = {
                            val calendar = Calendar.getInstance()
                            val week = calendar.get(Calendar.WEEK_OF_YEAR)
                            val year = calendar.get(Calendar.YEAR)
                            val fileName = "Plan_FitGenius_S${week}_${year}.xlsx"
                            exportLauncher.launch(fileName)
                        }) {
                            Icon(Icons.Default.FileDownload, contentDescription = "Exportar Excel")
                        }
                    }
                    IconButton(onClick = { navController.navigate("profile") }) {
                        if (userProfile?.profileImageUri != null) {
                            AsyncImage(
                                model = Uri.parse(userProfile.profileImageUri),
                                contentDescription = "Perfil",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Perfil", modifier = Modifier.size(32.dp))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = MaterialTheme.colorScheme.secondary
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AccountCircle, contentDescription = "Tu Perfil", tint = MaterialTheme.colorScheme.primary)
                Text("Tu Perfil", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileInfoChip("Objetivo", userProfile.goal, Icons.Default.CheckCircle, Modifier.weight(1f))
                ProfileInfoChip("Nivel", userProfile.activityLevel, Icons.Default.TrendingUp, Modifier.weight(1f)) // CORREGIDO: Icono
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        border = BorderStroke(1.dp, Color.LightGray),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
fun TrainingSummaryCard(weeklyWorkout: WeeklyWorkout, onNavigate: () -> Unit) {
    val today = SimpleDateFormat("EEEE", Locale.forLanguageTag("es-ES")).format(Date())
    val todayWorkout = weeklyWorkout.week.firstOrNull { it.day.equals(today, ignoreCase = true) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onNavigate),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.FitnessCenter, contentDescription = "Entrenamiento", tint = MaterialTheme.colorScheme.primary)
                    Text("Entrenamiento de Hoy", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                if (todayWorkout != null && todayWorkout.exercises.isNotEmpty()) {
                    Text(todayWorkout.name, fontWeight = FontWeight.Bold)
                    Text("${todayWorkout.exercises.size} ejercicios • ${todayWorkout.duration} min", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                } else {
                    Text("Día de descanso", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text("Aprovecha para recuperar energía", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
fun NutritionSummaryCard(weeklyNutrition: WeeklyNutrition, onNavigate: () -> Unit) {
    val today = SimpleDateFormat("EEEE", Locale.forLanguageTag("es-ES")).format(Date())
    val todayNutrition = weeklyNutrition.week.firstOrNull { it.day.equals(today, ignoreCase = true) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onNavigate),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Fastfood, contentDescription = "Nutrición", tint = MaterialTheme.colorScheme.primary)
                    Text("Nutrición de Hoy", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                todayNutrition?.summary?.let {
                    Text("${it.totalCalories} kcal", fontWeight = FontWeight.Bold)
                    LinearProgressIndicator(progress = { 1f }, modifier = Modifier.fillMaxWidth())
                    Text("P: ${it.protein}g C: ${it.carbs}g G: ${it.fats}g", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
fun ProgressSummaryCard(onNavigate: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onNavigate),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.TrendingUp, contentDescription = "Progreso", tint = MaterialTheme.colorScheme.primary)
                    Text("Tu Progreso", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Text("Registra tu peso y sigue tu evolución", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
fun NoPlanState(onCreatePlan: () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Dns, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text(
            "Aún no tienes un plan",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Cuando tu plan esté listo, aparecerá aquí.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onCreatePlan, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
            Icon(Icons.Default.Refresh, contentDescription = "Generar Plan", tint = MaterialTheme.colorScheme.onSecondary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generar Plan Ahora", color = MaterialTheme.colorScheme.onSecondary)
        }
    }
}

@Composable
fun LoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(16.dp))
        Text("Creando tu plan personalizado...", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
        Text("Esto puede tardar un minuto.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
fun ErrorState(msg: String, onRetry: () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.CloudOff, contentDescription = null, tint = Color.Red, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text(
            "¡Ups! Algo ha ido mal",
            style = MaterialTheme.typography.headlineSmall.copy(color = Color.Red),
            fontWeight = FontWeight.Bold
        )
        Text(msg, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp), color = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Reintentar") }
    }
}
