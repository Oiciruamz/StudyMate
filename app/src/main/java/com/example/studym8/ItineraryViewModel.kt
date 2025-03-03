package com.example.studym8

import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*
import android.util.Log

// Clase ViewModel para los itinerarios
class ItineraryViewModel : androidx.lifecycle.ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"

    init {
        if (FirebaseAuth.getInstance().currentUser == null) {
            FirebaseAuth.getInstance().signInAnonymously()
                .addOnSuccessListener {
                    Log.d("Auth", "Autenticación anónima exitosa")
                }
                .addOnFailureListener { e ->
                    Log.e("Auth", "Error en autenticación anónima", e)
                }
        }
    }

    // Función para generar un itinerario usando la IA
    suspend fun generateAIStudyPlan(
        subject: String,
        startDateTime: Date,
        endDateTime: Date
    ): String? {
        val db = FirebaseFirestore.getInstance()
        val generateCollectionRef = db.collection("generate")

        try {
            // Calculamos la duración en horas y minutos
            val durationMs = endDateTime.time - startDateTime.time
            val durationHours = (durationMs / (1000 * 60 * 60)).toInt()
            val durationMinutes = ((durationMs % (1000 * 60 * 60)) / (1000 * 60)).toInt()

            // Crear el prompt para la IA
            val prompt = """
                Crea un plan de estudio detallado para el tema '$subject' con duración de $durationHours horas y $durationMinutes minutos.
                El plan debe seguir la metodología Pomodoro (25 minutos de estudio y 5 minutos de descanso).
                Incluye actividades específicas para cada sesión de estudio, como lecturas, ejercicios prácticos, y repasos.
                Proporciona una estructura clara dividida en sesiones, con objetivos específicos para cada una.
            """.trimIndent()

            // Crear el documento para la extensión de Gemini
            val documentId = UUID.randomUUID().toString()
            val requestData = hashMapOf(
                "prompt" to prompt,
                "timestamp" to Timestamp.now()
            )

            // Guardar el documento en la colección "generate"
            generateCollectionRef.document(documentId).set(requestData).await()

            // Esperar a que la IA genere la respuesta (con timeout)
            var attempts = 0
            val maxAttempts = 10

            while (attempts < maxAttempts) {
                // Esperar un momento para que la extensión procese
                kotlinx.coroutines.delay(2000) // 2 segundos

                // Obtener el documento actualizado
                val document = generateCollectionRef.document(documentId).get().await()

                // Verificar si ya contiene la respuesta
                if (document.contains("response")) {
                    return document.getString("response")
                }

                attempts++
            }

            return "No se pudo generar el plan de estudio con IA después de varios intentos."
        } catch (e: Exception) {
            Log.e("ItineraryViewModel", "Error al generar plan con IA", e)
            return null
        }
    }

    // Función para crear un plan de estudio basado en la respuesta de la IA
    fun createAIStudyPlan(
        subject: String,
        startDateTime: Date,
        endDateTime: Date,
        aiResponse: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        // Calcular duración total en minutos
        val durationMs = endDateTime.time - startDateTime.time
        val durationMinutes = (durationMs / (1000 * 60)).toInt()

        // Crear modelo de datos para el plan de estudio
        val studyPlan = hashMapOf(
            "userId" to userId,
            "title" to "Estudio de $subject",
            "subject" to subject,
            "description" to "Plan de estudio generado con IA",
            "startDateTime" to Timestamp(startDateTime),
            "endDateTime" to Timestamp(endDateTime),
            "totalDuration" to durationMinutes,
            "isCompleted" to false,
            "completionRate" to 0,
            "aiGenerated" to true,
            "createdAt" to Timestamp.now(),
            "updatedAt" to Timestamp.now(),
            "tags" to listOf(subject.lowercase()),
            "color" to "#4285F4",  // Color azul de Google
            "aiResponse" to aiResponse  // Guardamos la respuesta completa de la IA
        )

        // Guardar en Firestore
        db.collection("studyPlans")
            .add(studyPlan)
            .addOnSuccessListener { documentReference ->
                onSuccess(documentReference.id)
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

    // Función para obtener los itinerarios del usuario
    suspend fun getStudyPlans(): List<Map<String, Any>> {
        return try {
            val snapshot = db.collection("studyPlans")
                .whereEqualTo("userId", userId)
                .orderBy("startDateTime")
                .get()
                .await()

            snapshot.documents.mapNotNull { it.data }
        } catch (e: Exception) {
            emptyList()
        }
    }
}