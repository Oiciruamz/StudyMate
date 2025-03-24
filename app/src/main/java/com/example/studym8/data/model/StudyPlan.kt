package com.example.studym8.data.model

import com.google.firebase.Timestamp
import java.util.Date

// Clase para representar una sesión de estudio
data class StudySession(
    val id: String = "",
    val title: String = "",
    val duration: Int = 0, // Duración en minutos
    val isCompleted: Boolean = false,
    val notes: String = ""
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "title" to title,
            "duration" to duration,
            "isCompleted" to isCompleted,
            "notes" to notes
        )
    }
    
    companion object {
        fun fromMap(map: Map<String, Any>): StudySession {
            return StudySession(
                id = map["id"] as? String ?: "",
                title = map["title"] as? String ?: "",
                duration = (map["duration"] as? Number)?.toInt() ?: 0,
                isCompleted = map["isCompleted"] as? Boolean ?: false,
                notes = map["notes"] as? String ?: ""
            )
        }
    }
}

data class StudyPlan(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val subject: String = "",
    val description: String = "",
    val startDateTime: Timestamp = Timestamp.now(),
    val endDateTime: Timestamp = Timestamp.now(),
    val totalDuration: Int = 0, // Duración en minutos
    val isCompleted: Boolean = false,
    val completionRate: Int = 0,
    val aiGenerated: Boolean = false,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val tags: List<String> = emptyList(),
    val color: String = "#4285F4",
    val aiResponse: String = "",
    val sessions: List<StudySession> = emptyList()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "title" to title,
            "subject" to subject,
            "description" to description,
            "startDateTime" to startDateTime,
            "endDateTime" to endDateTime,
            "totalDuration" to totalDuration,
            "isCompleted" to isCompleted,
            "completionRate" to completionRate,
            "aiGenerated" to aiGenerated,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "tags" to tags,
            "color" to color,
            "aiResponse" to aiResponse,
            "sessions" to sessions.map { it.toMap() }
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>, id: String = ""): StudyPlan {
            println("StudyPlan.fromMap - ID: $id, Datos: $map")
            try {
                // Procesamiento seguro para cada campo
                val userId = map["userId"] as? String ?: ""
                val title = map["title"] as? String ?: "Sin título"
                val subject = map["subject"] as? String ?: "General"
                val description = map["description"] as? String ?: ""
                
                val startDateTime = when (val startDT = map["startDateTime"]) {
                    is Timestamp -> startDT
                    is Date -> Timestamp(startDT)
                    else -> Timestamp.now()
                }
                
                val endDateTime = when (val endDT = map["endDateTime"]) {
                    is Timestamp -> endDT
                    is Date -> Timestamp(endDT)
                    else -> Timestamp.now()
                }
                
                val totalDuration = when (val duration = map["totalDuration"]) {
                    is Number -> duration.toInt()
                    else -> 0
                }
                
                val isCompleted = map["isCompleted"] as? Boolean ?: false
                
                val completionRate = when (val rate = map["completionRate"]) {
                    is Number -> rate.toInt()
                    else -> 0
                }
                
                val aiGenerated = map["aiGenerated"] as? Boolean ?: false
                
                val createdAt = when (val created = map["createdAt"]) {
                    is Timestamp -> created
                    is Date -> Timestamp(created)
                    else -> Timestamp.now()
                }
                
                val updatedAt = when (val updated = map["updatedAt"]) {
                    is Timestamp -> updated
                    is Date -> Timestamp(updated)
                    else -> Timestamp.now()
                }
                
                val tags = when (val tagsList = map["tags"]) {
                    is List<*> -> tagsList.filterIsInstance<String>()
                    else -> emptyList()
                }
                
                val color = map["color"] as? String ?: "#4285F4"
                val aiResponse = map["aiResponse"] as? String ?: ""
                
                // Procesar las sesiones si existen
                val sessions = when (val sessionsList = map["sessions"]) {
                    is List<*> -> {
                        sessionsList.filterIsInstance<Map<*, *>>().map { sessionMap ->
                            @Suppress("UNCHECKED_CAST")
                            StudySession.fromMap(sessionMap as Map<String, Any>)
                        }
                    }
                    else -> emptyList()
                }
                
                return StudyPlan(
                    id = id,
                    userId = userId,
                    title = title,
                    subject = subject,
                    description = description,
                    startDateTime = startDateTime,
                    endDateTime = endDateTime,
                    totalDuration = totalDuration,
                    isCompleted = isCompleted,
                    completionRate = completionRate,
                    aiGenerated = aiGenerated,
                    createdAt = createdAt,
                    updatedAt = updatedAt,
                    tags = tags,
                    color = color,
                    aiResponse = aiResponse,
                    sessions = sessions
                )
            } catch (e: Exception) {
                println("Error al convertir documento a StudyPlan: ${e.message}")
                e.printStackTrace()
                // Devolver un plan de estudio por defecto en caso de error
                return StudyPlan(id = id)
            }
        }
    }
} 