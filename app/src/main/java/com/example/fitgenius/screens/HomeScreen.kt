package com.example.fitgenius.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.fitgenius.data.AIResponse
import com.example.fitgenius.data.UserProfile
import com.example.fitgenius.ui.theme.PetrolGreen
import kotlinx.coroutines.launch
import java.util.Calendar

data class DayPlan(val title: String, val content: String)

val LightGreenBackground = Color(0xFFF0FFF8)
val MutedGreen = Color(0xFFE6F4EA)

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

    val onNavigateToTab: (Int) -> Unit = { tabIndex ->
        coroutineScope.launch {
            pagerState.animateScrollToPage(tabIndex)
        }
    }

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
            TabRow(selectedTabIndex = pagerState.currentPage, containerColor = MutedGreen) {
                Tab(selected = pagerState.currentPage == 0, onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } }, text = { Text("Inicio") })
                Tab(selected = pagerState.currentPage == 1, onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } }, text = { Text("Rutina") })
                Tab(selected = pagerState.currentPage == 2, onClick = { coroutineScope.launch { pagerState.animateScrollToPage(2) } }, text = { Text("Nutrición") })
                Tab(selected = pagerState.currentPage == 3, onClick = { coroutineScope.launch { pagerState.animateScrollToPage(3) } }, text = { Text("Progreso") })
            }

            HorizontalPager(state = pagerState) {
                page ->
                when (page) {
                    0 -> GeneralScreen(userProfile = userProfile, aiResponse = aiResponse, isLoading = isLoading, errorMessage = errorMessage, onNavigateToTab = onNavigateToTab)
                    1 -> RoutineScreen(aiResponse = aiResponse)
                    2 -> DietScreen(aiResponse = aiResponse)
                    3 -> ProgressScreen()
                }
            }
        }
    }
}

@Composable
fun GeneralScreen(userProfile: UserProfile?, aiResponse: AIResponse?, isLoading: Boolean, errorMessage: String?, onNavigateToTab: (Int) -> Unit) {
    when {
        isLoading -> LoadingState()
        errorMessage != null -> ErrorState(errorMessage) { }
        aiResponse != null && userProfile != null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MutedGreen)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProfileSummaryCard(userProfile = userProfile)
                TrainingSummaryCard(aiResponse = aiResponse) { onNavigateToTab(1) }
                NutritionSummaryCard { onNavigateToTab(2) }
                ProgressSummaryCard { onNavigateToTab(3) }
                RecentActivitySection()
            }
        }
        else -> NoPlanState { }
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
                ProfileInfoChip("Nivel", userProfile.activityLevel, Icons.Default.TrendingUp, Modifier.weight(1f))
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
fun TrainingSummaryCard(aiResponse: AIResponse, onNavigate: () -> Unit) {
    val todayRoutine = remember(aiResponse.routine) { getTodayPlan(aiResponse.routine) }
    Card(onClick = onNavigate, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.FitnessCenter, contentDescription = "Entrenamiento", tint = MaterialTheme.colorScheme.primary)
                    Text("Entrenamiento", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                if (todayRoutine != null) {
                    Text("Hoy: ${todayRoutine.title}", fontWeight = FontWeight.Bold)
                    Text("4 ejercicios • 45 min", style = MaterialTheme.typography.bodyMedium, color = Color.Gray) // Placeholder
                    FilterChip(selected = false, onClick = {}, label = { Text("Listo para entrenar") })
                } else {
                    Text("Día de descanso.", style = MaterialTheme.typography.bodyMedium)
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionSummaryCard(onNavigate: () -> Unit) {
    Card(onClick = onNavigate, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Fastfood, contentDescription = "Nutrición", tint = Color.Red.copy(alpha = 0.7f))
                    Text("Nutrición", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Text("1,850 / 2,200 kcal", fontWeight = FontWeight.Bold)
                LinearProgressIndicator(progress = { 1850f / 2200f }, modifier = Modifier.fillMaxWidth())
                Text("Te quedan 350 kcal", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
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
                    Icon(Icons.Default.Whatshot, contentDescription = "Progreso", tint = Color(0xFFFFA000))
                    Text("Progreso", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Text("Racha: 7 días", fontWeight = FontWeight.Bold)
                Text("¡Sigue así!", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                FilterChip(selected = false, onClick = {}, label = { Text("🔥 En fuego") })
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

@Composable
fun RecentActivitySection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.History, contentDescription = "Actividad Reciente", tint = MaterialTheme.colorScheme.primary)
            Text("Actividad Reciente", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Text("Tu actividad de esta semana", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

        // Placeholder activities
        Card(colors=CardDefaults.cardColors(containerColor = LightGreenBackground)){
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)){
                Icon(Icons.Default.FitnessCenter, "", modifier = Modifier.background(Color.White, CircleShape).padding(8.dp))
                Column(Modifier.weight(1f)){
                    Text("Entrenamiento de Pierna", fontWeight = FontWeight.Bold)
                    Text("Ayer • 45 minutos", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Text("Completado", color = MaterialTheme.colorScheme.primary, modifier = Modifier.background(MutedGreen, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
         Card(colors=CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))){
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)){
                Icon(Icons.Default.Fastfood, "", modifier = Modifier.background(Color.White, CircleShape).padding(8.dp), tint=Color.Blue.copy(alpha=0.8f))
                Column(Modifier.weight(1f)){
                    Text("Meta calórica alcanzada", fontWeight = FontWeight.Bold)
                    Text("Hace 2 días", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Icon(Icons.Default.Check, contentDescription = "Completado", tint = Color.Blue.copy(alpha=0.8f))
            }
        }
    }
}


@Composable
fun RoutineScreen(aiResponse: AIResponse?) {
    if (aiResponse == null) {
        NoPlanState { }
        return
    }
    val routineDays = remember(aiResponse.routine) { parsePlan(aiResponse.routine) }
    DayPager(plans = routineDays, initialIndex = getTodayIndex())
}

@Composable
fun DietScreen(aiResponse: AIResponse?) {
    if (aiResponse == null) {
        NoPlanState { }
        return
    }
    val dietDays = remember(aiResponse.diet) { parsePlan(aiResponse.diet) }
    DayPager(plans = dietDays, initialIndex = getTodayIndex())
}

@Composable
fun ProgressScreen() {
    Box(modifier = Modifier.fillMaxSize().background(MutedGreen), contentAlignment = Alignment.Center) {
        Text("Próximamente...", style = MaterialTheme.typography.headlineMedium)
    }
}

fun getTodayIndex(): Int {
    val calendar = Calendar.getInstance()
    return (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
}

fun getTodayPlan(planText: String): DayPlan? {
    val plans = parsePlan(planText)
    val todayIndex = getTodayIndex()
    return plans.getOrNull(todayIndex)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DayPager(plans: List<DayPlan>, initialIndex: Int) {
    val pagerState = rememberPagerState(initialPage = initialIndex) { plans.size }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.background(MutedGreen),
        contentPadding = PaddingValues(horizontal = 32.dp, vertical=16.dp)
    ) { page ->
        DayCard(
            dayPlan = plans[page],
            isToday = page == initialIndex
        )
    }
}

@Composable
fun DayCard(dayPlan: DayPlan, isToday: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = dayPlan.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = PetrolGreen
                )
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp),
                thickness = 1.dp,
                color = PetrolGreen.copy(alpha = 0.4f)
            )

            Text(
                text = dayPlan.content,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.Black.copy(alpha = 0.85f),
                    lineHeight = 24.sp
                )
            )
        }
    }
}

fun parsePlan(planText: String): List<DayPlan> {
    if (planText.isBlank()) return emptyList()

    val dayPlans = mutableListOf<DayPlan>()
    var currentTitle: String? = null
    val currentContent = StringBuilder()

    val dayKeywords = listOf(
        "LUNES", "MARTES", "MIÉRCOLES", "JUEVES",
        "VIERNES", "SÁBADO", "DOMINGO",
        "DÍA 1", "DÍA 2", "DÍA 3", "DÍA 4", "DÍA 5", "DÍA 6", "DÍA 7"
    )

    planText.lines().forEach { line ->
        val trimmedLine = line.trim().removeSurrounding("**").trim()
        val foundKeyword = dayKeywords.find { trimmedLine.uppercase().startsWith(it) }

        if (foundKeyword != null) {
            if (currentTitle != null)
                dayPlans.add(DayPlan(currentTitle!!, currentContent.toString().trim()))

            currentTitle = trimmedLine
            currentContent.clear()
        } else if (currentTitle != null) {
            currentContent.append(line).append("\n")
        }
    }

    if (currentTitle != null)
        dayPlans.add(DayPlan(currentTitle!!, currentContent.toString().trim()))

    return dayPlans
}

@Composable
fun NoPlanState(onCreatePlan: () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxSize().background(MutedGreen),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Bienvenido a FitGenius",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            "Aún no tienes un plan generado.",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onCreatePlan
        ) {
            Text("Crear Plan", fontWeight = FontWeight.Bold)
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
        Text(
            "Generando tu plan personalizado...",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun ErrorState(msg: String, onRetry: () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxSize().background(MutedGreen),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Oops, algo fue mal",
            style = MaterialTheme.typography.headlineSmall.copy(color = Color.Red)
        )
        Spacer(Modifier.height(8.dp))
        Text(msg)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Reintentar") }
    }
}
