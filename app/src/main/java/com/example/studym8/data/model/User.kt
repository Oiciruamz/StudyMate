package com.example.studym8.data.model

import com.google.firebase.Timestamp

data class User(
    val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val lastLoginAt: Timestamp = Timestamp.now(),
    val preferences: Map<String, Any> = mapOf()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "displayName" to displayName,
            "email" to email,
            "photoUrl" to photoUrl,
            "createdAt" to createdAt,
            "lastLoginAt" to lastLoginAt,
            "preferences" to preferences
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>, id: String = ""): User {
            return User(
                id = id,
                displayName = map["displayName"] as? String ?: "",
                email = map["email"] as? String ?: "",
                photoUrl = map["photoUrl"] as? String ?: "",
                createdAt = map["createdAt"] as? Timestamp ?: Timestamp.now(),
                lastLoginAt = map["lastLoginAt"] as? Timestamp ?: Timestamp.now(),
                preferences = map["preferences"] as? Map<String, Any> ?: mapOf()
            )
        }
    }
} 