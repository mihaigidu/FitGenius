package com.example.fitgenius.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitgenius.data.AIResponse
import com.example.fitgenius.data.DailySummary
import com.example.fitgenius.data.Meal
import com.example.fitgenius.data.WeeklyNutrition
import com.example.fitgenius.ui.theme.MutedGreen
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DietScreen(aiResponse: AIResponse?) {
    if (aiResponse == null) {
        NoPlanState { }
        return
    }

    val weeklyNutrition = remember(aiResponse.diet) { parseNutrition(aiResponse.diet) }

    if (weeklyNutrition == null) {
        ErrorState("No se pudo analizar la dieta.")
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MutedGreen)
            .padding(16.dp)
    ) {
        val today = SimpleDateFormat("EEEE", Locale.forLanguageTag("es-ES")).format(Date()).uppercase()
        val todayIndex = weeklyNutrition.week.indexOfFirst { it.day.uppercase() == today }.coerceAtLeast(0)

        val pagerState = rememberPagerState(initialPage = todayIndex) { weeklyNutrition.week.size }
        val coroutineScope = rememberCoroutineScope()

        PrimaryScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = MutedGreen,
            edgePadding = 0.dp,
            indicator = {},
            divider = {}
        ) {
            weeklyNutrition.week.forEachIndexed { index, dailyPlan ->
                val isSelected = pagerState.currentPage == index
                val shape = RoundedCornerShape(20.dp)
                Tab(
                    selected = isSelected,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .background(
                            if (isSelected) Color.White else MutedGreen,
                            shape = shape
                        ),
                    text = { Text(dailyPlan.day.take(3), color = if(isSelected) Color.Black else Color.Gray) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalPager(state = pagerState) { page ->
            val dailyNutrition = weeklyNutrition.week[page]
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                DailySummaryCard(dailyNutrition.summary)
                Spacer(modifier = Modifier.height(16.dp))
                dailyNutrition.meals.forEach {
                    meal ->
                    MealCard(meal)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun DailySummaryCard(summary: DailySummary) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3E8FF)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Total del día", style = MaterialTheme.typography.titleLarge)
                Text("${summary.totalCalories} kcal", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text(summary.extra, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("P: ${summary.protein}g", style = MaterialTheme.typography.bodyLarge)
                Text("C: ${summary.carbs}g", style = MaterialTheme.typography.bodyLarge)
                Text("G: ${summary.fats}g", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun MealCard(meal: Meal) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Text(meal.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(meal.time, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                }
                Text("${meal.calories} kcal", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            meal.foods.forEach {
                food ->
                Text("• $food", style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                MacroInfo("Proteínas", meal.macros.protein)
                MacroInfo("Carbohidratos", meal.macros.carbs)
                MacroInfo("Grasas", meal.macros.fats)
            }
        }
    }
}

@Composable
fun MacroInfo(name: String, amount: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(name, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(amount, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

fun parseNutrition(jsonString: String): WeeklyNutrition? {
    return try {
        val root = JSONObject(jsonString)
        val weekArray = root.getJSONArray("semana")
        val dailyNutritions = mutableListOf<com.example.fitgenius.data.DailyNutrition>()

        for (i in 0 until weekArray.length()) {
            val dayObject = weekArray.getJSONObject(i)
            val summaryObject = dayObject.getJSONObject("resumen_dia")
            val mealsArray = dayObject.getJSONArray("comidas")
            val meals = mutableListOf<com.example.fitgenius.data.Meal>()

            for (j in 0 until mealsArray.length()) {
                val mealObject = mealsArray.getJSONObject(j)
                val foodsArray = mealObject.getJSONArray("alimentos")
                val foods = mutableListOf<String>()
                for(k in 0 until foodsArray.length()) foods.add(foodsArray.getString(k))
                val macrosObject = mealObject.getJSONObject("macros")

                meals.add(
                    com.example.fitgenius.data.Meal(
                        name = mealObject.getString("nombre"),
                        time = mealObject.getString("hora"),
                        calories = mealObject.getString("calorias"),
                        foods = foods,
                        macros = com.example.fitgenius.data.Macros(
                            protein = macrosObject.getString("proteinas"),
                            carbs = macrosObject.getString("carbohidratos"),
                            fats = macrosObject.getString("grasas")
                        )
                    )
                )
            }

            dailyNutritions.add(
                com.example.fitgenius.data.DailyNutrition(
                    day = dayObject.getString("dia"),
                    summary = com.example.fitgenius.data.DailySummary(
                        totalCalories = summaryObject.getString("calorias_totales"),
                        protein = summaryObject.getString("proteinas"),
                        carbs = summaryObject.getString("carbohidratos"),
                        fats = summaryObject.getString("grasas"),
                        extra = summaryObject.getString("extra")
                    ),
                    meals = meals
                )
            )
        }
        WeeklyNutrition(week = dailyNutritions)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
