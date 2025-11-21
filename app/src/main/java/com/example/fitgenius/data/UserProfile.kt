package com.example.fitgenius.data

import android.net.Uri

data class UserProfile(
    // Parte 1: Registro Básico
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var profileImageUri: String? = null, // NUEVO: Para la foto de perfil

    // Parte 2: Detalles del Perfil
    var gender: String = "",
    var lastPeriodDate: Long? = null, // NUEVO: Para el calendario menstrual
    var cycleLength: Int = 28, // NUEVO: Duración del ciclo menstrual
    var age: Int = 0,
    var weight: Int = 0,
    var height: Int = 0,
    var goal: String = "",
    var activityLevel: String = "Moderado",

    // Nuevos campos
    var trainingDaysPerWeek: Int = 3, // RENOMBRADO
    var trainingLocation: String = "",
    var favoriteExercises: List<String> = emptyList(),
    var allergies: String = "",
    var foodPreferences: String = ""
)
