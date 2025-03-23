package com.example.studym8.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyPlanDetailScreen(
    planId: String,
    authViewModel: AuthViewModel = viewModel(),
    studyPlanViewModel: StudyPlanViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    // Estados
    val currentUser by authViewModel.currentUser.collectAsState(initial = null)
    val selectedPlan by studyPlanViewModel.selectedPlan.collectAsState()
    val isLoading by studyPlanViewModel.isLoading.collectAsState()
    val error by studyPlanViewModel.error.collectAsState()
    
    // Cargar el plan cuando se muestra la pantalla
    LaunchedEffect(planId) {
        studyPlanViewModel.getStudyPlanById(planId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles del Plan") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                        modifier = Modifier.fillMaxWidth(),
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

                    Spacer(modifier = Modifier.height(24.dp))

                    // Detalles de tiempo
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Fecha y hora de inicio
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                val startDate = plan.startDateTime.toDate()
                                Text(
                                    text = SimpleDateFormat("EEEE, d 'de' MMMM 'a las' h:mm a", Locale("es", "ES"))
                                        .format(startDate),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Duración
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
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

                            Spacer(modifier = Modifier.height(8.dp))

                            // Estado de completado
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (plan.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
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
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Materia: ${plan.subject}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = plan.description,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Mostrar respuesta de la IA si existe
                    if (plan.aiGenerated && plan.aiResponse.isNotBlank()) {
                        Text(
                            text = "Plan de Estudio Detallado",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        AiResponseView(
                            content = plan.aiResponse
                        )
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