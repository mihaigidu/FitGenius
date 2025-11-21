package com.example.fitgenius

import com.example.fitgenius.data.AIResponse
import com.example.fitgenius.data.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

class ScalewayAIService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    private val apiKey = "5f59c373-d1bb-4d47-b323-004c6f133cc0" // ⚠️ REEMPLAZA ESTO
    private val apiUrl = "https://api.scaleway.ai/v1/chat/completions"
    private val model = "llama-3.1-70b-instruct"

    suspend fun generateRoutineAndDiet(userProfile: UserProfile): AIResponse = withContext(Dispatchers.IO) {
        val prompt = buildPrompt(userProfile)
        val response = callScalewayAPI(prompt)
        return@withContext parseResponse(response)
    }

    private fun buildPrompt(profile: UserProfile): String {
        val menstrualPhase = profile.lastPeriodDate?.let {
            lastPeriod ->
            val today = Calendar.getInstance()
            val periodStart = Calendar.getInstance().apply { time = Date(lastPeriod) }
            val daysSincePeriod = ((today.timeInMillis - periodStart.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
            if (daysSincePeriod < 0) return@let null
            val dayOfCycle = daysSincePeriod % profile.cycleLength

            when {
                dayOfCycle in 0..4 -> "Menstruación"
                dayOfCycle in 5..11 -> "Fase Folicular"
                dayOfCycle in 12..15 -> "Ovulación"
                else -> "Fase Lútea"
            }
        }

        val menstrualInfo = if (profile.gender == "Mujer" && menstrualPhase != null) {
            "\n- Fase menstrual actual: $menstrualPhase (Adaptar intensidad: ${getMenstrualPhaseAdvice(menstrualPhase)})"
        } else ""

        val favoriteExercisesInfo = if (profile.favoriteExercises.isNotEmpty()) {
            "\n- Ejercicios favoritos: ${profile.favoriteExercises.joinToString()}"
        } else ""

        val allergiesInfo = if (profile.allergies.isNotBlank()) {
            "\n- Alergias (excluir de la dieta): ${profile.allergies}"
        } else ""

        val foodPreferencesInfo = if (profile.foodPreferences.isNotBlank()) {
            "\n- Preferencias alimentarias: ${profile.foodPreferences}"
        } else ""

        val workoutJsonStructure = """
        "rutina": {
          "semana": [
            {
              "dia": "Lunes",
              "nombre": "Hipertrofia - Tren Superior",
              "duracion": "45",
              "descripcion": "Rutina de hipertrofia para tren superior, enfocada en pecho y tríceps.",
              "ejercicios": [
                { "nombre": "Press de Banca", "series": "4", "repeticiones": "8-12", "descanso": "90s" },
                { "nombre": "Flexiones", "series": "3", "repeticiones": "Al fallo", "descanso": "60s" }
              ]
            }
          ]
        }
        """

        val nutritionJsonStructure = """
        "dieta": {
          "semana": [
            {
              "dia": "Lunes",
              "resumen_dia": {
                "calorias_totales": "2180",
                "proteinas": "105",
                "carbohidratos": "160",
                "grasas": "55",
                "extra": "Mantén la hidratación durante todo el día, bebe al menos 2.5 litros de agua."
              },
              "comidas": [
                {
                  "nombre": "Desayuno",
                  "hora": "08:00",
                  "calorias": "545",
                  "alimentos": ["3 huevos revueltos", "2 rebanadas de pan integral", "1 plátano", "Café o té"],
                  "macros": { "proteinas": "25g", "carbohidratos": "45g", "grasas": "12g" }
                }
              ]
            }
          ]
        }
        """

        return """
            Eres un experto entrenador personal y nutricionista de alto nivel. Tu misión es diseñar un plan de transformación integral para el usuario, optimizado para sus objetivos específicos.

            **PERFIL DEL USUARIO:**
            - Género: ${profile.gender}
            - Edad: ${profile.age} años
            - Peso: ${profile.weight} kg
            - Altura: ${profile.height} cm
            - Objetivo: ${profile.goal}
            - Nivel de Actividad: ${profile.activityLevel}
            - Días de entrenamiento: ${profile.trainingDaysPerWeek}
            - Lugar de entrenamiento: ${profile.trainingLocation}$menstrualInfo$favoriteExercisesInfo$allergiesInfo$foodPreferencesInfo

            **TU FILOSOFÍA:**
            - **Motivación**: Incluye mensajes cortos pero poderosos en las descripciones para mantener al usuario motivado.
            - **Ciencia**: Basa tus recomendaciones en principios científicos de hipertrofia, pérdida de grasa o rendimiento según corresponda.
            - **Variedad**: EVITA LA MONOTONÍA. La dieta debe ser variada y deliciosa. El entrenamiento debe ser desafiante pero realizable.

            **INSTRUCCIONES DE FORMATO OBLIGATORIO (CRÍTICO):**
            Tu respuesta DEBE ser ÚNICAMENTE un objeto JSON válido. NO incluyas texto introductorio, ni conclusiones, ni bloques de código markdown (```json). SOLO EL JSON PURO.

            El JSON debe tener esta estructura exacta:
            {
              "rutina": { ... },
              "dieta": { ... }
            }

            **1. Detalles de la Rutina ("rutina"):**
            - Clave "semana": Lista de 7 objetos (Lunes a Domingo).
            - Días de entrenamiento: Detalla ejercicios, series, repeticiones y descansos.
            - Días de descanso: "ejercicios": [], "descripcion": "Día de recuperación activa/descanso total".
            - Ejemplo estructura:
            $workoutJsonStructure

            **2. Detalles de la Dieta ("dieta"):**
            - Clave "semana": Lista de 7 objetos (Lunes a Domingo).
            - **IMPORTANTE**: Genera menús diferentes para cada día.
            - Ejemplo estructura:
            $nutritionJsonStructure

            Genera el plan ahora.
        """.trimIndent()
    }

    private fun getMenstrualPhaseAdvice(phase: String): String {
        return when (phase) {
            "Menstruación" -> "reducir la intensidad, enfocarse en movilidad y recuperación"
            "Fase Folicular" -> "aumentar la intensidad, ideal para entrenamientos de fuerza"
            "Ovulación" -> "pico de energía, perfecto para entrenamientos de alta intensidad y RPs"
            "Fase Lútea" -> "moderar la intensidad, enfocarse en ejercicios de tempo y resistencia"
            else -> "ajustar según sensaciones"
        }
    }

    private fun callScalewayAPI(prompt: String): String {
        val jsonBody = JSONObject().apply {
            put("model", model)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", "Eres un experto entrenador personal y nutricionista de alto nivel. Generas planes de transformación 100% personalizados en formato JSON estricto.")
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
            put("temperature", 0.7)
            put("max_tokens", 4096)
            put("top_p", 1.0)
            put("stream", false)
        }

        val requestBody = jsonBody.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(apiUrl)
            .post(requestBody)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string() ?: throw IOException("Respuesta vacía del servidor")

            if (!response.isSuccessful) {
                val errorMsg = try {
                    JSONObject(responseBody).optJSONObject("error")?.optString("message") ?: responseBody
                } catch (e: Exception) {
                    responseBody
                }
                throw IOException("Error ${response.code}: $errorMsg")
            }
            return responseBody
        }
    }

    private fun parseResponse(jsonResponse: String): AIResponse {
        try {
            val outerJson = JSONObject(jsonResponse)
            val contentJsonString = outerJson.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()

            // Intenta limpiar la respuesta si contiene caracteres extraños antes o después del JSON
            val cleanJsonString = contentJsonString.substring(contentJsonString.indexOf('{'), contentJsonString.lastIndexOf('}') + 1)

            val contentJson = JSONObject(cleanJsonString)

            val routineString = contentJson.getJSONObject("rutina").toString()
            val dietString = contentJson.getJSONObject("dieta").toString()

            return AIResponse(routine = routineString, diet = dietString)
        } catch (e: Exception) {
            System.err.println("Error al procesar JSON de la IA: ${e.message}")
            System.err.println("Respuesta recibida: $jsonResponse")
            throw Exception("La respuesta de la IA no tiene el formato JSON esperado. Por favor, reintenta. Error: ${e.message}")
        }
    }
}
