package com.example.studym8.data.repository

import com.example.studym8.data.model.StudyPlan
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

class StudyPlanRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val studyPlansCollection = db.collection("studyPlans")
    
    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("No hay una sesión de usuario activa")
    }
    
    suspend fun getStudyPlans(): List<StudyPlan> {
        val userId = getCurrentUserId()
        
        try {
            val querySnapshot = studyPlansCollection
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
                
            return querySnapshot.documents.map { document ->
                val data = document.data ?: mapOf()
                StudyPlan.fromMap(data, document.id)
            }
        } catch (e: Exception) {
            // En caso de error, devolver lista vacía
            return emptyList()
        }
    }
    
    suspend fun getStudyPlanById(planId: String): StudyPlan? {
        try {
            val document = studyPlansCollection.document(planId).get().await()
            
            return if (document.exists()) {
                val data = document.data ?: mapOf()
                StudyPlan.fromMap(data, document.id)
            } else {
                null
            }
        } catch (e: Exception) {
            return null
        }
    }
    
    suspend fun createStudyPlan(studyPlan: StudyPlan): String? {
        try {
            // Asegurarse de que el plan tenga el userId correcto
            val planWithUserId = studyPlan.copy(
                userId = getCurrentUserId(), 
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            
            // Crear nuevo documento en Firestore
            val documentRef = studyPlansCollection.add(planWithUserId.toMap()).await()
            return documentRef.id
        } catch (e: Exception) {
            return null
        }
    }
    
    suspend fun updateStudyPlan(studyPlan: StudyPlan): Boolean {
        try {
            // Actualizar el plan de estudio con la hora actual
            val updatedPlan = studyPlan.copy(updatedAt = Timestamp.now())
            
            // Actualizar en Firestore
            studyPlansCollection.document(studyPlan.id).set(updatedPlan.toMap()).await()
            return true
        } catch (e: Exception) {
            return false
        }
    }
    
    suspend fun deleteStudyPlan(planId: String): Boolean {
        try {
            studyPlansCollection.document(planId).delete().await()
            return true
        } catch (e: Exception) {
            return false
        }
    }
    
    suspend fun updateCompletionStatus(planId: String, isCompleted: Boolean, completionRate: Int): Boolean {
        try {
            studyPlansCollection.document(planId)
                .update(mapOf(
                    "isCompleted" to isCompleted,
                    "completionRate" to completionRate,
                    "updatedAt" to Timestamp.now()
                )).await()
            return true
        } catch (e: Exception) {
            return false
        }
    }
    
    suspend fun generateAIStudyPlan(subject: String, startDateTime: java.util.Date, endDateTime: java.util.Date): String? {
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

            // Crear el documento para la extensión de Gemini con el ID del usuario actual
            val documentId = UUID.randomUUID().toString()
            val requestData = hashMapOf(
                "prompt" to prompt,
                "timestamp" to Timestamp.now(),
                "userId" to getCurrentUserId()
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
            return null
        }
    }
} 