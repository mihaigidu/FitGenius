package com.example.fitgenius

data class UserProfile(
    val name: String,
    val email: String,
    val gender: String,
    val menstrualPhase: String? = null,
    val age: Int? = null,
    val weight: Double? = null,
    val height: Double? = null,
    val goal: String? = null, // "Pérdida de peso", "Ganar músculo", "Mantenimiento"
    val activityLevel: String? = null // "Sedentario", "Moderado", "Activo", "Muy activo"
)

data class AIResponse(
    val routine: String,
    val diet: String
)