package com.example.studym8.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studym8.data.model.ActivityQuestion
import com.example.studym8.data.model.QuestionDifficulty
import com.example.studym8.data.model.QuestionType

/**
 * Componente que muestra una lista de actividades disponibles
 */
@Composable
fun ActivityList(
    activities: List<ActivityQuestion>,
    onActivitySelected: (ActivityQuestion) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Actividades de evaluación",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (activities.isEmpty()) {
                Text(
                    text = "No hay actividades disponibles para esta sesión.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            } else {
                activities.forEachIndexed { index, activity ->
                    ActivityListItem(
                        activity = activity,
                        index = index,
                        onClick = { onActivitySelected(activity) }
                    )

                    if (index < activities.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Componente que muestra un elemento de la lista de actividades
 */
@Composable
fun ActivityListItem(
    activity: ActivityQuestion,
    index: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Indicador de estado (completado o pendiente)
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = if (activity.isAnswered) {
                        if (activity.isCorrect) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
                    } else MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (activity.isAnswered) {
                Icon(
                    imageVector = if (activity.isCorrect) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = if (activity.isCorrect) "Correcto" else "Incorrecto",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = "${index + 1}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = activity.question,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 2
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                // Tipo de pregunta
                Text(
                    text = when (activity.type) {
                        QuestionType.MULTIPLE_CHOICE -> "Opción múltiple"
                        QuestionType.TRUE_FALSE -> "Verdadero/Falso"
                        QuestionType.SHORT_ANSWER -> "Respuesta corta"
                        QuestionType.ESSAY -> "Ensayo"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Text(
                    text = " • ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                // Dificultad
                Text(
                    text = when (activity.difficulty) {
                        QuestionDifficulty.EASY -> "Fácil"
                        QuestionDifficulty.MEDIUM -> "Media"
                        QuestionDifficulty.HARD -> "Difícil"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when (activity.difficulty) {
                        QuestionDifficulty.EASY -> Color(0xFF4CAF50)
                        QuestionDifficulty.MEDIUM -> Color(0xFFFFA000)
                        QuestionDifficulty.HARD -> Color(0xFFF44336)
                    }
                )

                if (activity.isAnswered) {
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    // Puntuación
                    Text(
                        text = "${activity.score}/100",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (activity.score >= 70) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                }
            }
        }

        // Icono de estado
        Icon(
            imageVector = if (activity.isAnswered) Icons.Default.Check else Icons.Default.QuestionAnswer,
            contentDescription = if (activity.isAnswered) "Completado" else "Pendiente",
            tint = if (activity.isAnswered) {
                if (activity.isCorrect) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error
            } else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

/**
 * Componente que muestra una actividad para responder
 */
@Composable
fun ActivityQuestionView(
    activity: ActivityQuestion,
    userAnswer: String,
    onAnswerChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    isLoading: Boolean,
    evaluationResult: Pair<Boolean, String>?,
    onDismissResult: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Encabezado con tipo de pregunta y dificultad
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tipo de pregunta
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.QuestionAnswer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = when (activity.type) {
                            QuestionType.MULTIPLE_CHOICE -> "Opción múltiple"
                            QuestionType.TRUE_FALSE -> "Verdadero/Falso"
                            QuestionType.SHORT_ANSWER -> "Respuesta corta"
                            QuestionType.ESSAY -> "Ensayo"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // Dificultad
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            when (activity.difficulty) {
                                QuestionDifficulty.EASY -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                                QuestionDifficulty.MEDIUM -> Color(0xFFFFA000).copy(alpha = 0.1f)
                                QuestionDifficulty.HARD -> Color(0xFFF44336).copy(alpha = 0.1f)
                            }
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when (activity.difficulty) {
                            QuestionDifficulty.EASY -> "Fácil"
                            QuestionDifficulty.MEDIUM -> "Media"
                            QuestionDifficulty.HARD -> "Difícil"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = when (activity.difficulty) {
                            QuestionDifficulty.EASY -> Color(0xFF4CAF50)
                            QuestionDifficulty.MEDIUM -> Color(0xFFFFA000)
                            QuestionDifficulty.HARD -> Color(0xFFF44336)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pregunta
            Text(
                text = activity.question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Contenido según el tipo de pregunta
            when (activity.type) {
                QuestionType.MULTIPLE_CHOICE -> {
                    MultipleChoiceQuestion(
                        options = activity.options,
                        selectedOption = userAnswer,
                        onOptionSelected = onAnswerChanged,
                        isAnswered = activity.isAnswered,
                        correctAnswer = activity.correctAnswer
                    )
                }
                QuestionType.TRUE_FALSE -> {
                    TrueFalseQuestion(
                        selectedOption = userAnswer,
                        onOptionSelected = onAnswerChanged,
                        isAnswered = activity.isAnswered,
                        correctAnswer = activity.correctAnswer
                    )
                }
                QuestionType.SHORT_ANSWER, QuestionType.ESSAY -> {
                    TextQuestion(
                        answer = userAnswer,
                        onAnswerChanged = onAnswerChanged,
                        isMultiline = activity.type == QuestionType.ESSAY,
                        isAnswered = activity.isAnswered
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de enviar respuesta
            if (!activity.isAnswered && evaluationResult == null) {
                Button(
                    onClick = onSubmit,
                    enabled = !isLoading && userAnswer.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Enviar respuesta")
                    }
                }
            }

            // Mostrar resultado de la evaluación
            AnimatedVisibility(
                visible = evaluationResult != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                evaluationResult?.let { (isCorrect, feedback) ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        // Encabezado del resultado
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Icon(
                                imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = if (isCorrect) "Correcto" else "Incorrecto",
                                tint = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336),
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = if (isCorrect) "¡Respuesta correcta!" else "Respuesta incorrecta",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)
                            )
                        }

                        // Feedback
                        Text(
                            text = feedback,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // Botón para cerrar el resultado
                        TextButton(
                            onClick = onDismissResult,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Entendido")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Componente para preguntas de opción múltiple
 */
@Composable
fun MultipleChoiceQuestion(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    isAnswered: Boolean,
    correctAnswer: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when {
                            isAnswered && option == correctAnswer -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                            isAnswered && option == selectedOption && option != correctAnswer -> Color(0xFFF44336).copy(alpha = 0.1f)
                            option == selectedOption -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = when {
                            isAnswered && option == correctAnswer -> Color(0xFF4CAF50)
                            isAnswered && option == selectedOption && option != correctAnswer -> Color(0xFFF44336)
                            option == selectedOption -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable(enabled = !isAnswered) { onOptionSelected(option) }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = option == selectedOption,
                    onClick = { if (!isAnswered) onOptionSelected(option) },
                    enabled = !isAnswered
                )
                Text(
                    text = option,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )

                if (isAnswered && option == correctAnswer) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Respuesta correcta",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(20.dp)
                    )
                } else if (isAnswered && option == selectedOption && option != correctAnswer) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Respuesta incorrecta",
                        tint = Color(0xFFF44336),
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Componente para preguntas de verdadero/falso
 */
@Composable
fun TrueFalseQuestion(
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    isAnswered: Boolean,
    correctAnswer: String
) {
    val options = listOf("Verdadero", "Falso")

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when {
                            isAnswered && option == correctAnswer -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                            isAnswered && option == selectedOption && option != correctAnswer -> Color(0xFFF44336).copy(alpha = 0.1f)
                            option == selectedOption -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = when {
                            isAnswered && option == correctAnswer -> Color(0xFF4CAF50)
                            isAnswered && option == selectedOption && option != correctAnswer -> Color(0xFFF44336)
                            option == selectedOption -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable(enabled = !isAnswered) { onOptionSelected(option) }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = option == selectedOption,
                    onClick = { if (!isAnswered) onOptionSelected(option) },
                    enabled = !isAnswered
                )
                Text(
                    text = option,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )

                if (isAnswered && option == correctAnswer) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Respuesta correcta",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(20.dp)
                    )
                } else if (isAnswered && option == selectedOption && option != correctAnswer) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Respuesta incorrecta",
                        tint = Color(0xFFF44336),
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Componente para preguntas de texto (respuesta corta o ensayo)
 */
@Composable
fun TextQuestion(
    answer: String,
    onAnswerChanged: (String) -> Unit,
    isMultiline: Boolean,
    isAnswered: Boolean
) {
    OutlinedTextField(
        value = answer,
        onValueChange = { if (!isAnswered) onAnswerChanged(it) },
        label = { Text(if (isMultiline) "Escribe tu respuesta" else "Respuesta corta") },
        modifier = Modifier.fillMaxWidth(),
        enabled = !isAnswered,
        minLines = if (isMultiline) 4 else 1,
        maxLines = if (isMultiline) 8 else 1,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null
            )
        }
    )
}

/**
 * Componente que muestra el progreso de las actividades
 */
@Composable
fun ActivityProgressCard(
    progress: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            Text(
                text = "Progreso de actividades",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            CircularProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier.size(100.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeWidth = 8.dp
            )

            Text(
                text = "$progress%",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )

            Text(
                text = getProgressMessage(progress),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
            }
        }
    }
}

/**
 * Obtiene un mensaje motivacional según el progreso
 */
private fun getProgressMessage(progress: Int): String {
    return when {
        progress == 0 -> "¡Comienza a responder las actividades!"
        progress < 25 -> "¡Buen comienzo! Sigue adelante."
        progress < 50 -> "¡Vas por buen camino! Continúa así."
        progress < 75 -> "¡Excelente progreso! Ya casi llegas."
        progress < 100 -> "¡Casi lo logras! Solo un poco más."
        else -> "¡Felicidades! Has completado todas las actividades."
    }
}
