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
import com.example.fitgenius.data.Exercise
import com.example.fitgenius.data.WeeklyWorkout
import com.example.fitgenius.ui.theme.MutedGreen
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RoutineScreen(aiResponse: AIResponse?) {
    if (aiResponse == null) {
        NoPlanState { }
        return
    }

    val weeklyWorkout = remember(aiResponse.routine) { parseWorkout(aiResponse.routine) }

    if (weeklyWorkout == null) {
        ErrorState("No se pudo analizar la rutina.")
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MutedGreen)
            .padding(16.dp)
    ) {
        val today = SimpleDateFormat("EEEE", Locale.forLanguageTag("es-ES")).format(Date()).uppercase()
        val todayIndex = weeklyWorkout.week.indexOfFirst { it.day.uppercase() == today }.coerceAtLeast(0)

        val pagerState = rememberPagerState(initialPage = todayIndex) { weeklyWorkout.week.size }
        val coroutineScope = rememberCoroutineScope()

        PrimaryScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = MutedGreen,
            edgePadding = 0.dp,
            indicator = {},
            divider = {}
        ) {
            weeklyWorkout.week.forEachIndexed { index, dailyWorkout ->
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
                    text = { Text(dailyWorkout.day.take(3), color = if(isSelected) Color.Black else Color.Gray) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalPager(state = pagerState) { page ->
            val dailyWorkout = weeklyWorkout.week[page]
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3E8FF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(dailyWorkout.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("${dailyWorkout.duration} min", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(dailyWorkout.description, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                dailyWorkout.exercises.forEach {
                    exercise ->
                    ExerciseCard(exercise)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun ExerciseCard(exercise: Exercise) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(exercise.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Series", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text(exercise.series, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Reps", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text(exercise.reps, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Descanso", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text(exercise.rest, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

fun parseWorkout(jsonString: String): WeeklyWorkout? {
    return try {
        val root = JSONObject(jsonString)
        val weekArray = root.getJSONArray("semana")
        val dailyWorkouts = mutableListOf<com.example.fitgenius.data.DailyWorkout>()

        for (i in 0 until weekArray.length()) {
            val dayObject = weekArray.getJSONObject(i)
            val exercisesArray = dayObject.getJSONArray("ejercicios")
            val exercises = mutableListOf<Exercise>()

            for (j in 0 until exercisesArray.length()) {
                val exerciseObject = exercisesArray.getJSONObject(j)
                exercises.add(
                    Exercise(
                        name = exerciseObject.getString("nombre"),
                        series = exerciseObject.getString("series"),
                        reps = exerciseObject.getString("repeticiones"),
                        rest = exerciseObject.getString("descanso")
                    )
                )
            }

            dailyWorkouts.add(
                com.example.fitgenius.data.DailyWorkout(
                    day = dayObject.getString("dia"),
                    name = dayObject.getString("nombre"),
                    duration = dayObject.getString("duracion"),
                    description = dayObject.getString("descripcion"),
                    exercises = exercises
                )
            )
        }
        WeeklyWorkout(week = dailyWorkouts)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
