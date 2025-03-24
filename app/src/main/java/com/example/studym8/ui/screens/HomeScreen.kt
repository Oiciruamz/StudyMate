package com.example.studym8.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@OptIn(ExperimentalMaterial3Api::class)
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
    
    // Estados para pestañas y fecha seleccionada
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedDateIndex by remember { mutableIntStateOf(0) }
    
    // Obtener las fechas para el selector
    val dates = List(5) { index ->
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, index)
        calendar.time
    }
    
    // Cargar los planes de estudio cuando se muestra la pantalla
    LaunchedEffect(currentUser) {
        studyPlanViewModel.loadStudyPlans()
    }
    
    // Verificar si los planes han cargado pero están vacíos
    val hasLoadedButEmpty = !isLoading && studyPlans.isEmpty() && error == null
    
    // Para depuración - verificar si los planes están cargando correctamente
    LaunchedEffect(studyPlans) {
        if (studyPlans.isNotEmpty()) {
            println("StudyPlans cargados: ${studyPlans.size}")
            studyPlans.forEach { plan ->
                println("Plan: ${plan.title}, ID: ${plan.id}, Subject: ${plan.subject}")
            }
        } else if (!isLoading) {
            println("No hay planes de estudio cargados y no está cargando.")
        }
    }
    
    // Calcular el progreso total de los planes de estudio
    val totalProgress = if (studyPlans.isNotEmpty()) {
        studyPlans.sumOf { it.completionRate }.toFloat() / studyPlans.size
    } else {
        0f
    }
    
    // Animación del progreso
    val animatedProgress by animateFloatAsState(
        targetValue = totalProgress / 100f,
        animationSpec = tween(durationMillis = 1000),
        label = "progressAnimation"
    )
    
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Navegar a crear nuevo plan */ },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar plan de estudio"
                )
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp)
            ) {
                // Barra superior con avatar y notificaciones
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sección del usuario
                    currentUser?.let { user ->
                        UserProfileSection(user)
                    }
                    
                    // Icono de notificaciones
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = { /* Abrir notificaciones */ }) {
                            Icon(
                                imageVector = Icons.Rounded.Notifications,
                                contentDescription = "Notificaciones",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Tarjeta de progreso
                ProgressCard(
                    progress = animatedProgress,
                    progressText = "${totalProgress.toInt()}%",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Selector de fechas
                DateHeader(selectedDate = dates[selectedDateIndex])
                Spacer(modifier = Modifier.height(8.dp))
                DateSelector(
                    selectedIndex = selectedDateIndex,
                    onDateSelected = { index -> selectedDateIndex = index }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Pestañas de filtrado con tamaño reducido
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val tabsEs = listOf("Todos", "Por hacer", "En progreso", "Completados")
                    tabsEs.forEachIndexed { index, title ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { selectedTabIndex = index }
                                .background(
                                    if (selectedTabIndex == index)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        Color.Transparent
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 11.sp, // Texto más pequeño
                                color = if (selectedTabIndex == index)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (selectedTabIndex == index)
                                    FontWeight.Bold
                                else
                                    FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (isLoading) {
                    // Indicador de carga
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Cargando tus planes de estudio...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else if (error != null) {
                    // Mostrar error con mejor formato
                    FirestoreErrorMessage(
                        error = error!!,
                        onRetry = { studyPlanViewModel.loadStudyPlans() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(16.dp)
                    )
                } else {
                    // Lista de planes filtrados según la pestaña seleccionada
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val filteredPlans = when (selectedTabIndex) {
                            0 -> studyPlans // Todos
                            1 -> studyPlans.filter { !it.isCompleted && it.completionRate <= 0 } // Por hacer
                            2 -> studyPlans.filter { !it.isCompleted && it.completionRate > 0 } // En Progreso
                            3 -> studyPlans.filter { it.isCompleted } // Completados
                            else -> studyPlans
                        }
                        
                        // Filtrar por fecha seleccionada
                        val selectedDate = dates[selectedDateIndex]
                        val dateFilteredPlans = filteredPlans.filter { plan ->
                            val planDate = plan.startDateTime.toDate()
                            isSameDay(planDate, selectedDate)
                        }
                        
                        if (dateFilteredPlans.isEmpty() && hasLoadedButEmpty) {
                            item {
                                EmptyStateMessage(
                                    message = "No hay planes para este día.\nCrea un nuevo plan con el botón +",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        } else if (dateFilteredPlans.isEmpty()) {
                            item {
                                EmptyStateMessage(
                                    message = "No hay planes para este día",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        } else {
                            items(dateFilteredPlans) { plan ->
                                val startDate = plan.startDateTime.toDate()
                                val formattedTime = SimpleDateFormat("dd MMM, HH:mm", Locale("es", "ES")).format(startDate)
                                
                                StudyPlanItem(
                                    studyPlan = plan,
                                    formattedTime = formattedTime,
                                    onClick = { onNavigateToStudyPlanDetail(plan.id) }
                                )
                            }
                        }
                        
                        // Si no hay planes, agregar ejemplo de ProjectCard para mostrar el diseño
                        if (hasLoadedButEmpty) {
                            item {
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = "Ejemplo de cómo se verán tus planes:",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                ProjectCard(
                                    title = "Matemáticas Avanzadas",
                                    subject = "Ciencias Exactas",
                                    progress = 0.7f,
                                    color = MaterialTheme.colorScheme.primary,
                                    onClick = { },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                ProjectCard(
                                    title = "Literatura Universal",
                                    subject = "Humanidades",
                                    progress = 0.3f,
                                    color = Color(0xFFE91E63),
                                    onClick = { },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        
                        // Espacio adicional para evitar que el FAB tape contenido
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserProfileSection(user: User) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar o icono de usuario con borde
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (user.photoUrl.isNotEmpty()) {
                // Aquí iría la carga de imagen con Coil o similar
                // por ahora usamos un placeholder
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Texto de bienvenida
        Column {
            Text(
                text = "¡Hola!",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = user.displayName.takeIf { it.isNotEmpty() } ?: "Estudiante",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ProgressCard(
    progress: Float,
    progressText: String,
    modifier: Modifier = Modifier
) {
    // Animación de entrada para la tarjeta
    val cardAnimatedScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardScale"
    )
    
    // Animación para el progreso
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1500),
        label = "progressAnimation"
    )
    
    // Efecto gradiente para el fondo
    val gradientColors = listOf(
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
    )
    
    Box(
        modifier = modifier
            .scale(cardAnimatedScale)
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = gradientColors,
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                )
            )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "¡Tu tarea de hoy casi completada!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Continúa donde lo dejaste",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Botón de "Ver tarea" con sombra y efecto hover
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { /* Acción del botón */ }
                    ) {
                        Text(
                            text = "Ver tarea",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Indicador de progreso circular con texto y animación
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(100.dp)
                ) {
                    // Track background
                    CircularProgressIndicator(
                        progress = 1f,
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 8.dp,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                        strokeCap = StrokeCap.Round
                    )
                    
                    // Animated progress
                    CircularProgressIndicator(
                        progress = animatedProgress,
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 8.dp,
                        color = MaterialTheme.colorScheme.primary,
                        strokeCap = StrokeCap.Round
                    )
                    
                    // Animated text value
                    val animatedTextProgress = (animatedProgress * 100).toInt()
                    Text(
                        text = "$animatedTextProgress%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryHeader(
    title: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        // Indicador de cantidad
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ProjectCard(
    title: String,
    subject: String,
    progress: Float,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Icono de la categoría
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Categoría del proyecto
            Text(
                text = subject,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Título del proyecto
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Barra de progreso
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth(),
                color = color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun TaskGroupCard(
    title: String,
    count: Int,
    progress: Float,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de la categoría
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Información del grupo
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "$count Tareas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            // Indicador circular de progreso
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(50.dp)
            ) {
                CircularProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 4.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    color = color
                )
                
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun EmptyProjectCard(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(180.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun EmptyStateMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun DateHeader(selectedDate: Date) {
    // Formatear la fecha seleccionada
    val dateFormatter = SimpleDateFormat("EEEE, d MMMM", Locale("es", "ES"))
    
    Text(
        text = dateFormatter.format(selectedDate),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun DateSelector(
    selectedIndex: Int,
    onDateSelected: (Int) -> Unit
) {
    // Obtener los próximos 5 días
    val dates = List(5) { index ->
        val newCalendar = Calendar.getInstance()
        newCalendar.add(Calendar.DAY_OF_YEAR, index)
        Triple(
            newCalendar.get(Calendar.DAY_OF_MONTH),
            SimpleDateFormat("EEE", Locale("es", "ES")).format(newCalendar.time),
            index == selectedIndex // Usar el parámetro para determinar cuál está seleccionado
        )
    }
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        itemsIndexed(dates) { index, (day, dayName, _) ->
            // Calcular si este ítem está seleccionado basado en el índice
            val isSelected = index == selectedIndex
            
            // Animación al renderizar cada fecha
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(initialAlpha = 0.4f) + 
                    slideInVertically(
                        initialOffsetY = { it * (1 + index / 5) }, 
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
            ) {
                DateChip(
                    day = day.toString(), 
                    dayName = dayName, 
                    isSelected = isSelected,
                    onDateSelected = { onDateSelected(index) }
                )
            }
        }
    }
}

@Composable
fun DateChip(
    day: String,
    dayName: String,
    isSelected: Boolean,
    onDateSelected: () -> Unit
) {
    // Animación del escalado al seleccionar una fecha
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scaleAnimation"
    )
    
    // Usamos directamente los colores sin animación
    val backgroundColor = if (isSelected) 
        MaterialTheme.colorScheme.primary 
    else 
        MaterialTheme.colorScheme.surfaceVariant
    
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 0.dp
        ),
        modifier = Modifier
            .size(width = 60.dp, height = 80.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onDateSelected() }
            .scale(scale)
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = dayName.uppercase(),
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = day,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FirestoreErrorMessage(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No se pudieron cargar los planes de estudio",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (error.contains("index")) {
            Text(
                text = "Es necesario crear un índice en Firestore para esta consulta. " +
                      "Este es un error de configuración que debe ser resuelto por el administrador.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Reintentar")
        }
    }
}

// Función auxiliar para verificar si dos fechas son del mismo día
private fun isSameDay(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
           cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
}