package com.example.fitgenius

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
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Lee la API key de forma segura
    private val apiKey = "5f59c373-d1bb-4d47-b323-004c6f133cc0" // ⚠️ REEMPLAZA ESTO

    // URL de la API de Scaleway Generative APIs
    // Región: fr-par (Paris) - Puedes usar también nl-ams (Amsterdam)
    private val apiUrl = "https://api.scaleway.ai/v1/chat/completions"

    // Modelos disponibles en Scaleway (todos gratis):
    // - llama-3.1-8b-instruct (rápido, bueno)
    // - llama-3.1-70b-instruct (más potente, un poco más lento)
    // - mistral-nemo-instruct-2407 (equilibrado)
    private val model = "llama-3.1-70b-instruct"

    suspend fun generateRoutineAndDiet(userProfile: UserProfile): AIResponse = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isEmpty() || apiKey == "TU_SECRET_KEY_DE_SCALEWAY_AQUI") {
                return@withContext AIResponse(
                    routine = "❌ Error: API key no configurada.\n\n" +
                            "Pasos para configurar:\n" +
                            "1. Ve a https://console.scaleway.com/\n" +
                            "2. Crea una cuenta gratis\n" +
                            "3. Ve a IAM → API Keys\n" +
                            "4. Crea una Secret Key\n" +
                            "5. Agrégala en local.properties como:\n" +
                            "   SCALEWAY_API_KEY=scw_xxxxx",
                    diet = "Configura tu API key primero"
                )
            }

            val prompt = buildPrompt(userProfile)
            val response = callScalewayAPI(prompt)
            parseResponse(response)

        } catch (e: IOException) {
            AIResponse(
                routine = "❌ Error de conexión: ${e.message}\n\n" +
                        "Verifica:\n" +
                        "• Tu conexión a internet\n" +
                        "• Que la API key sea válida (debe empezar con 'scw_')\n" +
                        "• Que tu cuenta de Scaleway esté activa",
                diet = "Error de conexión"
            )
        } catch (e: Exception) {
            AIResponse(
                routine = "❌ Error inesperado: ${e.message}",
                diet = "Error al procesar"
            )
        }
    }

    private fun buildPrompt(profile: UserProfile): String {
        val menstrualInfo = if (profile.gender == "Mujer" && profile.menstrualPhase != null) {
            "\n- Fase menstrual actual: ${profile.menstrualPhase}"
        } else ""

        val ageInfo = if (profile.age != null) "\n- Edad: ${profile.age} años" else ""
        val weightInfo = if (profile.weight != null) "\n- Peso: ${profile.weight} kg" else ""
        val heightInfo = if (profile.height != null) "\n- Altura: ${profile.height} cm" else ""
        val goalInfo = if (profile.goal != null) "\n- Objetivo: ${profile.goal}" else ""
        val activityInfo = if (profile.activityLevel != null) "\n- Nivel de actividad: ${profile.activityLevel}" else ""

        return """
Eres un experto entrenador personal y nutricionista certificado. Analiza cuidadosamente el perfil del usuario y genera un plan 100% personalizado y único.

PERFIL DEL USUARIO:
- Nombre: ${profile.name}
- Género: ${profile.gender}$menstrualInfo$ageInfo$weightInfo$heightInfo$goalInfo$activityInfo

IMPORTANTE: Genera un plan COMPLETAMENTE PERSONALIZADO basado en estos datos específicos. NO uses plantillas genéricas.

SECCIÓN 1: RUTINA DE EJERCICIOS PERSONALIZADA
Crea un plan semanal detallado que incluya:
- Días específicos de entrenamiento (Ejemplo: Lunes, Miércoles, Viernes)
- Ejercicios concretos con nombres específicos
- Series y repeticiones exactas para cada ejercicio
- Tiempo de descanso entre series
- Duración total de cada sesión
- Calentamiento específico (5-10 min)
- Enfriamiento y estiramientos (5-10 min)
${if (profile.gender == "Mujer" && profile.menstrualPhase != null) {
            "- ADAPTA la intensidad considerando la fase ${profile.menstrualPhase} (en esta fase se recomienda: ${getMenstrualPhaseAdvice(profile.menstrualPhase)})"
        } else ""}
- Consejos de progresión semana a semana
- Variaciones según el nivel actual

SECCIÓN 2: PLAN DE DIETA PERSONALIZADO
Calcula y proporciona:
- Calorías diarias exactas basadas en: peso=${profile.weight}kg, altura=${profile.height}cm, objetivo=${profile.goal}, actividad=${profile.activityLevel}
- Distribución precisa de macronutrientes (gramos de proteína, carbohidratos y grasas)
- Menú completo para UN DÍA con 5 comidas:
  * Desayuno (hora sugerida + alimentos específicos + cantidades)
  * Media mañana (hora + alimentos + cantidades)
  * Almuerzo (hora + alimentos + cantidades)
  * Merienda (hora + alimentos + cantidades)
  * Cena (hora + alimentos + cantidades)
- Plan de hidratación específico (litros por día)
- Suplementos recomendados si aplican para el objetivo
- Alimentos a evitar o limitar
- Timing de nutrientes (cuándo comer qué)

FORMATO DE RESPUESTA:
Primero escribe toda la RUTINA DE EJERCICIOS completa.
Después escribe EXACTAMENTE esto en una línea: ===DIETA===
Luego escribe todo el PLAN DE DIETA completo.

Sé muy específico con cantidades, nombres de ejercicios, horarios y porciones. Usa emojis para mejor visualización. El tono debe ser motivador y profesional.
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
        // Construcción del JSON según la API de Scaleway (formato OpenAI-compatible)
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
            put("temperature", 0.8) // Mayor creatividad para respuestas únicas
            put("max_tokens", 3000) // Suficiente para respuestas detalladas
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
                    "Error ${response.code}: $errorMsg\n\n" +
                            "Verifica:\n" +
                            "1. Que tu Secret Key de Scaleway sea correcta (empieza con 'scw_')\n" +
                            "2. Que tu cuenta esté activa\n" +
                            "3. Que tengas conexión a internet\n" +
                            "4. Región configurada correctamente"
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

            // Separar rutina y dieta usando el delimitador
            val sections = fullText.split("===DIETA===", ignoreCase = true)

            val routine = if (sections.isNotEmpty()) {
                sections[0].trim().ifEmpty {
                    "❌ No se pudo generar la rutina correctamente"
                }
            } else {
                "❌ Error al procesar la rutina"
            }

            val diet = if (sections.size > 1) {
                sections[1].trim().ifEmpty {
                    "❌ No se pudo generar la dieta correctamente"
                }
            } else {
                // Si no encuentra el separador, intentar buscar la palabra "dieta"
                val dietIndex = fullText.indexOf("PLAN DE DIETA", ignoreCase = true)
                if (dietIndex != -1) {
                    fullText.substring(dietIndex).trim()
                } else {
                    "⚠️ No se pudo separar la dieta del plan completo.\n\nPlan completo generado:\n\n$fullText"
                }
            }

            return AIResponse(routine = routine, diet = diet)

        } catch (e: Exception) {
            throw Exception("Error al procesar la respuesta de Scaleway: ${e.message}\n\nRespuesta recibida: $jsonResponse")
        }
    }
}