package com.example.fitgenius.data


data class UserProfile(
    // Parte 1: Registro Básico
    var id: String = "",
    var name: String = "",
    var email: String = "",

    // Parte 2: Detalles del Perfil
    var gender: String = "Hombre",
    var menstrualPhase: String? = null,
    var age: Int = 0,
    var weight: Double = 0.0,
    var height: Double = 0.0,
    var goal: String = "Mantenimiento",
    var activityLevel: String = "Moderado",

    // Nuevos campos
    var trainingDays: Int = 3,
    var trainingLocation: String = "Gimnasio",
    var favoriteExercises: List<String> = emptyList(),
    var allergies: String = "",
    var foodPreferences: String = ""
)
