package com.example.studym8.data.repository

import com.example.studym8.data.model.StudyPlan
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Repositorio para obtener y analizar estadísticas basadas en los datos de planes de estudio
 */
class StatsRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val studyPlansCollection = db.collection("studyPlans")
    
    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("No hay una sesión de usuario activa")
    }
    
    /**
     * Obtiene estadísticas diarias para un período
     * @param days Número de días a incluir (por defecto 7 días)
     * @return Mapa con fecha como clave y número de planes completados como valor
     */
    suspend fun getDailyCompletionStats(days: Int = 7): Map<String, Int> {
        val userId = getCurrentUserId()
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        val startDate = calendar.time
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        try {
            val querySnapshot = studyPlansCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isCompleted", true)
                .whereGreaterThanOrEqualTo("updatedAt", Timestamp(startDate))
                .get()
                .await()
                
            // Agrupar por fecha
            val completionsByDate = mutableMapOf<String, Int>()
            
            // Inicializar todas las fechas en el rango con 0
            for (i in 0 until days) {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -i)
                val dateKey = dateFormat.format(cal.time)
                completionsByDate[dateKey] = 0
            }
            
            // Procesar resultados
            querySnapshot.documents.forEach { document ->
                val data = document.data ?: return@forEach
                val studyPlan = StudyPlan.fromMap(data, document.id)
                
                // Si está completado, incrementar contador para esa fecha
                if (studyPlan.isCompleted) {
                    val completionDate = studyPlan.updatedAt.toDate()
                    val dateKey = dateFormat.format(completionDate)
                    
                    // Solo contar si está dentro del rango solicitado
                    if (completionsByDate.containsKey(dateKey)) {
                        completionsByDate[dateKey] = (completionsByDate[dateKey] ?: 0) + 1
                    }
                }
            }
            
            return completionsByDate
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyMap()
        }
    }
    
    /**
     * Obtiene los planes con mejor tiempo de completado
     * @param limit Número máximo de planes a retornar
     * @return Lista de planes ordenados por tiempo de completado (ascendente)
     */
    suspend fun getBestCompletionTimePlans(limit: Int = 5): List<StudyPlan> {
        val userId = getCurrentUserId()
        
        try {
            val querySnapshot = studyPlansCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isCompleted", true)
                .get()
                .await()
                
            // Mapear documentos a StudyPlan
            val completedPlans = querySnapshot.documents.mapNotNull { document ->
                val data = document.data ?: return@mapNotNull null
                StudyPlan.fromMap(data, document.id)
            }
            
            // Ordenar por duración y limitar
            return completedPlans
                .sortedBy { it.totalDuration }
                .take(limit)
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }
    
    /**
     * Obtiene estadísticas de distribución por tema
     * @return Mapa con tema como clave y cantidad de planes como valor
     */
    suspend fun getSubjectDistribution(): Map<String, Int> {
        val userId = getCurrentUserId()
        
        try {
            val querySnapshot = studyPlansCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
                
            // Agrupar por tema
            return querySnapshot.documents
                .mapNotNull { document -> 
                    val data = document.data ?: return@mapNotNull null
                    StudyPlan.fromMap(data, document.id)
                }
                .groupBy { it.subject }
                .mapValues { (_, plans) -> plans.size }
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyMap()
        }
    }
    
    /**
     * Calcula cuántos días consecutivos ha estudiado el usuario
     * @return Número de días consecutivos
     */
    suspend fun getConsecutiveStudyDays(): Int {
        val userId = getCurrentUserId()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        try {
            // Obtener planes completados en el último mes
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, -1)
            val startDate = calendar.time
            
            val querySnapshot = studyPlansCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isCompleted", true)
                .whereGreaterThanOrEqualTo("updatedAt", Timestamp(startDate))
                .get()
                .await()
                
            // Obtener fechas de completado y formatearlas como YYYY-MM-DD
            val completionDates = querySnapshot.documents
                .mapNotNull { document ->
                    val data = document.data ?: return@mapNotNull null
                    val plan = StudyPlan.fromMap(data, document.id)
                    plan.updatedAt.toDate()
                }
                .map { dateFormat.format(it) }
                .distinct()
                .sorted()
                
            if (completionDates.isEmpty()) return 0
            
            // Verificar si hay un plan completado hoy
            val today = dateFormat.format(Date())
            
            // Si no hay estudio hoy, devolver 0
            if (completionDates.last() != today) return 0
            
            // Contar días consecutivos
            var consecutiveDays = 1
            
            for (i in completionDates.size - 2 downTo 0) {
                // Verificar si la fecha anterior es un día antes
                val cal1 = Calendar.getInstance().apply {
                    time = dateFormat.parse(completionDates[i + 1])!!
                    add(Calendar.DAY_OF_YEAR, -1)
                }
                val expectedPreviousDay = dateFormat.format(cal1.time)
                
                if (completionDates[i] == expectedPreviousDay) {
                    consecutiveDays++
                } else {
                    break
                }
            }
            
            return consecutiveDays
        } catch (e: Exception) {
            e.printStackTrace()
            return 0
        }
    }
    
    /**
     * Obtiene estadísticas generales sobre los planes de estudio
     */
    suspend fun getGeneralStats(): Map<String, Any> {
        val userId = getCurrentUserId()
        
        try {
            val querySnapshot = studyPlansCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
                
            val plans = querySnapshot.documents.mapNotNull { document ->
                val data = document.data ?: return@mapNotNull null
                StudyPlan.fromMap(data, document.id)
            }
            
            // Calcular estadísticas generales
            val totalPlans = plans.size
            val completedPlans = plans.count { it.isCompleted }
            val totalDuration = plans.sumOf { it.totalDuration }
            
            val averageCompletionRate = if (completedPlans > 0) {
                plans.filter { it.isCompleted }.map { it.completionRate }.average()
            } else 0.0
            
            val perfectCompletions = plans.count { it.isCompleted && it.completionRate == 100 }
            
            val mostStudiedSubject = plans
                .groupBy { it.subject }
                .maxByOrNull { (_, subjectPlans) -> subjectPlans.size }
                ?.key ?: ""
                
            return mapOf(
                "totalPlans" to totalPlans,
                "completedPlans" to completedPlans,
                "totalDuration" to totalDuration,
                "averageCompletionRate" to averageCompletionRate,
                "perfectCompletions" to perfectCompletions,
                "mostStudiedSubject" to mostStudiedSubject
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyMap()
        }
    }
} 