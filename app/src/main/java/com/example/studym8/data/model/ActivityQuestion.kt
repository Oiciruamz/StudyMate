package com.example.studym8.data.model

import com.google.firebase.Timestamp
import java.util.UUID

/**
 * Modelo para representar una pregunta o actividad de evaluación
 */
data class ActivityQuestion(
    val id: String = UUID.randomUUID().toString(),
    val studyPlanId: String = "",
    val sessionId: String = "",
    val question: String = "",
    val options: List<String> = emptyList(), // Para preguntas de opción múltiple
    val correctAnswer: String = "", // Puede ser el índice de la opción correcta o la respuesta textual
    val type: QuestionType = QuestionType.MULTIPLE_CHOICE,
    val explanation: String = "", // Explicación de la respuesta correcta
    val difficulty: QuestionDifficulty = QuestionDifficulty.MEDIUM,
    val userAnswer: String = "", // Respuesta proporcionada por el usuario
    val isAnswered: Boolean = false,
    val isCorrect: Boolean = false,
    val feedback: String = "", // Retroalimentación personalizada de la IA
    val score: Int = 0, // Puntuación de 0 a 100
    val createdAt: Timestamp = Timestamp.now(),
    val answeredAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf(
            "id" to id,
            "studyPlanId" to studyPlanId,
            "sessionId" to sessionId,
            "question" to question,
            "options" to options,
            "correctAnswer" to correctAnswer,
            "type" to type.name,
            "explanation" to explanation,
            "difficulty" to difficulty.name,
            "userAnswer" to userAnswer,
            "isAnswered" to isAnswered,
            "isCorrect" to isCorrect,
            "feedback" to feedback,
            "score" to score,
            "createdAt" to createdAt
        )
        
        // Solo incluir answeredAt si no es nulo
        answeredAt?.let { map["answeredAt"] = it }
        
        return map
    }

    companion object {
        fun fromMap(map: Map<String, Any>, id: String = ""): ActivityQuestion {
            return ActivityQuestion(
                id = id.ifEmpty { map["id"] as? String ?: UUID.randomUUID().toString() },
                studyPlanId = map["studyPlanId"] as? String ?: "",
                sessionId = map["sessionId"] as? String ?: "",
                question = map["question"] as? String ?: "",
                options = (map["options"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                correctAnswer = map["correctAnswer"] as? String ?: "",
                type = try {
                    QuestionType.valueOf(map["type"] as? String ?: QuestionType.MULTIPLE_CHOICE.name)
                } catch (e: Exception) {
                    QuestionType.MULTIPLE_CHOICE
                },
                explanation = map["explanation"] as? String ?: "",
                difficulty = try {
                    QuestionDifficulty.valueOf(map["difficulty"] as? String ?: QuestionDifficulty.MEDIUM.name)
                } catch (e: Exception) {
                    QuestionDifficulty.MEDIUM
                },
                userAnswer = map["userAnswer"] as? String ?: "",
                isAnswered = map["isAnswered"] as? Boolean ?: false,
                isCorrect = map["isCorrect"] as? Boolean ?: false,
                feedback = map["feedback"] as? String ?: "",
                score = (map["score"] as? Number)?.toInt() ?: 0,
                createdAt = map["createdAt"] as? Timestamp ?: Timestamp.now(),
                answeredAt = map["answeredAt"] as? Timestamp
            )
        }
    }
}

enum class QuestionType {
    MULTIPLE_CHOICE,
    TRUE_FALSE,
    SHORT_ANSWER,
    ESSAY
}

enum class QuestionDifficulty {
    EASY,
    MEDIUM,
    HARD
}
