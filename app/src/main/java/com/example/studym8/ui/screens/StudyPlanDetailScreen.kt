package com.example.studym8.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studym8.data.model.StudyPlan
import com.example.studym8.ui.components.AiResponseView
import com.example.studym8.ui.viewmodel.AuthViewModel
import com.example.studym8.ui.viewmodel.StudyPlanViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.withStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyPlanDetailScreen(
    planId: String,
    authViewModel: AuthViewModel = viewModel(),
    studyPlanViewModel: StudyPlanViewModel = viewModel(),
    onBackClick: () -> Unit,
    onChatClick: (String) -> Unit = {}
) {
    // Estados
    val currentUser by authViewModel.currentUser.collectAsState(initial = null)
    val selectedPlan by studyPlanViewModel.selectedPlan.collectAsState()
    val isLoading by studyPlanViewModel.isLoading.collectAsState()
    val error by studyPlanViewModel.error.collectAsState()
    var showFullDescription by remember { mutableStateOf(false) }
    
    // Cargar el plan cuando se muestra la pantalla
    LaunchedEffect(planId) {
        studyPlanViewModel.getStudyPlanById(planId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Detalles del Plan",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    // Botón para abrir el chat
                    IconButton(onClick = { onChatClick(planId) }) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "Chat",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isLoading && selectedPlan == null) {
                // Mostrar indicador de carga
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (selectedPlan != null) {
                // Mostrar detalles del plan
                val plan = selectedPlan!!
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Encabezado con título y chip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = plan.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // Crear el chip basado en el estado del plan
                        val chipText = when {
                            plan.isCompleted -> "Completado"
                            plan.aiGenerated -> "Generado por IA"
                            else -> "En progreso"
                        }
                        
                        val chipColor = when {
                            plan.isCompleted -> Color(0xFF4CAF50) // Verde
                            plan.aiGenerated -> Color(0xFF2196F3) // Azul
                            else -> Color(0xFFFFA000) // Ámbar
                        }

                        Box(
                            modifier = Modifier
                                .background(chipColor, shape = RoundedCornerShape(16.dp))
                                .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = chipText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Detalles de tiempo
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Fecha y hora de inicio
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                                val startDate = plan.startDateTime.toDate()
                                Text(
                                    text = SimpleDateFormat("EEEE, d 'de' MMMM 'a las' h:mm a", Locale("es", "ES"))
                                        .format(startDate),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Duración
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                                val hours = plan.totalDuration / 60
                                val minutes = plan.totalDuration % 60
                                Text(
                                    text = if (hours > 0) {
                                        "$hours hora${if (hours > 1) "s" else ""} y $minutes minuto${if (minutes > 1) "s" else ""}"
                                    } else {
                                        "$minutes minuto${if (minutes > 1) "s" else ""}"
                                    },
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Estado de completado
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (plan.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                                Text(
                                    text = if (plan.isCompleted) "Completado" else "Pendiente (${plan.completionRate}%)",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Descripción o materia
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                                Text(
                                    text = "Materia: ${plan.subject}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Divider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            )

                            // Procesar la descripción para convertir texto entre asteriscos en negrita
                            val formattedDescription = formatDescriptionText(plan.description)
                            
                            // Si la descripción es larga, mostrar un botón para expandir
                            if (plan.description.length > 200 && !showFullDescription) {
                                Text(
                                    text = formattedDescription.subSequence(0, minOf(200, formattedDescription.length)),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                                
                                Text(
                                    text = "Mostrar más",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .clickable { showFullDescription = true }
                                )
                            } else {
                                Text(
                                    text = formattedDescription,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                                
                                if (plan.description.length > 200 && showFullDescription) {
                                    Text(
                                        text = "Mostrar menos",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .padding(top = 8.dp)
                                            .clickable { showFullDescription = false }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                    
                    // Secciones del Plan de Estudio con Checkboxes
                    if (plan.sessions.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "Sesiones de estudio",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                
                                plan.sessions.forEachIndexed { index, session ->
                                    if (index > 0) {
                                        Divider(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                        )
                                    }
                                    
                                    SessionItem(
                                        session = session,
                                        onSessionStatusChanged = { isCompleted ->
                                            studyPlanViewModel.updateSessionStatus(
                                                planId = plan.id,
                                                sessionId = session.id,
                                                isCompleted = isCompleted
                                            )
                                        }
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(28.dp))
                    }
                    // Si no hay sesiones pero el plan es generado por IA, mostrar botón para generar sesiones
                    else if (plan.aiGenerated && plan.aiResponse.isNotBlank()) {
                        // Como ahora las sesiones se generan automáticamente, mostramos un indicador de carga
                        // mientras se procesan en lugar del botón
                        if (isLoading) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(20.dp)
                                        .fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Procesando sesiones...",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                    
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(40.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        } else {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Extraer sesiones del plan generado por IA",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                    
                                    androidx.compose.material3.Button(
                                        onClick = {
                                            studyPlanViewModel.generateSessionsFromAIResponse(plan.id)
                                        },
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                        Text("Generar sesiones")
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(28.dp))
                    }
                    
                    // Mostrar barra de progreso animada
                    if (!plan.isCompleted) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Progreso de estudio",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                
                                val animatedProgress by animateFloatAsState(
                                    targetValue = plan.completionRate / 100f,
                                    label = "progressAnimation"
                                )
                                
                                CircularProgressIndicator(
                                    progress = animatedProgress,
                                    modifier = Modifier.size(100.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    strokeWidth = 8.dp
                                )
                                
                                Text(
                                    text = "${plan.completionRate}%",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 16.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(28.dp))
                    }

                    // Mostrar respuesta de la IA si existe
                    if (plan.aiGenerated && plan.aiResponse.isNotBlank()) {
                        AnimatedVisibility(
                            visible = false,
                            enter = fadeIn() + expandVertically()
                        ) {
                            Column {
                                Text(
                                    text = "Plan de Estudio Detallado",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .padding(vertical = 12.dp)
                                        .fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp)),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    AiResponseView(
                                        showResponse = true,
                                        aiResponse = plan.aiResponse,
                                        onCloseClick = {},
                                        formatAiResponse = studyPlanViewModel::formatAIResponse,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (error != null) {
                // Mostrar error
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error ?: "Error desconocido",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Procesa el texto de la descripción para manejar formato
 * Convierte **texto** en texto con formato negrita
 */
@Composable
fun formatDescriptionText(description: String): AnnotatedString {
    return buildAnnotatedString {
        val boldRegex = """(\*\*)(.*?)(\*\*)""".toRegex()
        var lastMatchEnd = 0
        
        // Encontrar todas las coincidencias de patrón **texto**
        boldRegex.findAll(description).forEach { match ->
            // Añadir texto normal antes del texto en negrita
            append(description.substring(lastMatchEnd, match.range.first))
            
            // Añadir texto en negrita (sin los **)
            val boldText = match.groupValues[2]
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(boldText)
            }
            
            lastMatchEnd = match.range.last + 1
        }
        
        // Añadir cualquier texto restante después de la última coincidencia
        if (lastMatchEnd < description.length) {
            append(description.substring(lastMatchEnd))
        }
    }
}

/**
 * Componente para mostrar una sesión de estudio con un checkbox
 */
@Composable
fun SessionItem(
    session: com.example.studym8.data.model.StudySession,
    onSessionStatusChanged: (Boolean) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            androidx.compose.material3.Checkbox(
                checked = session.isCompleted,
                onCheckedChange = { isChecked ->
                    onSessionStatusChanged(isChecked)
                },
                colors = androidx.compose.material3.CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { isExpanded = !isExpanded }
            ) {
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (session.isCompleted) 
                        MaterialTheme.colorScheme.primary
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "${session.duration} minutos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            IconButton(onClick = { isExpanded = !isExpanded }) {
                Icon(
                    imageVector = if (isExpanded) 
                        androidx.compose.material.icons.Icons.Default.KeyboardArrowUp
                    else 
                        androidx.compose.material.icons.Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Contraer" else "Expandir",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        
        // Contenido expandido con notas
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 56.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
            ) {
                if (session.notes.isNotBlank()) {
                    Text(
                        text = formatDescriptionText(session.notes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
} 