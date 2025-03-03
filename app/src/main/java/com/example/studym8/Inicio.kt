package com.example.studym8
import com.example.studym8.ItineraryViewModel
import com.example.studym8.StudyM8Application

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.abs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyMateApp() {
    var selectedItem by remember { mutableStateOf(0) }
    val navItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Itinerary,
        BottomNavItem.Stats
    )

    Scaffold(
        topBar = {
            TopBarUserInfo(
                name = "Mauricio Muñiz",
                email = "mauricio09aguirre@gmail.com"
            )
        },
        bottomBar = {
            NavigationBar {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedItem) {
                0 -> HomeScreen()
                1 -> ItineraryScreen()
                2 -> StatsScreen()
            }
        }
    }
}

/**
 * Modelo para la navegación inferior
 */
sealed class BottomNavItem(
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
) {
    object Home : BottomNavItem("home", Icons.Default.Home, "Inicio")
    object Itinerary : BottomNavItem("itinerary", Icons.Default.CalendarMonth, "Itinerario")
    object Stats : BottomNavItem("stats", Icons.Default.BarChart, "Estadísticas")
}

/**
 * TopBar con la información del usuario
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarUserInfo(name: String, email: String) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "StudyMate de $name",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = email,
                    fontSize = 14.sp
                )
            }
        }
    )
}

/**
 * Pantalla principal (Home) que muestra el mockup:
 * - Sección "Mis itinerarios"
 * - Sección "Actividades Pendientes"
 */
@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Mis itinerarios",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Ejemplo de dos tarjetas de itinerarios
        ItineraryCard(
            title = "Programación Web",
            time = "Hoy a las 12:00 PM",
            chipText = "Estudios",
            chipColor = Color(0xFFB3E5FC) // Azul claro
        )
        Spacer(modifier = Modifier.height(8.dp))
        ItineraryCard(
            title = "Dieta Balanceada",
            time = "Hoy a las 01:00 PM",
            chipText = "Hábitos",
            chipColor = Color(0xFFC8E6C9) // Verde claro
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Actividades Pendientes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Fechas en forma de "chips"
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            DateChip("Feb 20", "Lun")
            DateChip("Feb 21", "Mar")
            DateChip("Feb 22", "Mié")
            DateChip("Feb 23", "Jue", selected = true)
            DateChip("Feb 24", "Vie")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filtros de tareas
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip("Todas", selected = true)
            FilterChip("To do", selected = false)
            FilterChip("En Progreso", selected = false)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tareas
        ActivityCard(
            title = "Centrar un DIV",
            project = "Itinerario Programación Web",
            status = "En Progreso"
        )
        Spacer(modifier = Modifier.height(8.dp))
        ActivityCard(
            title = "Crear un Header",
            project = "Itinerario Programación Web",
            status = "En Progreso"
        )
    }
}

/**
 * Tarjeta individual para un itinerario
 */
@Composable
fun ItineraryCard(
    title: String,
    time: String,
    chipText: String,
    chipColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = time, fontSize = 14.sp, color = Color.Gray)
            }
            Box(
                modifier = Modifier
                    .background(chipColor, shape = MaterialTheme.shapes.medium)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(text = chipText, fontSize = 14.sp)
            }
        }
    }
}

/**
 * Chip para fechas
 */
@Composable
fun DateChip(date: String, day: String, selected: Boolean = false) {
    val backgroundColor = if (selected) MaterialTheme.colorScheme.primary else Color(0xFFF5F5F5)
    val contentColor = if (selected) Color.White else Color.Black

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(48.dp)
            .background(backgroundColor, shape = MaterialTheme.shapes.medium)
            .padding(vertical = 4.dp)
    ) {
        Text(text = date, color = contentColor, fontSize = 12.sp)
        Text(text = day, color = contentColor, fontSize = 12.sp)
    }
}

/**
 * Chip para filtros
 */
@Composable
fun FilterChip(text: String, selected: Boolean) {
    val backgroundColor = if (selected) MaterialTheme.colorScheme.primary else Color(0xFFF5F5F5)
    val contentColor = if (selected) Color.White else Color.Black

    Box(
        modifier = Modifier
            .background(backgroundColor, shape = MaterialTheme.shapes.medium)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = text, color = contentColor, fontSize = 14.sp)
    }
}

/**
 * Tarjeta para una actividad pendiente
 */
@Composable
fun ActivityCard(
    title: String,
    project: String,
    status: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = project, fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))

            // Etiqueta de estado
            Box(
                modifier = Modifier
                    .background(Color(0xFFFFF9C4), shape = MaterialTheme.shapes.medium)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(text = status, fontSize = 14.sp)
            }
        }
    }
}

/**
 * Pantalla de Itinerario (placeholder)
 */


// Tipos para formatear la respuesta de IA
enum class ResponseSectionType {
    TITLE,
    SUBTITLE,
    INFO_ITEM,
    PARAGRAPH,
    BREAK
}

data class ResponseSection(
    val type: ResponseSectionType,
    val content: String
)

// Función para formatear la respuesta de IA
fun formatAIResponse(response: String): List<ResponseSection> {
    val sections = mutableListOf<ResponseSection>()

    // Si la respuesta está vacía, devolver lista vacía
    if (response.isBlank()) {
        return sections
    }

    // Dividir por líneas
    val lines = response.split("\n")

    // Procesar líneas
    var currentSection = ""
    var inList = false

    for (line in lines) {
        val trimmedLine = line.trim()

        // Detectar títulos y subtítulos
        when {
            trimmedLine.startsWith("# ") -> {
                // Agregar sección anterior si existe
                if (currentSection.isNotBlank()) {
                    sections.add(ResponseSection(ResponseSectionType.PARAGRAPH, currentSection))
                    currentSection = ""
                }
                sections.add(ResponseSection(ResponseSectionType.TITLE, trimmedLine.substring(2)))
            }
            trimmedLine.startsWith("## ") -> {
                // Agregar sección anterior si existe
                if (currentSection.isNotBlank()) {
                    sections.add(ResponseSection(ResponseSectionType.PARAGRAPH, currentSection))
                    currentSection = ""
                }
                sections.add(ResponseSection(ResponseSectionType.SUBTITLE, trimmedLine.substring(3)))
            }
            trimmedLine.startsWith("**") && trimmedLine.endsWith("**") -> {
                // Agregar sección anterior si existe
                if (currentSection.isNotBlank()) {
                    sections.add(ResponseSection(ResponseSectionType.PARAGRAPH, currentSection))
                    currentSection = ""
                }
                val content = trimmedLine.substring(2, trimmedLine.length - 2)
                sections.add(ResponseSection(ResponseSectionType.SUBTITLE, content))
            }
            trimmedLine.startsWith("- ") || trimmedLine.startsWith("* ") -> {
                // Si no estábamos en una lista, agregar la sección anterior
                if (!inList && currentSection.isNotBlank()) {
                    sections.add(ResponseSection(ResponseSectionType.PARAGRAPH, currentSection))
                    currentSection = ""
                }
                inList = true

                // Agregar el ítem de la lista
                val content = trimmedLine.substring(2)
                sections.add(ResponseSection(ResponseSectionType.INFO_ITEM, content))
            }
            trimmedLine.isEmpty() -> {
                // Agregar la sección actual si existe
                if (currentSection.isNotBlank()) {
                    sections.add(ResponseSection(ResponseSectionType.PARAGRAPH, currentSection))
                    currentSection = ""
                }

                // Salir de la lista si estábamos en una
                if (inList) {
                    inList = false
                    sections.add(ResponseSection(ResponseSectionType.BREAK, ""))
                }
            }
            else -> {
                // Si estábamos en una lista, salir de ella
                if (inList) {
                    inList = false
                    // Iniciar una nueva sección de párrafo
                    if (currentSection.isNotBlank()) {
                        currentSection += " "
                    }
                    currentSection += trimmedLine
                } else {
                    // Continuar o iniciar un párrafo
                    if (currentSection.isNotBlank()) {
                        currentSection += " "
                    }
                    currentSection += trimmedLine
                }
            }
        }
    }

    // Agregar la última sección si quedó algo pendiente
    if (currentSection.isNotBlank()) {
        sections.add(ResponseSection(ResponseSectionType.PARAGRAPH, currentSection))
    }

    return sections
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryScreen(viewModel: ItineraryViewModel = viewModel()) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Estados para los campos del formulario
    var subject by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(Calendar.getInstance()) }
    var endDate by remember { mutableStateOf(Calendar.getInstance().apply {
        add(Calendar.HOUR, 2) // 2 horas después por defecto
    })}

    // Estados para la generación con IA
    var isGeneratingAI by remember { mutableStateOf(false) }
    var aiResponse by remember { mutableStateOf<String?>(null) }
    var showAIResponse by remember { mutableStateOf(false) }

    // Estado para los diálogos
    var showDateTimePicker by remember { mutableStateOf(false) }
    var isStartDateTimeSelection by remember { mutableStateOf(true) }

    // Estado para mostrar mensajes
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateTimeFormatter = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault())

    // Colores
    val gradientColors = listOf(
        Color(0xFF4285F4), // Google Blue
        Color(0xFF0F9D58)  // Google Green
    )

    // Efecto de pulsación para el botón de IA
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
            LargeTopAppBar(
                title = {
                    Text(
                        "Planificador de Estudio",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = {
            if (showSnackbar) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { showSnackbar = false }) {
                            Text("OK", color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Text(text = snackbarMessage)
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Contenido principal
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Tarjeta para el tema de estudio
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "¿Qué quieres estudiar hoy?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Campo de texto para el tema
                        OutlinedTextField(
                            value = subject,
                            onValueChange = { subject = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Tema a estudiar") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.School,
                                    contentDescription = "Tema"
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tarjeta para la selección de tiempo
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "¿Cuándo vas a estudiar?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Selección de hora de inicio
                        TimeSelectionItem(
                            icon = Icons.Filled.PlayCircle,
                            label = "Comenzar",
                            dateTime = startDate.time,
                            formatter = dateTimeFormatter,
                            onClick = {
                                isStartDateTimeSelection = true
                                showDateTimePicker = true
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Selección de hora de fin
                        TimeSelectionItem(
                            icon = Icons.Filled.StopCircle,
                            label = "Terminar",
                            dateTime = endDate.time,
                            formatter = dateTimeFormatter,
                            onClick = {
                                isStartDateTimeSelection = false
                                showDateTimePicker = true
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Duración calculada
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Timer,
                                contentDescription = "Duración",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            val durationMs = endDate.timeInMillis - startDate.timeInMillis
                            val hours = durationMs / (1000 * 60 * 60)
                            val minutes = (durationMs % (1000 * 60 * 60)) / (1000 * 60)
                            Text(
                                text = "Duración: $hours h $minutes min",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón para generar el plan con IA
                Button(
                    onClick = {
                        if (subject.isBlank()) {
                            snackbarMessage = "Por favor ingresa un tema para estudiar"
                            showSnackbar = true
                            return@Button
                        }

                        if (endDate.time.before(startDate.time)) {
                            snackbarMessage = "La hora de finalización debe ser posterior a la hora de inicio"
                            showSnackbar = true
                            return@Button
                        }

                        scope.launch {
                            isGeneratingAI = true
                            aiResponse = null
                            showAIResponse = false

                            try {
                                val response = viewModel.generateAIStudyPlan(
                                    subject = subject,
                                    startDateTime = startDate.time,
                                    endDateTime = endDate.time
                                )

                                aiResponse = response
                                showAIResponse = true
                                isGeneratingAI = false

                                if (response != null) {
                                    // Crear el plan con la respuesta de la IA
                                    viewModel.createAIStudyPlan(
                                        subject = subject,
                                        startDateTime = startDate.time,
                                        endDateTime = endDate.time,
                                        aiResponse = response,
                                        onSuccess = { planId ->
                                            snackbarMessage = "¡Itinerario con IA creado con éxito!"
                                            showSnackbar = true
                                        },
                                        onError = { error ->
                                            snackbarMessage = "Error al guardar: ${error.message}"
                                            showSnackbar = true
                                        }
                                    )
                                } else {
                                    snackbarMessage = "No se pudo generar el plan con IA"
                                    showSnackbar = true
                                    isGeneratingAI = false
                                }
                            } catch (e: Exception) {
                                snackbarMessage = "Error: ${e.message}"
                                showSnackbar = true
                                isGeneratingAI = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(32.dp)
                        )
                        .scale(if (isGeneratingAI) 1f else scale),
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isGeneratingAI) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "GENERANDO PLAN...",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = "IA",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "GENERAR PLAN CON IA",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Mostrar respuesta de la IA cuando esté disponible
                AnimatedVisibility(
                    visible = showAIResponse && aiResponse != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
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
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
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

                                IconButton(onClick = { showAIResponse = false }) {
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

                            // Formatear la respuesta de la IA
                            val formattedResponse = remember(aiResponse) {
                                formatAIResponse(aiResponse ?: "")
                            }

                            // Mostrar respuesta formateada
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

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }

    // DateTimePicker personalizado
    if (showDateTimePicker) {
        CustomDateTimePickerDialog(
            initialCalendar = if (isStartDateTimeSelection) startDate else endDate,
            onDateTimeSelected = { selectedCalendar ->
                if (isStartDateTimeSelection) {
                    startDate = selectedCalendar
                } else {
                    endDate = selectedCalendar
                }
                showDateTimePicker = false
            },
            onDismiss = { showDateTimePicker = false }
        )
    }
}

@Composable
fun TimeSelectionItem(
    icon: ImageVector,
    label: String,
    dateTime: Date,
    formatter: SimpleDateFormat,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = formatter.format(dateTime),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = "Seleccionar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CustomDateTimePickerDialog(
    initialCalendar: Calendar,
    onDateTimeSelected: (Calendar) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = remember { initialCalendar.clone() as Calendar }
    var showDatePicker by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (showDatePicker) "Selecciona fecha" else "Selecciona hora",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Alternar entre selectores de fecha y hora
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TabButton(
                        icon = Icons.Filled.DateRange,
                        text = "Fecha",
                        selected = showDatePicker,
                        onClick = { showDatePicker = true }
                    )

                    TabButton(
                        icon = Icons.Filled.Schedule,
                        text = "Hora",
                        selected = !showDatePicker,
                        onClick = { showDatePicker = false }
                    )
                }

                // Contenido del selector
                if (showDatePicker) {
                    // Selector de fecha personalizado
                    DatePickerContent(
                        calendar = calendar,
                        onDateSelected = { year, month, day ->
                            calendar.set(Calendar.YEAR, year)
                            calendar.set(Calendar.MONTH, month)
                            calendar.set(Calendar.DAY_OF_MONTH, day)
                        }
                    )
                } else {
                    // Selector de hora personalizado
                    TimePickerContent(
                        calendar = calendar,
                        onTimeSelected = { hour, minute ->
                            calendar.set(Calendar.HOUR_OF_DAY, hour)
                            calendar.set(Calendar.MINUTE, minute)
                        }
                    )
                }

                // Botones de acción
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            onDateTimeSelected(calendar)
                        }
                    ) {
                        Text("Aceptar")
                    }
                }
            }
        }
    }
}

@Composable
fun TabButton(
    icon: ImageVector,
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .clickable(onClick = onClick),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun DatePickerContent(
    calendar: Calendar,
    onDateSelected: (year: Int, month: Int, day: Int) -> Unit
) {
    var selectedYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var selectedDay by remember { mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }

    // Actualizar calendario cuando cambia la selección
    LaunchedEffect(selectedYear, selectedMonth, selectedDay) {
        onDateSelected(selectedYear, selectedMonth, selectedDay)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Selector de mes y año
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (selectedMonth == 0) {
                    selectedMonth = 11
                    selectedYear--
                } else {
                    selectedMonth--
                }
            }) {
                Icon(Icons.Filled.NavigateBefore, "Mes anterior")
            }

            Text(
                text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(
                    Calendar.getInstance().apply {
                        set(Calendar.YEAR, selectedYear)
                        set(Calendar.MONTH, selectedMonth)
                        set(Calendar.DAY_OF_MONTH, 1)
                    }.time
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = {
                if (selectedMonth == 11) {
                    selectedMonth = 0
                    selectedYear++
                } else {
                    selectedMonth++
                }
            }) {
                Icon(Icons.Filled.NavigateNext, "Mes siguiente")
            }
        }

        // Días de la semana
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val daysOfWeek = listOf("Lu", "Ma", "Mi", "Ju", "Vi", "Sa", "Do")
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        // Días del mes
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedYear)
            set(Calendar.MONTH, selectedMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }

        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val today = Calendar.getInstance()

        // Ajustar para que la semana comience en lunes (1 = domingo, 2 = lunes, etc.)
        val offset = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - 2

        Column(modifier = Modifier.fillMaxWidth()) {
            var dayCount = 1
            for (i in 0 until 6) { // 6 semanas como máximo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (j in 0 until 7) {
                        if ((i == 0 && j < offset) || dayCount > daysInMonth) {
                            // Espacio vacío
                            Box(modifier = Modifier.size(40.dp)) {}
                        } else {
                            val day = dayCount
                            val isSelected = day == selectedDay
                            val isToday = today.get(Calendar.YEAR) == selectedYear &&
                                    today.get(Calendar.MONTH) == selectedMonth &&
                                    today.get(Calendar.DAY_OF_MONTH) == day

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable { selectedDay = day },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                        isToday -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.onSurface
                                    },
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                            dayCount++
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Salir si ya se han mostrado todos los días
                if (dayCount > daysInMonth) break
            }
        }
    }
}

@Composable
fun TimePickerContent(
    calendar: Calendar,
    onTimeSelected: (hour: Int, minute: Int) -> Unit
) {
    var selectedHour by remember { mutableStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(calendar.get(Calendar.MINUTE)) }

    // Actualizar calendario cuando cambia la selección
    LaunchedEffect(selectedHour, selectedMinute) {
        onTimeSelected(selectedHour, selectedMinute)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mostrar hora seleccionada
        Text(
            text = String.format("%02d:%02d", selectedHour, selectedMinute),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Selector de horas
        Text(
            text = "Hora",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                selectedHour = if (selectedHour == 0) 23 else selectedHour - 1
            }) {
                Icon(Icons.Filled.Remove, "Disminuir hora")
            }

            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = String.format("%02d", selectedHour),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = {
                selectedHour = if (selectedHour == 23) 0 else selectedHour + 1
            }) {
                Icon(Icons.Filled.Add, "Aumentar hora")
            }
        }

        // Selector de minutos
        Text(
            text = "Minutos",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                selectedMinute = if (selectedMinute == 0) 55 else selectedMinute - 5
            }) {
                Icon(Icons.Filled.Remove, "Disminuir minutos")
            }

            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = String.format("%02d", selectedMinute),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = {
                selectedMinute = if (selectedMinute == 55) 0 else selectedMinute + 5
            }) {
                Icon(Icons.Filled.Add, "Aumentar minutos")
            }
        }
    }
}
/**
 * Pantalla de Estadísticas (placeholder)
 */
@Composable
fun StatsScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Pantalla de Estadísticas")
    }
}

/**
 * Tema personalizado (puedes ajustarlo a tu paleta de colores)
 */
@Composable
fun StudyMateTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(),
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

/**
 * Definición rápida de un colorScheme claro
 */
@Composable
fun lightColorSchemeCustom() = lightColorScheme(
    primary = Color(0xFF6200EE),   // Morado principal
    secondary = Color(0xFF03DAC6)  // Turquesa
    // Puedes agregar más colores personalizados si es necesario
)
/**
 * Tipografía y shapes por defecto
 */
val Typography = Typography()
val Shapes = Shapes()

@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    StudyMateTheme {
        StudyMateApp()
    }
}