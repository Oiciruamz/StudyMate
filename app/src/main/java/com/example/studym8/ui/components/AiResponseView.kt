package com.example.studym8.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studym8.data.model.ResponseSection
import com.example.studym8.data.model.ResponseSectionType

/**
 * Un componente simplificado para mostrar el contenido generado por IA.
 */
@Composable
fun AiResponseView(
    content: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Formatear el texto para que sea más legible
        val paragraphs = content.split("\n\n")
        
        paragraphs.forEach { paragraph ->
            if (paragraph.startsWith("#")) {
                // Es un título
                Text(
                    text = paragraph.substringAfter("#").trim(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else if (paragraph.startsWith("*") || paragraph.startsWith("-")) {
                // Es una lista
                val items = paragraph.split("\n")
                items.forEach { item ->
                    Text(
                        text = item.trim(),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                    )
                }
            } else {
                // Es un párrafo normal
                Text(
                    text = paragraph.trim(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun AiResponseView(
    showResponse: Boolean,
    aiResponse: String?,
    onCloseClick: () -> Unit,
    formatAiResponse: (String) -> List<ResponseSection>,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = showResponse && aiResponse != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp)
                ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "IA",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Plan generado por IA",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    IconButton(onClick = onCloseClick) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Cerrar",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Divider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f)
                )

                // Formatear y mostrar la respuesta de IA
                val formattedResponse = remember(aiResponse) {
                    aiResponse?.let { formatAiResponse(it) } ?: emptyList()
                }

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    formattedResponse.forEach { section ->
                        when (section.type) {
                            ResponseSectionType.TITLE -> {
                                Text(
                                    text = section.content,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            ResponseSectionType.SUBTITLE -> {
                                Text(
                                    text = section.content,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp)
                                )
                            }
                            ResponseSectionType.INFO_ITEM -> {
                                Row(
                                    modifier = Modifier.padding(vertical = 2.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "•",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(
                                        text = section.content,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                            ResponseSectionType.PARAGRAPH -> {
                                Text(
                                    text = section.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                                )
                            }
                            ResponseSectionType.BREAK -> {
                                Spacer(modifier = Modifier.height(8.dp))
                                Divider(
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .padding(vertical = 4.dp)
                                        .align(Alignment.CenterHorizontally),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
} 