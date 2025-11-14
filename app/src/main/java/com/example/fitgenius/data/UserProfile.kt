package com.example.fitgenius.data

data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val gender: String,
    val menstrualPhase: String?,
    val age: Int,
    val weight: Double,
    val height: Double,
    val goal: String,
    val activityLevel: String
)