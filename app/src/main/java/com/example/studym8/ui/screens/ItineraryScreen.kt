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
import androidx.compose.foundation.background
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
    Scaffold(
        topBar = {
            StudyMateTopBar(
                user = currentUser,
                title = "Planificador de Estudio",
                onLogoutClick = onLogout
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Título de la pantalla
                Text(
                    text = "Planificador de Estudio",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Formulario de creación de plan de estudio
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Campo de asignatura
                        OutlinedTextField(
                            value = subject,
                            onValueChange = { subject = it },
                            label = { Text("Asignatura o tema") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words
                            )
                        )
                        
                        // Selector de fecha y hora de inicio
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showStartDatePicker = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Seleccionar fecha de inicio"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (startDate != null) 
                                        dateFormatter.format(startDate!!)
                                    else 
                                        "Fecha inicio"
                                )
                            }
                            
                            OutlinedButton(
                                onClick = { showStartTimePicker = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "Seleccionar hora de inicio"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (startDate != null) 
                                        timeFormatter.format(startDate!!)
                                    else 
                                        "Hora inicio"
                                )
                            }
                        }
                        
                        // Selector de fecha y hora de fin
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showEndDatePicker = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Seleccionar fecha de fin"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (endDate != null) 
                                        dateFormatter.format(endDate!!)
                                    else 
                                        "Fecha fin"
                                )
                            }
                            
                            OutlinedButton(
                                onClick = { showEndTimePicker = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "Seleccionar hora de fin"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (endDate != null) 
                                        timeFormatter.format(endDate!!)
                                    else 
                                        "Hora fin"
                                )
                            }
                        }
                        
                        // Botones de acción
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // Botón para generar con IA
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
                                    .padding(horizontal = 24.dp)
                                    .scale(if (!isLoading) scale else 1f)
                                    .shadow(
                                        elevation = 6.dp,
                                        shape = RoundedCornerShape(50),
                                        spotColor = MaterialTheme.colorScheme.primary
                                    ),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                    contentColor = MaterialTheme.colorScheme.onTertiary
                                )
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
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Generar Plan de Estudio con IA")
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Mostrar respuesta de IA si está disponible
                if (showAiResponse && aiResponse != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(16.dp),
                                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Sugerencia generada por IA",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                
                                IconButton(onClick = { showAiResponse = false }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cerrar",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Mostrar contenido de IA con la vista mejorada
                            AiResponseView(
                                showResponse = aiResponse != null,
                                aiResponse = aiResponse,
                                onCloseClick = {},
                                formatAiResponse = studyPlanViewModel::formatAIResponse,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
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
                                    .padding(horizontal = 24.dp),
                                enabled = currentUser != null && aiResponse != null && !isLoading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Guardar Plan")
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
            DatePickerDialog(
                onDismissRequest = { showStartDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        startDatePickerState.selectedDateMillis?.let { millis ->
                            val calendar = Calendar.getInstance().apply {
                                timeInMillis = millis
                                // Asegurarse de que la fecha es exactamente la seleccionada
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                                
                                if (startDate != null) {
                                    val oldCal = Calendar.getInstance().apply { time = startDate!! }
                                    set(Calendar.HOUR_OF_DAY, oldCal.get(Calendar.HOUR_OF_DAY))
                                    set(Calendar.MINUTE, oldCal.get(Calendar.MINUTE))
                                } else {
                                    set(Calendar.HOUR_OF_DAY, 9)
                                    set(Calendar.MINUTE, 0)
                                }
                            }
                            startDate = calendar.time
                        }
                        showStartDatePicker = false
                    }) {
                        Text("Confirmar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showStartDatePicker = false }) {
                        Text("Cancelar")
                    }
                }
            ) {
                DatePicker(state = startDatePickerState)
            }
        }
        
        if (showEndDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showEndDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        endDatePickerState.selectedDateMillis?.let { millis ->
                            val calendar = Calendar.getInstance().apply {
                                timeInMillis = millis
                                // Asegurarse de que la fecha es exactamente la seleccionada
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                                
                                if (endDate != null) {
                                    val oldCal = Calendar.getInstance().apply { time = endDate!! }
                                    set(Calendar.HOUR_OF_DAY, oldCal.get(Calendar.HOUR_OF_DAY))
                                    set(Calendar.MINUTE, oldCal.get(Calendar.MINUTE))
                                } else {
                                    set(Calendar.HOUR_OF_DAY, 11)
                                    set(Calendar.MINUTE, 0)
                                }
                            }
                            endDate = calendar.time
                        }
                        showEndDatePicker = false
                    }) {
                        Text("Confirmar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEndDatePicker = false }) {
                        Text("Cancelar")
                    }
                }
            ) {
                DatePicker(state = endDatePickerState)
            }
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
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Seleccionar hora",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                content()
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    dismissButton()
                    Spacer(modifier = Modifier.width(8.dp))
                    confirmButton()
                }
            }
        }
    }
} 