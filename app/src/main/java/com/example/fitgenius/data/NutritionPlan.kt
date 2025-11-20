package com.example.fitgenius.data

data class WeeklyNutrition(
    val week: List<DailyNutrition>
)

data class DailyNutrition(
    val day: String,
    val summary: DailySummary,
    val meals: List<Meal>
)

data class DailySummary(
    val totalCalories: String,
    val protein: String,
    val carbs: String,
    val fats: String,
    val extra: String
)

data class Meal(
    val name: String,
    val time: String,
    val calories: String,
    val foods: List<String>,
    val macros: Macros
)

data class Macros(
    val protein: String,
    val carbs: String,
    val fats: String
)
