package com.example.studym8.data.model

import com.google.firebase.Timestamp
import java.util.Date

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
    val aiResponse: String = ""
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
            "aiResponse" to aiResponse
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>, id: String = ""): StudyPlan {
            return StudyPlan(
                id = id,
                userId = map["userId"] as? String ?: "",
                title = map["title"] as? String ?: "",
                subject = map["subject"] as? String ?: "",
                description = map["description"] as? String ?: "",
                startDateTime = map["startDateTime"] as? Timestamp ?: Timestamp.now(),
                endDateTime = map["endDateTime"] as? Timestamp ?: Timestamp.now(),
                totalDuration = (map["totalDuration"] as? Number)?.toInt() ?: 0,
                isCompleted = map["isCompleted"] as? Boolean ?: false,
                completionRate = (map["completionRate"] as? Number)?.toInt() ?: 0,
                aiGenerated = map["aiGenerated"] as? Boolean ?: false,
                createdAt = map["createdAt"] as? Timestamp ?: Timestamp.now(),
                updatedAt = map["updatedAt"] as? Timestamp ?: Timestamp.now(),
                tags = map["tags"] as? List<String> ?: emptyList(),
                color = map["color"] as? String ?: "#4285F4",
                aiResponse = map["aiResponse"] as? String ?: ""
            )
        }
    }
} 