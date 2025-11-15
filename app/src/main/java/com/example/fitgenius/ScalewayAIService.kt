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
        val menstrualPhase = profile.menstrualPhase
        val menstrualInfo = if (profile.gender == "Mujer" && menstrualPhase != null) {
            "\n- Fase menstrual actual: $menstrualPhase"
        } else ""

        val favoriteExercisesInfo = if (profile.favoriteExercises.isNotEmpty()) {
            "\n- Ejercicios favoritos: ${profile.favoriteExercises.joinToString()}"
        } else ""

        val allergiesInfo = if (profile.allergies.isNotBlank()) {
            "\n- Alergias a tener en cuenta: ${profile.allergies}"
        } else ""

        val foodPreferencesInfo = if (profile.foodPreferences.isNotBlank()) {
            "\n- Preferencias alimentarias: ${profile.foodPreferences}"
        } else ""

        // NUEVO: Prompt mejorado con instrucciones más estrictas
        return """
Eres un experto entrenador personal y nutricionista de élite. Tu misión es crear un plan semanal 100% personalizado, detallado y con un formato muy específico.

**PERFIL DEL USUARIO:**
- Nombre: ${profile.name}
- Género: ${profile.gender}$menstrualInfo
- Edad: ${profile.age} años
- Peso: ${profile.weight} kg
- Altura: ${profile.height} cm
- Objetivo Principal: ${profile.goal}
- Nivel de Actividad Física: ${profile.activityLevel}

**PREFERENCIAS DE ENTRENAMIENTO:**
- Días de entrenamiento por semana: ${profile.trainingDays}
- Lugar de entrenamiento: ${profile.trainingLocation}$favoriteExercisesInfo

**PREFERENCIAS DE DIETA:**$allergiesInfo$foodPreferencesInfo

**INSTRUCCIONES DE FORMATO (MUY IMPORTANTE):**
La respuesta DEBE seguir esta estructura de formato para que la aplicación la pueda interpretar. NO uses negrita ni asteriscos en los títulos.

**SECCIÓN 1: RUTINA DE ENTRENAMIENTO SEMANAL**
Crea un plan para los **${profile.trainingDays} días** especificados. Para CADA día de entrenamiento, usa el siguiente formato:
- **TÍTULO OBLIGATORIO:** El título DEBE empezar con `Día X:` seguido de los grupos musculares principales. Ejemplo: `Día 1: Pecho y Tríceps`.
- Detalla los ejercicios para el lugar de entrenamiento (${profile.trainingLocation}) con series, repeticiones y descansos.
- Incluye una sección de calentamiento y enfriamiento para cada sesión.
${if (profile.gender == "Mujer" && menstrualPhase != null) {
            "- **ADAPTACIÓN MENSTRUAL:** Adapta la intensidad a la fase ($menstrualPhase) recomendando: ${getMenstrualPhaseAdvice(menstrualPhase)}."
        } else ""}

**SECCIÓN 2: PLAN DE DIETA SEMANAL**
Genera un plan de comidas detallado para **6 días (Lunes a Sábado)**. El **Domingo** es un **DÍA LIBRE** (cheat day) y no debes generar un plan para él, solo mencionarlo al final de la sección de dieta.
Para CADA UNO de los 6 días, usa el siguiente formato:
- **TÍTULO OBLIGATORIO:** El título DEBE ser el día de la semana, seguido de dos puntos y un recuento de las calorías totales aproximadas. Ejemplo: `Lunes: ~2250 kcal`.
- Detalla 5 comidas (Desayuno, Media Mañana, Almuerzo, Merienda, Cena) con alimentos y cantidades.
- El plan DEBE excluir estrictamente los alérgenos: ${profile.allergies}.
- Incluye un plan de hidratación diario.

**ESTRUCTURA FINAL DE LA RESPUESTA:**
1. Primero, la **RUTINA DE ENTRENAMIENTO SEMANAL** completa.
2. Luego, en una nueva línea, escribe la palabra clave `===DIETA===`.
3. Finalmente, el **PLAN DE DIETA SEMANAL COMPLETO**.

Usa un tono motivador y profesional y añade emojis para hacer la lectura más amena.
        """.trimIndent()
    }

    private fun getMenstrualPhaseAdvice(phase: String): String {
        return when (phase) {
            "Menstruación" -> "ejercicios de baja-moderada intensidad, yoga, caminatas, evitar ejercicios de alto impacto"
            "Folicular" -> "aumentar intensidad gradualmente, buen momento para entrenamientos de fuerza"
            "Ovulación" -> "máxima energía, ideal para entrenamientos intensos y levantamiento de peso"
            "Lútea" -> "mantener intensidad moderada, enfocarse en resistencia, incluir más descanso"
            else -> "ajustar según cómo te sientas"
        }
    }

    private fun callScalewayAPI(prompt: String): String {
        val jsonBody = JSONObject().apply {
            put("model", model)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", "Eres un experto entrenador personal y nutricionista certificado con 15 años de experiencia. Generas planes personalizados únicos basados en el perfil exacto de cada usuario, considerando todos sus datos específicos.")
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
            put("temperature", 0.8)
            put("max_tokens", 4096)
            put("top_p", 0.9)
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
                    val errorJson = JSONObject(responseBody)
                    errorJson.optJSONObject("error")?.optString("message") ?: responseBody
                } catch (e: Exception) {
                    responseBody
                }

                throw IOException(
                    "Error ${response.code}: $errorMsg"
                )
            }

            return responseBody
        }
    }

    private fun parseResponse(jsonResponse: String): AIResponse {
        try {
            val json = JSONObject(jsonResponse)
            val choices = json.getJSONArray("choices")
            if (choices.length() == 0) {
                throw Exception("No se generó ninguna respuesta de la IA")
            }
            val message = choices.getJSONObject(0).getJSONObject("message")
            val fullText = message.getString("content").trim()

            // Se mantiene la lógica de separación
            val sections = fullText.split("===DIETA===", ignoreCase = true, limit = 2)
            val routine = sections.getOrNull(0)?.trim() ?: ""
            val diet = sections.getOrNull(1)?.trim() ?: ""

            return AIResponse(routine = routine, diet = diet)
        } catch (e: Exception) {
            throw Exception("Error al procesar la respuesta de Scaleway: ${e.message}")
        }
    }
}
