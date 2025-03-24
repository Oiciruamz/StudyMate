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
            println("StudyPlanRepository: Intentando obtener planes para usuario: $userId")
            
            // Modificar la consulta para evitar el error de índice
            // Primero filtramos por userId sin ordenar
            val querySnapshot = studyPlansCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
                
            println("StudyPlanRepository: Documentos obtenidos: ${querySnapshot.documents.size}")
            
            val plans = querySnapshot.documents.mapNotNull { document ->
                try {
                    val data = document.data ?: return@mapNotNull null
                    println("StudyPlanRepository: Documento ID: ${document.id}, Datos: $data")
                    StudyPlan.fromMap(data, document.id)
                } catch (e: Exception) {
                    println("StudyPlanRepository: Error al procesar documento ${document.id}: ${e.message}")
                    null
                }
            }
            
            // Ordenar los planes en memoria en lugar de en la consulta
            val sortedPlans = plans.sortedByDescending { 
                it.createdAt.toDate().time 
            }
            
            println("StudyPlanRepository: Planes procesados: ${sortedPlans.size}")
            return sortedPlans
            
        } catch (e: Exception) {
            println("StudyPlanRepository: Error al obtener planes: ${e.message}")
            e.printStackTrace()
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
            
            println("StudyPlanRepository: Creando nuevo plan con título: ${planWithUserId.title}")
            
            // Crear nuevo documento en Firestore
            val documentRef = studyPlansCollection.add(planWithUserId.toMap()).await()
            val planId = documentRef.id
            
            // Actualizamos el documento con su propio ID para facilitar referencias
            val updatedData = planWithUserId.copy(id = planId).toMap()
            studyPlansCollection.document(planId).set(updatedData).await()
            
            println("StudyPlanRepository: Plan creado con ID: $planId")
            return planId
        } catch (e: Exception) {
            println("StudyPlanRepository: Error al crear plan: ${e.message}")
            e.printStackTrace()
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

            // Crear el prompt para la IA mejorado para más consistencia
            val prompt = """
                Crea un plan de estudio detallado para el tema '$subject' con duración de $durationHours horas y $durationMinutes minutos.
                El plan debe seguir la metodología Pomodoro (25 minutos de estudio y 5 minutos de descanso).
                
                Estructura tu respuesta siguiendo EXACTAMENTE este formato para cada sesión:
                
                # Plan de Estudio: $subject
                
                ## Sesión 1: [Título descriptivo]
                [Descripción detallada de la sesión, incluyendo objetivos, actividades y materiales]
                
                ## Sesión 2: [Título descriptivo]
                [Descripción detallada de la sesión, incluyendo objetivos, actividades y materiales]
                
                [Continuar con el número de sesiones necesarias para completar el tiempo disponible]
                
                # Recomendaciones finales
                [Breves recomendaciones para sacar el máximo provecho del estudio]
                
                Asegúrate de que cada sesión tenga una duración de 25 minutos y esté claramente etiquetada como "Sesión X".
                Cada sesión debe tener un título descriptivo y una explicación detallada de lo que el estudiante debe hacer.
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
    
    suspend fun generateChatResponse(prompt: String): String? {
        val generateCollectionRef = db.collection("generate")
        
        try {
            // Crear un ID único para esta solicitud
            val documentId = UUID.randomUUID().toString()
            
            // Preparar los datos del documento
            val requestData = hashMapOf(
                "prompt" to prompt,
                "timestamp" to Timestamp.now(),
                "userId" to getCurrentUserId(),
                "type" to "chat"  // Indicar que es una solicitud de chat
            )
            
            // Guardar el documento en Firestore
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
            
            return "No se pudo generar una respuesta después de varios intentos. Por favor, intenta de nuevo más tarde."
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
} 