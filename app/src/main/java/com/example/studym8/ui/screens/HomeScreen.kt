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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studym8.data.model.StudyPlan
import com.example.studym8.data.model.User
import com.example.studym8.ui.components.StudyPlanItem
import com.example.studym8.ui.viewmodel.AuthViewModel
import com.example.studym8.ui.viewmodel.StudyPlanViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    onNavigateToStudyPlanDetail: (String) -> Unit,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    studyPlanViewModel: StudyPlanViewModel = viewModel()
) {
    // Estado para el usuario actual, planes de estudio, carga y errores
    val currentUser by authViewModel.currentUser.collectAsState(initial = null)
    val studyPlans by studyPlanViewModel.studyPlans.collectAsState(initial = emptyList())
    val isLoading by studyPlanViewModel.isLoading.collectAsState()
    val error by studyPlanViewModel.error.collectAsState()
    
    // Cargar los planes de estudio cuando se muestra la pantalla
    LaunchedEffect(currentUser) {
        studyPlanViewModel.loadStudyPlans()
    }
    
    // Contenido principal
    Box(modifier = Modifier.fillMaxSize()) {
        // Mostrar un indicador de carga mientras se obtienen los datos
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.Center)
            )
        } else if (error != null) {
            // Mostrar mensaje de error si hay alguno
            Text(
                text = "Error: $error",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            )
        } else {
            // Contenido principal cuando los datos están cargados
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Sección de bienvenida
                item {
                    currentUser?.let { user ->
                        WelcomeSection(user)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // Fila de fichas con fechas
                item {
                    DateChipsRow()
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // Sección de "Mis planes de estudio"
                item {
                    Text(
                        text = "Mis planes de estudio",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                
                // Lista de planes de estudio
                if (studyPlans.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No tienes planes de estudio activos.\nCrea uno nuevo en la sección de Itinerarios.",
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(studyPlans) { plan ->
                        val startDate = plan.startDateTime.toDate()
                        val formattedTime = SimpleDateFormat("dd MMM, HH:mm", Locale("es", "ES")).format(startDate)
                        
                        StudyPlanItem(
                            studyPlan = plan,
                            formattedTime = formattedTime,
                            onClick = { onNavigateToStudyPlanDetail(plan.id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                // Sección de actividades pendientes
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Actividades pendientes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                
                // Actividades pendientes
                item {
                    if (studyPlans.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No tienes actividades pendientes.",
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // Mostrar algunas actividades de ejemplo basadas en los planes
                        studyPlans.take(2).forEach { plan ->
                            ActivityCard(
                                title = "Revisión: ${plan.subject}",
                                description = "Repasar material para ${plan.subject}",
                                date = plan.startDateTime.toDate(),
                                isUrgent = false
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                
                // Espaciado adicional al final
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun WelcomeSection(user: User) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar o icono de usuario
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Avatar",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(30.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Texto de bienvenida
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "¡Hola, ${user.displayName.orEmpty().ifBlank { "Estudiante" }}!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Bienvenido a tu espacio de estudio",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DateChipsRow() {
    // Obtener la fecha actual
    val calendar = Calendar.getInstance()
    val currentDate = calendar.time
    val dateFormatter = SimpleDateFormat("EEE\ndd", Locale("es", "ES"))
    
    // Crear una lista de fechas (hoy + 6 días siguientes)
    val dates = List(7) { index ->
        calendar.time.apply {
            calendar.add(Calendar.DAY_OF_YEAR, if (index > 0) 1 else 0)
        }
    }
    
    // Mostrar las fichas de fecha en una fila horizontal
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(dates) { date ->
            val isToday = android.text.format.DateFormat.format("dd", date) == 
                          android.text.format.DateFormat.format("dd", currentDate)
            
            DateChip(
                date = date,
                isSelected = isToday,
                dateFormatter = dateFormatter
            )
        }
    }
}

@Composable
fun DateChip(
    date: Date,
    isSelected: Boolean,
    dateFormatter: SimpleDateFormat
) {
    Card(
        modifier = Modifier
            .width(60.dp)
            .height(70.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = dateFormatter.format(date),
                textAlign = TextAlign.Center,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.onPrimary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityCard(
    title: String,
    description: String,
    date: Date,
    isUrgent: Boolean
) {
    val dateFormatter = SimpleDateFormat("HH:mm", Locale("es", "ES"))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isUrgent) 
                MaterialTheme.colorScheme.errorContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de la actividad
            Icon(
                imageVector = if (isUrgent) Icons.Default.Warning else Icons.Default.Schedule,
                contentDescription = null,
                tint = if (isUrgent) 
                    MaterialTheme.colorScheme.error 
                else 
                    MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Contenido de texto
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isUrgent) 
                        MaterialTheme.colorScheme.onErrorContainer 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUrgent) 
                        MaterialTheme.colorScheme.onErrorContainer 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Hora: ${dateFormatter.format(date)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isUrgent) 
                        MaterialTheme.colorScheme.onErrorContainer 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Botón de completado
            IconButton(onClick = { /* Marcar como completado */ }) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Marcar como completado",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
} 