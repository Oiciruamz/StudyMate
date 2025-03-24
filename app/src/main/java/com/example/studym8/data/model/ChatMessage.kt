package com.example.studym8.data.model

import com.google.firebase.Timestamp
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String = "",
    val isFromUser: Boolean = true,
    val timestamp: Timestamp = Timestamp.now()
) 