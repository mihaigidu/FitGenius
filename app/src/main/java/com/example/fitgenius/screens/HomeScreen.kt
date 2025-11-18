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
import com.example.fitgenius.data.AIResponse
import com.example.fitgenius.data.UserProfile
import com.example.fitgenius.ui.theme.BrightBlue
import com.example.fitgenius.ui.theme.PetrolGreen
import com.example.fitgenius.ui.theme.PureWhite
import java.util.Calendar

data class DayPlan(val title: String, val content: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    userProfile: UserProfile?,
    aiResponse: AIResponse?,
    isLoading: Boolean,
    errorMessage: String?
) {

    val gradientBackground = Brush.verticalGradient(
        listOf(
            PetrolGreen.copy(alpha = 0.80f),
            BrightBlue.copy(alpha = 0.60f)
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Tu Plan de Hoy",
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = PureWhite
                ),
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Perfil",
                            tint = PureWhite
                        )
                    }
                }
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBackground)
                .padding(padding)
        ) {

            when {
                isLoading -> LoadingState()
                errorMessage != null -> ErrorState(errorMessage) { }
                aiResponse != null -> {
                    val routineDays = remember(aiResponse.routine) { parsePlan(aiResponse.routine) }
                    val dietDays = remember(aiResponse.diet) { parsePlan(aiResponse.diet) }
                    DailyPlanScreen(routineDays, dietDays)
                }
                else -> NoPlanState { }
            }
        }
    }
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
            style = MaterialTheme.typography.headlineMedium.copy(color = PureWhite)
        )
        Text(
            "Aún no tienes un plan generado.",
            style = MaterialTheme.typography.bodyLarge.copy(color = PureWhite)
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onCreatePlan,
            colors = ButtonDefaults.buttonColors(containerColor = PureWhite)
        ) {
            Text("Crear Plan", color = PetrolGreen, fontWeight = FontWeight.Bold)
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
        CircularProgressIndicator(color = PureWhite)
        Spacer(Modifier.height(16.dp))
        Text(
            "Generando tu plan personalizado...",
            style = MaterialTheme.typography.titleMedium.copy(color = PureWhite)
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
        Text(msg, color = PureWhite)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Reintentar") }
    }
}
