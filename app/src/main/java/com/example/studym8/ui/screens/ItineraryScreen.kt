package com.example.studym8.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studym8.data.model.StudyPlan
import com.example.studym8.ui.components.AiResponseView
import com.example.studym8.ui.components.StudyMateTopBar
import com.example.studym8.ui.viewmodel.AuthViewModel
import com.example.studym8.ui.viewmodel.StudyPlanViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import androidx.compose.ui.geometry.Offset

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ItineraryScreen(
    onNavigateToStudyPlanDetail: (String) -> Unit,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    studyPlanViewModel: StudyPlanViewModel = viewModel()
) {
    // Estados del formulario
    var subject by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf<Date?>(null) }
    var endDate by remember { mutableStateOf<Date?>(null) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    
    // Estados para los pickers
    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()
    val startTimePickerState = rememberTimePickerState()
    val endTimePickerState = rememberTimePickerState()
    
    // Estados de ViewModel
    val currentUser by authViewModel.currentUser.collectAsState(initial = null)
    val isLoading by studyPlanViewModel.isLoading.collectAsState()
    val error by studyPlanViewModel.error.collectAsState()
    val aiResponse by studyPlanViewModel.aiResponse.collectAsState()
    var showAiResponse by remember { mutableStateOf(false) }
    
    // Animación para el botón de IA
    val rotation by animateFloatAsState(
        targetValue = if (isLoading) 360f else 0f,
        label = "RotationAnimation"
    )
    
    // Contexto y scope
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Formatters
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale("es", "ES"))
    val timeFormatter = SimpleDateFormat("HH:mm", Locale("es", "ES"))
    
    // Animación para el botón de IA
    val infiniteTransition = rememberInfiniteTransition(label = "aiButtonAnimation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scaleAnimation"
    )
    
    // Animaciones adicionales para elementos de la interfaz
    val contentScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "contentScaleAnimation"
    )
    
    // Colores y gradientes
    val primaryGradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f, 1000f)
    )
    
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
        )
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        // Elementos decorativos de fondo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
        )
        
        // Círculos decorativos
        Box(
            modifier = Modifier
                .size(150.dp)
                .offset((-50).dp, (-50).dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        )
        
        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.TopEnd)
                .offset((20).dp, (40).dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
        )
        
        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .scale(contentScale),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Encabezado
            Text(
                text = "Crear Plan de Estudio",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            // Descripción para el usuario
            Text(
                text = "Completa la información para generar un plan personalizado",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Formulario de creación de plan de estudio
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(24.dp),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Icono y título de la sección
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Detalles del Plan",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    // Línea decorativa
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    )
                                )
                            )
                    )
                    
                    // Campo de asignatura con mejor diseño
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Asignatura o tema") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    // Sección de fechas con título
                    Column {
                        Text(
                            text = "Periodo de estudio",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        // Selector de fecha y hora de inicio
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showStartDatePicker = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Seleccionar fecha de inicio",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (startDate != null) 
                                        dateFormatter.format(startDate!!)
                                    else 
                                        "Fecha inicio",
                                    color = if (startDate != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            
                            OutlinedButton(
                                onClick = { showStartTimePicker = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "Seleccionar hora de inicio",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (startDate != null) 
                                        timeFormatter.format(startDate!!)
                                    else 
                                        "Hora inicio",
                                    color = if (startDate != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Selector de fecha y hora de fin
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showEndDatePicker = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Seleccionar fecha de fin",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (endDate != null) 
                                        dateFormatter.format(endDate!!)
                                    else 
                                        "Fecha fin",
                                    color = if (endDate != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            
                            OutlinedButton(
                                onClick = { showEndTimePicker = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "Seleccionar hora de fin",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (endDate != null) 
                                        timeFormatter.format(endDate!!)
                                    else 
                                        "Hora fin",
                                    color = if (endDate != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Botón para generar con IA mejorado
                    Button(
                        onClick = {
                            if (subject.isNotBlank() && currentUser != null) {
                                if (startDate != null && endDate != null) {
                                    studyPlanViewModel.generateAIStudyPlan(subject, startDate!!, endDate!!)
                                } else {
                                    // Usar fechas predeterminadas
                                    val now = Calendar.getInstance()
                                    val start = now.time
                                    now.add(Calendar.HOUR, 2)
                                    val end = now.time
                                    studyPlanViewModel.generateAIStudyPlan(subject, start, end)
                                }
                                showAiResponse = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                            .height(54.dp)
                            .scale(if (!isLoading) scale else 1f)
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(50),
                                spotColor = MaterialTheme.colorScheme.primary
                            ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary
                        ),
                        shape = RoundedCornerShape(50)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onTertiary,
                                modifier = Modifier
                                    .size(24.dp)
                                    .rotate(rotation)
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Generar con IA",
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Generar Plan de Estudio con IA",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
            
            // Información sobre el uso de IA
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Sugerencias Inteligentes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Nuestra IA analizará el tema y creará un plan de estudio personalizado adaptado a tus necesidades.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            
            // Mostrar respuesta de IA si está disponible
            if (showAiResponse && aiResponse != null) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(24.dp),
                            ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Plan de Estudio Generado",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            IconButton(
                                onClick = { showAiResponse = false },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Línea decorativa
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        )
                                    )
                                )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Mostrar contenido de IA con la vista mejorada
                        AiResponseView(
                            showResponse = aiResponse != null,
                            aiResponse = aiResponse,
                            onCloseClick = {},
                            formatAiResponse = studyPlanViewModel::formatAIResponse,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Botón para guardar el plan generado por IA
                        Button(
                            onClick = {
                                if (currentUser != null && aiResponse != null) {
                                    // Usar las fechas seleccionadas o predeterminadas
                                    val startTime = startDate ?: Calendar.getInstance().time
                                    val calendar = Calendar.getInstance()
                                    calendar.time = startTime
                                    calendar.add(Calendar.HOUR, 2)
                                    val endTime = endDate ?: calendar.time
                                    
                                    // Crear el plan de estudio con IA
                                    studyPlanViewModel.createAIStudyPlan(
                                        subject = subject,
                                        startDateTime = startTime,
                                        endDateTime = endTime,
                                        aiResponse = aiResponse ?: "",
                                        onSuccess = { planId -> 
                                            onNavigateToStudyPlanDetail(planId)
                                        }, 
                                        onError = { /* Manejar error */ }
                                    )
                                    
                                    // Limpiar formulario y ocultar respuesta
                                    subject = ""
                                    startDate = null
                                    endDate = null
                                    showAiResponse = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .shadow(
                                    elevation = 6.dp,
                                    shape = RoundedCornerShape(50),
                                    spotColor = MaterialTheme.colorScheme.primary
                                ),
                            enabled = currentUser != null && aiResponse != null && !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(50)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Guardar Plan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
            
            // Mostrar mensaje de error si existe
            if (error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(12.dp),
                            spotColor = MaterialTheme.colorScheme.error
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 8.dp)
                        )
                        
                        Text(
                            text = "Error: $error",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // Espacio al final para que no se tape contenido
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // Indicador de carga
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .shadow(
                            elevation = 10.dp,
                            spotColor = MaterialTheme.colorScheme.primary,
                            ambientColor = MaterialTheme.colorScheme.secondary
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(64.dp)
                                .rotate(rotation)
                        )
                        
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier
                                .size(48.dp)
                                .rotate(-rotation * 0.7f),
                            strokeWidth = 2.dp
                        )
                        
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(24.dp)
                                .scale(scale)
                        )
                    }
                }
            }
        }
    }
    
    // Diálogos para seleccionar fecha y hora
    if (showStartDatePicker) {
        CalendarioPersonalizado(
            onDismissRequest = { showStartDatePicker = false },
            onDateSelected = { selectedDate ->
                // Preservar la hora del día actual si ya existe una fecha
                val calendar = Calendar.getInstance()
                if (startDate != null) {
                    calendar.time = startDate!!
                } else {
                    calendar.set(Calendar.HOUR_OF_DAY, 9)
                    calendar.set(Calendar.MINUTE, 0)
                }
                
                // Establecer la fecha seleccionada (año, mes, día)
                calendar.set(Calendar.YEAR, selectedDate.get(Calendar.YEAR))
                calendar.set(Calendar.MONTH, selectedDate.get(Calendar.MONTH))
                calendar.set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH))
                
                startDate = calendar.time
                showStartDatePicker = false
            }
        )
    }
    
    if (showEndDatePicker) {
        CalendarioPersonalizado(
            onDismissRequest = { showEndDatePicker = false },
            onDateSelected = { selectedDate ->
                // Preservar la hora del día actual si ya existe una fecha
                val calendar = Calendar.getInstance()
                if (endDate != null) {
                    calendar.time = endDate!!
                } else {
                    calendar.set(Calendar.HOUR_OF_DAY, 11)
                    calendar.set(Calendar.MINUTE, 0)
                }
                
                // Establecer la fecha seleccionada (año, mes, día)
                calendar.set(Calendar.YEAR, selectedDate.get(Calendar.YEAR))
                calendar.set(Calendar.MONTH, selectedDate.get(Calendar.MONTH))
                calendar.set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH))
                
                endDate = calendar.time
                showEndDatePicker = false
            }
        )
    }
    
    if (showStartTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showStartTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val calendar = Calendar.getInstance()
                    if (startDate != null) {
                        calendar.time = startDate!!
                    }
                    calendar.set(Calendar.HOUR_OF_DAY, startTimePickerState.hour)
                    calendar.set(Calendar.MINUTE, startTimePickerState.minute)
                    startDate = calendar.time
                    showStartTimePicker = false
                }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            TimePicker(state = startTimePickerState)
        }
    }
    
    if (showEndTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showEndTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val calendar = Calendar.getInstance()
                    if (endDate != null) {
                        calendar.time = endDate!!
                    }
                    calendar.set(Calendar.HOUR_OF_DAY, endTimePickerState.hour)
                    calendar.set(Calendar.MINUTE, endTimePickerState.minute)
                    endDate = calendar.time
                    showEndTimePicker = false
                }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndTimePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            TimePicker(state = endTimePickerState)
        }
    }
}

@Composable
fun CalendarioPersonalizado(
    onDismissRequest: () -> Unit,
    onDateSelected: (Calendar) -> Unit
) {
    val mesesEspanol = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )
    val diasSemanaEspanol = listOf("L", "M", "X", "J", "V", "S", "D")
    
    val calendarioActual = remember { Calendar.getInstance() }
    var mesSeleccionado by remember { mutableStateOf(calendarioActual.get(Calendar.MONTH)) }
    var anoSeleccionado by remember { mutableStateOf(calendarioActual.get(Calendar.YEAR)) }
    var diaSeleccionado by remember { mutableStateOf<Int?>(null) }
    
    // Obtener el primer día del mes y número de días en el mes
    val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, anoSeleccionado)
        set(Calendar.MONTH, mesSeleccionado)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val primerDiaSemana = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Ajuste para que lunes sea el primer día
    val numDiasEnMes = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    // Animaciones y efectos visuales
    val transitionScale = remember { androidx.compose.animation.core.Animatable(0.95f) }
    val mesTransition = remember { androidx.compose.animation.core.Animatable(1f) }
    val scope = rememberCoroutineScope()
    
    // Gradientes para efectos visuales
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)
        )
    )
    
    // Efecto inicial de entrada
    LaunchedEffect(Unit) {
        transitionScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(350, easing = FastOutSlowInEasing)
        )
    }
    
    // Efecto cuando cambia el mes
    LaunchedEffect(mesSeleccionado, anoSeleccionado) {
        mesTransition.snapTo(0.85f)
        mesTransition.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            )
        )
    }
    
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)  // Limitar el ancho al 95% de la pantalla
                .padding(8.dp)
                .shadow(
                    elevation = 8.dp,
                    spotColor = MaterialTheme.colorScheme.primary,
                    ambientColor = MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(16.dp)
                )
                .scale(transitionScale.value),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(gradientBrush)
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Cabecera con título y botones de navegación
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                if (mesSeleccionado == 0) {
                                    mesSeleccionado = 11
                                    anoSeleccionado--
                                } else {
                                    mesSeleccionado--
                                }
                                diaSeleccionado = null
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Mes anterior",
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(270f),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.scale(mesTransition.value)
                    ) {
                        Text(
                            text = "${mesesEspanol[mesSeleccionado]} $anoSeleccionado",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        // Indicador decorativo
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .width(50.dp)
                                .height(2.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                            Color.Transparent
                                        )
                                    ),
                                    RoundedCornerShape(8.dp)
                                )
                        )
                    }
                    
                    IconButton(
                        onClick = {
                            scope.launch {
                                if (mesSeleccionado == 11) {
                                    mesSeleccionado = 0
                                    anoSeleccionado++
                                } else {
                                    mesSeleccionado++
                                }
                                diaSeleccionado = null
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Mes siguiente",
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(90f),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Cabecera de días de la semana
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    diasSemanaEspanol.forEach { dia ->
                        Text(
                            text = dia,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Grilla de días del mes con efecto de escala
                Box(
                    modifier = Modifier
                        .scale(mesTransition.value)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val filas = (primerDiaSemana + numDiasEnMes + 6) / 7
                        
                        for (i in 0 until filas) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                for (j in 0 until 7) {
                                    // Calcular el índice del día en el mes
                                    val diaIndice = i * 7 + j - primerDiaSemana
                                    
                                    if (diaIndice >= 0 && diaIndice < numDiasEnMes) {
                                        val dia = diaIndice + 1
                                        val esHoy = esMismoDia(
                                            Calendar.getInstance(),
                                            Calendar.getInstance().apply {
                                                set(Calendar.YEAR, anoSeleccionado)
                                                set(Calendar.MONTH, mesSeleccionado)
                                                set(Calendar.DAY_OF_MONTH, dia)
                                            }
                                        )
                                        val estaSeleccionado = diaSeleccionado == dia
                                        
                                        // Botón de día con efectos visuales
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)  // Reducir tamaño para pantallas pequeñas
                                                .shadow(
                                                    elevation = if (estaSeleccionado) 4.dp else 0.dp,
                                                    shape = RoundedCornerShape(50),
                                                    spotColor = if (estaSeleccionado) 
                                                        MaterialTheme.colorScheme.secondary 
                                                    else 
                                                        Color.Transparent
                                                )
                                                .clip(RoundedCornerShape(50))
                                                .background(
                                                    when {
                                                        estaSeleccionado -> MaterialTheme.colorScheme.secondary
                                                        esHoy -> MaterialTheme.colorScheme.primary
                                                        else -> if ((i + j) % 2 == 0) 
                                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                                        else 
                                                            Color.Transparent
                                                    }
                                                )
                                                .clickable {
                                                    diaSeleccionado = dia
                                                    
                                                    // Crear el calendario seleccionado
                                                    val selectedCalendar = Calendar.getInstance()
                                                    selectedCalendar.set(Calendar.YEAR, anoSeleccionado)
                                                    selectedCalendar.set(Calendar.MONTH, mesSeleccionado)
                                                    selectedCalendar.set(Calendar.DAY_OF_MONTH, dia)
                                                    
                                                    // Efecto visual al seleccionar
                                                    scope.launch {
                                                        transitionScale.animateTo(
                                                            targetValue = 0.96f,
                                                            animationSpec = tween(150)
                                                        )
                                                        onDateSelected(selectedCalendar)
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "$dia",
                                                color = when {
                                                    estaSeleccionado -> MaterialTheme.colorScheme.onSecondary
                                                    esHoy -> MaterialTheme.colorScheme.onPrimary
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = when {
                                                    estaSeleccionado || esHoy -> FontWeight.Bold
                                                    else -> FontWeight.Normal
                                                }
                                            )
                                        }
                                    } else {
                                        // Espacio vacío
                                        Spacer(modifier = Modifier.size(32.dp))
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Botones de acción
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = { onDismissRequest() },
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancelar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Cancelar",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    
                    TextButton(
                        onClick = {
                            val hoy = Calendar.getInstance()
                            val selectedCalendar = Calendar.getInstance()
                            
                            // Seleccionar la fecha de hoy
                            diaSeleccionado = hoy.get(Calendar.DAY_OF_MONTH)
                            mesSeleccionado = hoy.get(Calendar.MONTH)
                            anoSeleccionado = hoy.get(Calendar.YEAR)
                            
                            // Pasar el calendario seleccionado
                            onDateSelected(selectedCalendar)
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Hoy",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Hoy",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

// Función para determinar si dos fechas son el mismo día
fun esMismoDia(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
           cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    // Animación de entrada
    val transitionScale = remember { androidx.compose.animation.core.Animatable(0.95f) }
    
    LaunchedEffect(Unit) {
        transitionScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        )
    }
    
    // Gradiente de fondo sutil
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.02f),
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.02f)
        )
    )
    
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth(0.95f)  // Limitar el ancho al 95% de la pantalla
                .padding(8.dp)
                .shadow(
                    elevation = 8.dp,
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                )
                .scale(transitionScale.value)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(gradientBrush)
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Seleccionar hora",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                // Añadir decoración
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Contenedor para el TimePicker que permite hacer scroll si es necesario
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)  // Limitar la altura máxima
                        .clip(RoundedCornerShape(8.dp))
                        .verticalScroll(rememberScrollState())
                ) {
                    content()
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Añadir decoración
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                
                // Botones de acción
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Convertir dismiss a un TextButton personalizado
                    TextButton(
                        onClick = { onDismissRequest() },
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancelar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Cancelar",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    
                    // Botón de confirmar personalizado
                    TextButton(
                        onClick = { /* Se maneja en el confirmButton */ },
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Confirmar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Confirmar",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium
                        )
                        
                        // Componente invisible que captura el clic del confirmButton original
                        Box(modifier = Modifier.size(0.dp)) {
                            confirmButton()
                        }
                    }
                }
            }
        }
    }
} 