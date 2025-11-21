package com.example.fitgenius.data

data class WeeklyWorkout(
    val week: List<DailyWorkout>
)

data class DailyWorkout(
    val day: String,
    val name: String,
    val duration: String,
    val description: String,
    val exercises: List<Exercise>
)

data class Exercise(
    val name: String,
    val series: String,
    val reps: String,
    val rest: String,
    val observations: String = ""
)
