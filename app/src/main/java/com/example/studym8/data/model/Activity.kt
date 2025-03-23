package com.example.studym8.data.model

import com.google.firebase.Timestamp

data class Activity(
    val id: String = "",
    val studyPlanId: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val dueDate: Timestamp = Timestamp.now(),
    val status: ActivityStatus = ActivityStatus.TODO,
    val priority: ActivityPriority = ActivityPriority.MEDIUM,
    val duration: Int = 0, // Duración en minutos
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "studyPlanId" to studyPlanId,
            "userId" to userId,
            "title" to title,
            "description" to description,
            "dueDate" to dueDate,
            "status" to status.name,
            "priority" to priority.name,
            "duration" to duration,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>, id: String = ""): Activity {
            return Activity(
                id = id,
                studyPlanId = map["studyPlanId"] as? String ?: "",
                userId = map["userId"] as? String ?: "",
                title = map["title"] as? String ?: "",
                description = map["description"] as? String ?: "",
                dueDate = map["dueDate"] as? Timestamp ?: Timestamp.now(),
                status = ActivityStatus.valueOf((map["status"] as? String) ?: ActivityStatus.TODO.name),
                priority = ActivityPriority.valueOf((map["priority"] as? String) ?: ActivityPriority.MEDIUM.name),
                duration = (map["duration"] as? Number)?.toInt() ?: 0,
                createdAt = map["createdAt"] as? Timestamp ?: Timestamp.now(),
                updatedAt = map["updatedAt"] as? Timestamp ?: Timestamp.now()
            )
        }
    }
}

enum class ActivityStatus {
    TODO, IN_PROGRESS, COMPLETED
}

enum class ActivityPriority {
    LOW, MEDIUM, HIGH
} 