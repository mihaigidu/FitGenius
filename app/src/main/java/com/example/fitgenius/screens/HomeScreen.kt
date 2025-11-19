package com.example.fitgenius.screens

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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.fitgenius.data.AIResponse
import com.example.fitgenius.data.UserProfile
import com.example.fitgenius.ui.theme.BrightBlue
import com.example.fitgenius.ui.theme.PetrolGreen
import com.example.fitgenius.ui.theme.PureWhite
import kotlinx.coroutines.launch
import java.util.Calendar

data class DayPlan(val title: String, val content: String)

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
                }
            )
        },
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(selectedTabIndex = pagerState.currentPage) {
                Tab(selected = pagerState.currentPage == 0, onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } }, text = { Text("General") })
                Tab(selected = pagerState.currentPage == 1, onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } }, text = { Text("Rutina") })
                Tab(selected = pagerState.currentPage == 2, onClick = { coroutineScope.launch { pagerState.animateScrollToPage(2) } }, text = { Text("Dieta") })
                Tab(selected = pagerState.currentPage == 3, onClick = { coroutineScope.launch { pagerState.animateScrollToPage(3) } }, text = { Text("Progreso") })
            }

            HorizontalPager(state = pagerState) {
                page ->
                when (page) {
                    0 -> GeneralScreen(userProfile = userProfile, aiResponse = aiResponse, isLoading = isLoading, errorMessage = errorMessage)
                    1 -> RoutineScreen(aiResponse = aiResponse)
                    2 -> DietScreen(aiResponse = aiResponse)
                    3 -> ProgressScreen()
                }
            }
        }
    }
}

@Composable
fun GeneralScreen(userProfile: UserProfile?, aiResponse: AIResponse?, isLoading: Boolean, errorMessage: String?) {
    when {
        isLoading -> LoadingState()
        errorMessage != null -> ErrorState(errorMessage) { }
        aiResponse != null && userProfile != null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Hola, ${userProfile.name}", style = MaterialTheme.typography.headlineSmall)
                ProfileSummaryCard(userProfile = userProfile)
                TrainingSummaryCard(aiResponse = aiResponse)
                NutritionSummaryCard(aiResponse = aiResponse)
            }
        }
        else -> NoPlanState { }
    }
}

@Composable
fun ProfileSummaryCard(userProfile: UserProfile) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Tu Perfil", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                ProfileInfoChip("Objetivo", userProfile.goal)
                ProfileInfoChip("Nivel", userProfile.activityLevel)
                ProfileInfoChip("Peso", "${userProfile.weight} kg")
            }
        }
    }
}

@Composable
fun ProfileInfoChip(label: String, value: String) {
    Card(shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
    }
}


@Composable
fun TrainingSummaryCard(aiResponse: AIResponse) {
    val todayRoutine = remember(aiResponse.routine) { getTodayPlan(aiResponse.routine) }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Entrenamiento", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            if (todayRoutine != null) {
                Text("Hoy: ${todayRoutine.title}")
                Text(todayRoutine.content.lines().firstOrNull() ?: "")
                Button(onClick = { /*TODO*/ }) {
                    Text("Listo para entrenar")
                }
            } else {
                Text("Descanso.")
            }
        }
    }
}


@Composable
fun NutritionSummaryCard(aiResponse: AIResponse) {
    val todayDiet = remember(aiResponse.diet) { getTodayPlan(aiResponse.diet) }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Nutrición", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            if (todayDiet != null) {
                val currentMeal = remember(todayDiet.content) { getCurrentMeal(todayDiet.content) }
                Text(currentMeal)
            } else {
                Text("Sin plan de dieta para hoy.")
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
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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

fun getCurrentMeal(dietPlanContent: String): String {
    val calendar = Calendar.getInstance()
    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

    val mealTimeName = when (currentHour) {
        in 5..10 -> "Desayuno"
        in 11..14 -> "Comida"
        in 15..18 -> "Merienda"
        in 19..22 -> "Cena"
        else -> null
    }

    if (mealTimeName == null) {
        return "Revisa tu plan de dieta para ver tus comidas."
    }

    val mealKeywords = mapOf(
        "Desayuno" to listOf("DESAYUNO"),
        "Comida" to listOf("COMIDA", "ALMUERZO"),
        "Merienda" to listOf("MERIENDA"),
        "Cena" to listOf("CENA")
    )

    val targetKeywords = mealKeywords[mealTimeName] ?: emptyList()
    val allLines = dietPlanContent.lines()

    val mealContent = StringBuilder()
    var capturing = false

    for (line in allLines) {
        val trimmedUpper = line.trim().uppercase()
        val isNewMealHeader = mealKeywords.values.flatten().any { keyword -> trimmedUpper.startsWith(keyword) }

        if (isNewMealHeader) {
            if (capturing) break
            if (targetKeywords.any { keyword -> trimmedUpper.startsWith(keyword) }) {
                capturing = true
                mealContent.append(line).append("\n")
            }
        } else if (capturing) {
            mealContent.append(line).append("\n")
        }
    }

    if (mealContent.isNotBlank()) {
        return "Para tu $mealTimeName:\n${mealContent.toString().trim()}"
    }

    return "Es hora de tu $mealTimeName. Revisa tu plan de dieta completo para ver los detalles."
}

@Composable
fun DailyPlanScreen(routineDays: List<DayPlan>, dietDays: List<DayPlan>) {
    val calendar = Calendar.getInstance()
    val todayIndex = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {

        Text(
            "Rutina Semanal",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = PureWhite,
                fontWeight = FontWeight.Black
            ),
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        DayPager(plans = routineDays, initialIndex = todayIndex)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Dieta Semanal",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = PureWhite,
                fontWeight = FontWeight.Black
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        DayPager(plans = dietDays, initialIndex = todayIndex)

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DayPager(plans: List<DayPlan>, initialIndex: Int) {
    val pagerState = rememberPagerState(initialPage = initialIndex) { plans.size }

    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(horizontal = 32.dp)
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
            .fillMaxWidth()
            .height(450.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                if (isToday) PureWhite.copy(alpha = 0.90f)
                else PureWhite.copy(alpha = 0.75f)
        ),
        elevation = CardDefaults.cardElevation(10.dp)
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
fun NoPlanState(onCreatePlan: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
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
        modifier = Modifier.fillMaxSize(),
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
fun ErrorState(msg: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
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
