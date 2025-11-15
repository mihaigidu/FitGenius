package com.example.fitgenius.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fitgenius.data.AIResponse
import com.example.fitgenius.data.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    userProfile: UserProfile?,
    aiResponse: AIResponse?,
    isLoading: Boolean,
    errorMessage: String?
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Tu Plan FitGenius", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                actions = {
                    if (aiResponse != null) {
                        IconButton(onClick = { navController.navigate("profile") }) {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = "Perfil",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                isLoading -> LoadingState()
                errorMessage != null -> ErrorState(errorMessage) { /* TODO: Add retry logic */ }
                aiResponse != null -> PlanContent(aiResponse)
                else -> Unit // Should not happen
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
fun ErrorState(errorMessage: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Ups! Algo salió mal", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(8.dp))
        Text(errorMessage, style = MaterialTheme.typography.bodyMedium)
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

        when (selectedTab) {
            0 -> DayByDayPlan(planText = aiResponse.routine, isDiet = false)
            1 -> DayByDayPlan(planText = aiResponse.diet, isDiet = true)
        }
    }
}

@Composable
fun DayByDayPlan(planText: String, isDiet: Boolean) {
    val days: List<Pair<String, String>> = remember(planText, isDiet) {
        val dayPlans = mutableListOf<Pair<String, String>>()
        val titleRegex = if (isDiet) {
            "^(?i)(Lunes|Martes|Miércoles|Jueves|Viernes|Sábado|Domingo)".toRegex()
        } else {
            "^(?i)Día \\d+".toRegex()
        }

        val lines = planText.lines()
        var currentContent = mutableListOf<String>()
        var currentTitle: String? = null

        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty() && currentTitle == null) continue // Skip empty lines at the beginning

            // A line is a title if it matches the regex and is reasonably short
            if (titleRegex.containsMatchIn(trimmedLine) && trimmedLine.length < 80) {
                // If we have content for a previous day, save it
                if (currentTitle != null) {
                    dayPlans.add(currentTitle to currentContent.joinToString("\n").trim())
                }
                // Start a new day
                currentTitle = trimmedLine
                currentContent = mutableListOf()
            } else if (currentTitle != null) {
                // This line is content for the current day
                currentContent.add(line)
            }
        }

        // Add the last captured day to the list
        if (currentTitle != null) {
            dayPlans.add(currentTitle to currentContent.joinToString("\n").trim())
        }

        dayPlans
    }

    if (days.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            ) {
            Text(
                "No se ha podido interpretar el plan.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Intenta generar uno nuevo desde el menú de Ajustes.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(days) { (title, content) ->
                DayItem(dayTitle = title, dayContent = content)
            }
        }
    }
}

@Composable
fun DayItem(dayTitle: String, dayContent: String) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .clickable { isExpanded = !isExpanded }
                .padding(16.dp)
                .animateContentSize()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = dayTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Contraer" else "Expandir"
                )
            }
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(modifier = Modifier.padding(bottom = 16.dp))
                Text(
                    text = dayContent,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 26.sp
                )
            }
        }
    }
}