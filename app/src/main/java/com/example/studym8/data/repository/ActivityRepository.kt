package com.example.studym8.data.repository

import com.example.studym8.data.model.ActivityQuestion
import com.example.studym8.data.model.QuestionDifficulty
import com.example.studym8.data.model.QuestionType
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ActivityRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val activitiesCollection = db.collection("activities")
    private val generateCollection = db.collection("generate")
    
    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("No hay una sesión de usuario activa")
    }
    
    suspend fun getActivitiesForSession(studyPlanId: String, sessionId: String): List<ActivityQuestion> {
        try {
            val querySnapshot = activitiesCollection
                .whereEqualTo("studyPlanId", studyPlanId)
                .whereEqualTo("sessionId", sessionId)
                .get()
                .await()
                
            return querySnapshot.documents.mapNotNull { document ->
                val data = document.data ?: return@mapNotNull null
                ActivityQuestion.fromMap(data, document.id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }
    
    suspend fun getActivityById(activityId: String): ActivityQuestion? {
        try {
            val document = activitiesCollection.document(activityId).get().await()
            if (document.exists()) {
                val data = document.data ?: return null
                return ActivityQuestion.fromMap(data, document.id)
            }
            return null
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    suspend fun createActivity(activity: ActivityQuestion): String? {
        try {
            val activityWithUserId = activity.copy(
                id = UUID.randomUUID().toString(),
                createdAt = Timestamp.now()
            )
            
            // Crear nuevo documento en Firestore
            val documentRef = activitiesCollection.document(activityWithUserId.id)
            documentRef.set(activityWithUserId.toMap()).await()
            
            return activityWithUserId.id
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    suspend fun updateActivity(activity: ActivityQuestion): Boolean {
        try {
            activitiesCollection.document(activity.id).set(activity.toMap()).await()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
    
    suspend fun deleteActivity(activityId: String): Boolean {
        try {
            activitiesCollection.document(activityId).delete().await()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
    
    suspend fun submitAnswer(
        activityId: String, 
        userAnswer: String
    ): Pair<Boolean, String> {
        try {
            // Obtener la actividad actual
            val activity = getActivityById(activityId) ?: 
                return Pair(false, "No se encontró la actividad")
            
            // Verificar si la respuesta es correcta
            val isCorrect = when (activity.type) {
                QuestionType.MULTIPLE_CHOICE, QuestionType.TRUE_FALSE -> {
                    userAnswer.trim() == activity.correctAnswer.trim()
                }
                QuestionType.SHORT_ANSWER -> {
                    userAnswer.trim().equals(activity.correctAnswer.trim(), ignoreCase = true)
                }
                QuestionType.ESSAY -> {
                    // Para respuestas de ensayo, necesitamos evaluación de IA
                    false // Temporalmente, se marcará como incorrecto hasta que la IA lo evalúe
                }
            }
            
            // Actualizar la actividad con la respuesta del usuario
            val updatedActivity = activity.copy(
                userAnswer = userAnswer,
                isAnswered = true,
                isCorrect = isCorrect,
                answeredAt = Timestamp.now()
            )
            
            // Si es una pregunta de ensayo, solicitar evaluación de IA
            val feedback = if (activity.type == QuestionType.ESSAY) {
                generateAIFeedback(activity.question, activity.correctAnswer, userAnswer)
            } else {
                if (isCorrect) "¡Correcto! ${activity.explanation}" else "Incorrecto. ${activity.explanation}"
            }
            
            // Calcular puntuación
            val score = if (isCorrect) 100 else 0
            
            // Actualizar con feedback y puntuación
            val finalActivity = updatedActivity.copy(
                feedback = feedback,
                score = score
            )
            
            // Guardar en Firestore
            updateActivity(finalActivity)
            
            return Pair(isCorrect, feedback)
        } catch (e: Exception) {
            e.printStackTrace()
            return Pair(false, "Error al procesar la respuesta: ${e.message}")
        }
    }
    
    suspend fun generateActivitiesForSession(
        studyPlanId: String,
        sessionId: String,
        sessionTitle: String,
        sessionContent: String,
        count: Int = 3
    ): List<ActivityQuestion> {
        try {
            // Crear un ID único para esta solicitud
            val documentId = UUID.randomUUID().toString()
            
            // Crear el prompt para la IA
            val prompt = """
                Genera $count preguntas de evaluación específicas y detalladas sobre el siguiente tema de estudio:
                
                Título de la sesión: $sessionTitle
                
                Contenido:
                $sessionContent
                
                IMPORTANTE: Las preguntas deben estar directamente relacionadas con el contenido específico de la sesión.
                Extrae conceptos, definiciones, ejemplos y aplicaciones mencionados en el contenido.
                No generes preguntas genéricas o que no estén basadas en la información proporcionada.
                
                Para cada pregunta, proporciona:
                1. La pregunta (debe ser clara, específica y basada en el contenido)
                2. Tipo de pregunta (MULTIPLE_CHOICE, TRUE_FALSE, SHORT_ANSWER, ESSAY)
                3. Opciones (para preguntas de opción múltiple, deben ser plausibles y relacionadas con el tema)
                4. Respuesta correcta
                5. Explicación detallada de la respuesta (citando la parte relevante del contenido)
                6. Dificultad (EASY, MEDIUM, HARD)
                
                Distribuye las preguntas de la siguiente manera:
                - 1 pregunta fácil (EASY)
                - 1 pregunta de dificultad media (MEDIUM)
                - 1 pregunta difícil (HARD)
                
                Incluye al menos una pregunta de cada tipo:
                - Una pregunta de opción múltiple (MULTIPLE_CHOICE)
                - Una pregunta de verdadero/falso (TRUE_FALSE)
                - Una pregunta de ensayo (ESSAY) que requiera aplicar los conceptos aprendidos
                
                Formatea la respuesta en JSON para que pueda ser procesada automáticamente.
                Ejemplo:
                [
                  {
                    "question": "¿Cuál de los siguientes es un ejemplo de [concepto específico mencionado en el contenido]?",
                    "type": "MULTIPLE_CHOICE",
                    "options": ["Opción 1 relacionada", "Opción 2 relacionada", "Opción 3 relacionada", "Opción 4 relacionada"],
                    "correctAnswer": "Opción correcta",
                    "explanation": "Explicación detallada citando el contenido: 'cita textual del contenido'",
                    "difficulty": "MEDIUM"
                  }
                ]
            """.trimIndent()
            
            // Preparar los datos del documento
            val requestData = hashMapOf(
                "prompt" to prompt,
                "timestamp" to Timestamp.now(),
                "userId" to getCurrentUserId(),
                "type" to "activities"
            )
            
            // Guardar el documento en Firestore
            generateCollection.document(documentId).set(requestData).await()
            
            // Esperar a que la IA genere la respuesta
            var attempts = 0
            val maxAttempts = 10
            
            while (attempts < maxAttempts) {
                // Esperar un momento para que la extensión procese
                kotlinx.coroutines.delay(2000) // 2 segundos
                
                // Obtener el documento actualizado
                val document = generateCollection.document(documentId).get().await()
                
                // Verificar si ya contiene la respuesta
                if (document.contains("response")) {
                    val response = document.getString("response") ?: return emptyList()
                    
                    // Procesar la respuesta JSON
                    val activities = parseActivitiesFromAIResponse(response, studyPlanId, sessionId)
                    
                    // Guardar las actividades en Firestore
                    activities.forEach { activity ->
                        createActivity(activity)
                    }
                    
                    return activities
                }
                
                attempts++
            }
            
            return emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }
    
    private suspend fun generateAIFeedback(question: String, correctAnswer: String, userAnswer: String): String {
        try {
            // Crear un ID único para esta solicitud
            val documentId = UUID.randomUUID().toString()
            
            // Crear el prompt para la IA
            val prompt = """
                Evalúa la siguiente respuesta de un estudiante:
                
                Pregunta: $question
                
                Respuesta correcta: $correctAnswer
                
                Respuesta del estudiante: $userAnswer
                
                Proporciona:
                1. Si la respuesta es correcta o incorrecta
                2. Una explicación detallada de por qué
                3. Sugerencias para mejorar
                4. Una puntuación del 0 al 100
            """.trimIndent()
            
            // Preparar los datos del documento
            val requestData = hashMapOf(
                "prompt" to prompt,
                "timestamp" to Timestamp.now(),
                "userId" to getCurrentUserId(),
                "type" to "feedback"
            )
            
            // Guardar el documento en Firestore
            generateCollection.document(documentId).set(requestData).await()
            
            // Esperar a que la IA genere la respuesta
            var attempts = 0
            val maxAttempts = 10
            
            while (attempts < maxAttempts) {
                // Esperar un momento para que la extensión procese
                kotlinx.coroutines.delay(2000) // 2 segundos
                
                // Obtener el documento actualizado
                val document = generateCollection.document(documentId).get().await()
                
                // Verificar si ya contiene la respuesta
                if (document.contains("response")) {
                    return document.getString("response") ?: 
                        "No se pudo generar retroalimentación. Por favor, revisa tu respuesta."
                }
                
                attempts++
            }
            
            return "No se pudo generar retroalimentación después de varios intentos."
        } catch (e: Exception) {
            e.printStackTrace()
            return "Error al generar retroalimentación: ${e.message}"
        }
    }
    
    // Método para parsear la respuesta de la IA y convertirla en objetos ActivityQuestion
    private fun parseActivitiesFromAIResponse(
        response: String,
        studyPlanId: String,
        sessionId: String
    ): List<ActivityQuestion> {
        val activities = mutableListOf<ActivityQuestion>()
        
        try {
            // Intentar parsear la respuesta JSON
            // Nota: En una implementación real, usaríamos una biblioteca JSON como Gson o Moshi
            // Esta es una implementación simplificada para fines de demostración
            
            // Si la respuesta no contiene actividades o no se puede parsear, crear actividades predeterminadas
            if (!response.contains("question") || !response.contains("type") || !response.contains("correctAnswer")) {
                return createDefaultActivities(studyPlanId, sessionId)
            }
            
            // Extraer las actividades de la respuesta
            // Buscar patrones como "question": "...", "type": "...", etc.
            val questionPattern = Regex("\"question\"\\s*:\\s*\"([^\"]+)\"")
            val typePattern = Regex("\"type\"\\s*:\\s*\"([^\"]+)\"")
            val optionsPattern = Regex("\"options\"\\s*:\\s*\\[([^\\]]+)\\]")
            val correctAnswerPattern = Regex("\"correctAnswer\"\\s*:\\s*\"([^\"]+)\"")
            val explanationPattern = Regex("\"explanation\"\\s*:\\s*\"([^\"]+)\"")
            val difficultyPattern = Regex("\"difficulty\"\\s*:\\s*\"([^\"]+)\"")
            
            // Dividir la respuesta en secciones para cada actividad
            val activitySections = response.split("}").filter { it.contains("question") }
            
            for (section in activitySections) {
                // Extraer los datos de la actividad
                val questionMatch = questionPattern.find(section)
                val typeMatch = typePattern.find(section)
                val optionsMatch = optionsPattern.find(section)
                val correctAnswerMatch = correctAnswerPattern.find(section)
                val explanationMatch = explanationPattern.find(section)
                val difficultyMatch = difficultyPattern.find(section)
                
                if (questionMatch != null && typeMatch != null && correctAnswerMatch != null) {
                    val question = questionMatch.groupValues[1]
                    val typeStr = typeMatch.groupValues[1]
                    val correctAnswer = correctAnswerMatch.groupValues[1]
                    val explanation = explanationMatch?.groupValues?.get(1) ?: "Sin explicación disponible"
                    val difficultyStr = difficultyMatch?.groupValues?.get(1) ?: "MEDIUM"
                    
                    // Convertir el tipo de pregunta
                    val type = try {
                        QuestionType.valueOf(typeStr)
                    } catch (e: Exception) {
                        QuestionType.MULTIPLE_CHOICE
                    }
                    
                    // Convertir la dificultad
                    val difficulty = try {
                        QuestionDifficulty.valueOf(difficultyStr)
                    } catch (e: Exception) {
                        QuestionDifficulty.MEDIUM
                    }
                    
                    // Procesar las opciones para preguntas de opción múltiple
                    val options = if (type == QuestionType.MULTIPLE_CHOICE && optionsMatch != null) {
                        optionsMatch.groupValues[1]
                            .split(",")
                            .map { it.trim().replace("\"", "") }
                            .filter { it.isNotBlank() }
                    } else if (type == QuestionType.TRUE_FALSE) {
                        listOf("Verdadero", "Falso")
                    } else {
                        emptyList()
                    }
                    
                    // Crear la actividad
                    activities.add(
                        ActivityQuestion(
                            studyPlanId = studyPlanId,
                            sessionId = sessionId,
                            question = question,
                            options = options,
                            correctAnswer = correctAnswer,
                            type = type,
                            explanation = explanation,
                            difficulty = difficulty
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // Si no se pudieron extraer actividades, crear actividades predeterminadas
        if (activities.isEmpty()) {
            return createDefaultActivities(studyPlanId, sessionId)
        }
        
        return activities
    }
    
    // Método para crear actividades predeterminadas si falla el parseo
    private fun createDefaultActivities(studyPlanId: String, sessionId: String): List<ActivityQuestion> {
        val activities = mutableListOf<ActivityQuestion>()
        
        // Actividad de opción múltiple
        activities.add(
            ActivityQuestion(
                studyPlanId = studyPlanId,
                sessionId = sessionId,
                question = "¿Cuál es el objetivo principal de esta sesión de estudio?",
                options = listOf(
                    "Memorizar fechas y datos",
                    "Comprender conceptos clave",
                    "Practicar ejercicios",
                    "Todas las anteriores"
                ),
                correctAnswer = "Comprender conceptos clave",
                type = QuestionType.MULTIPLE_CHOICE,
                explanation = "El objetivo principal de esta sesión es comprender los conceptos fundamentales antes de aplicarlos.",
                difficulty = QuestionDifficulty.MEDIUM
            )
        )
        
        // Actividad de verdadero/falso
        activities.add(
            ActivityQuestion(
                studyPlanId = studyPlanId,
                sessionId = sessionId,
                question = "¿Es importante revisar el material después de la sesión de estudio?",
                options = listOf("Verdadero", "Falso"),
                correctAnswer = "Verdadero",
                type = QuestionType.TRUE_FALSE,
                explanation = "Revisar el material después de la sesión ayuda a consolidar el aprendizaje y mejorar la retención.",
                difficulty = QuestionDifficulty.EASY
            )
        )
        
        // Actividad de ensayo
        activities.add(
            ActivityQuestion(
                studyPlanId = studyPlanId,
                sessionId = sessionId,
                question = "Explica con tus propias palabras cómo aplicarías lo aprendido en esta sesión en un contexto real.",
                options = emptyList(),
                correctAnswer = "La respuesta debe incluir ejemplos concretos de aplicación práctica de los conceptos estudiados.",
                type = QuestionType.ESSAY,
                explanation = "Una buena respuesta debe demostrar comprensión de los conceptos y capacidad para aplicarlos en situaciones reales.",
                difficulty = QuestionDifficulty.HARD
            )
        )
        
        return activities
    }
}
