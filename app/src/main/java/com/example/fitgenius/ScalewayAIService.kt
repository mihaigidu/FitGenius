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
        .connectTimeout(120, TimeUnit.SECONDS) // Increased timeout
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

        return """
Eres un experto entrenador personal y nutricionista de élite. Analiza el siguiente perfil de usuario para crear un plan semanal completo, detallado y 100% personalizado.

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

**INSTRUCCIONES DETALLADAS:**

**SECCIÓN 1: RUTINA DE ENTRENAMIENTO SEMANAL**
Crea todo lo siguiente que te voy a pedir de la forma mas rapido que puedas como si fueras un entrenador profesional:
Crea un plan de entrenamiento para los **${profile.trainingDays} días** especificados. Para cada día de entrenamiento:
- Asigna un nombre al día (Ej: Día 1: Pecho y Tríceps, Día 2: Pierna, etc.).
- Detalla los ejercicios específicos para el lugar de entrenamiento (${profile.trainingLocation}).
- Incluye series, repeticiones y tiempos de descanso para CADA ejercicio.
- Adapta la rutina al objetivo (${profile.goal}) y al nivel de actividad (${profile.activityLevel}).
- Si el usuario especificó ejercicios favoritos, intégralos de forma lógica en la rutina.
- Proporciona una sección de calentamiento (5-10 min) y enfriamiento (5-10 min) para cada sesión.
${if (profile.gender == "Mujer" && menstrualPhase != null) {
            "- **IMPORTANTE:** Adapta la intensidad y tipo de ejercicio a la fase menstrual actual ($menstrualPhase). Recomienda: ${getMenstrualPhaseAdvice(menstrualPhase)}."
        } else ""}

**SECCIÓN 2: PLAN DE DIETA SEMANAL COMPLETO**
Crea todo lo siguiente que te voy a pedir de la forma mas rapido que puedas como si fueras un dieditsta profesional:
Calcula las necesidades calóricas y de macronutrientes diarias exactas. Luego, genera un menú detallado para **TODA LA SEMANA (Lunes a Domingo)**. Para CADA DÍA de la semana:
- Muestra el total de calorías y la distribución de macros (proteínas, carbohidratos, grasas) para ese día.
- Detalla 5 comidas (Desayuno, Media Mañana, Almuerzo, Merienda, Cena) con:
  - Alimentos específicos y cantidades precisas (en gramos).
  - Horarios sugeridos.
- **IMPORTANTE:** El plan debe excluir estrictamente cualquier alérgeno mencionado (${profile.allergies}).
- Adapta las comidas a las preferencias del usuario (${profile.foodPreferences}).
- Incluye un plan de hidratación diario (en litros).

**FORMATO DE RESPUESTA OBLIGATORIO:**
Para cada día de la rutina y de la dieta, **SIEMPRE** empieza la sección con el título del día en negrita y entre dos asteriscos. Por ejemplo: `**Día 1: Pecho y Tríceps**` o `**Lunes**`. Este formato es crucial para que la app pueda mostrar la información correctamente.
Primero, escribe la **RUTINA DE ENTRENAMIENTO SEMANAL** completa.
Luego, en una nueva línea, escribe exactamente: `===DIETA===`
Finalmente, escribe el **PLAN DE DIETA SEMANAL COMPLETO**.

Usa un tono motivador y profesional. Utiliza emojis para hacer la lectura más amena.
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
            put("max_tokens", 4096) // Increased for weekly plan
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

            val sections = fullText.split("===DIETA===", ignoreCase = true, limit = 2)
            val routine = sections.getOrNull(0)?.trim() ?: ""
            val diet = sections.getOrNull(1)?.trim() ?: ""

            return AIResponse(routine = routine, diet = diet)
        } catch (e: Exception) {
            throw Exception("Error al procesar la respuesta de Scaleway: ${e.message}")
        }
    }
}
