package com.example.studym8.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studym8.data.NotificationManager
import com.example.studym8.data.NotificationType
import com.example.studym8.data.model.ResponseSection
import com.example.studym8.data.model.ResponseSectionType
import com.example.studym8.data.model.StudyPlan
import com.example.studym8.data.model.StudySession
import com.example.studym8.data.model.toResponseSections
import com.example.studym8.data.repository.StudyPlanRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class StudyPlanViewModel : ViewModel() {
    
    private val repository = StudyPlanRepository()
    private var notificationManager: NotificationManager? = null
    
    // StateFlow para la lista de planes de estudio
    private val _studyPlans = MutableStateFlow<List<StudyPlan>>(emptyList())
    val studyPlans: StateFlow<List<StudyPlan>> = _studyPlans
    
    // StateFlow para el plan de estudio seleccionado
    private val _selectedPlan = MutableStateFlow<StudyPlan?>(null)
    val selectedPlan: StateFlow<StudyPlan?> = _selectedPlan
    
    // StateFlow para manejar estados de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    // StateFlow para manejar errores
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    // StateFlow para la respuesta de IA
    private val _aiResponse = MutableStateFlow<String?>(null)
    val aiResponse: StateFlow<String?> = _aiResponse
    
    // StateFlow para controlar si la respuesta de IA debe mostrarse
    private val _showAIResponse = MutableStateFlow(false)
    val showAIResponse: StateFlow<Boolean> = _showAIResponse
    
    init {
        loadStudyPlans()
    }
    
    fun initializeNotificationManager(context: Context) {
        notificationManager = NotificationManager(context)
    }
    
    private fun checkStudyPlanStatus(studyPlan: StudyPlan) {
        val currentDate = Date()
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        
        // Verificar si el plan está vencido
        if (studyPlan.endDateTime.toDate().before(currentDate) && !studyPlan.isCompleted) {
            notificationManager?.showStudyPlanNotification(studyPlan, NotificationType.OVERDUE)
        }
        
        // Verificar si el plan está próximo a vencer (3 días antes)
        calendar.add(Calendar.DAY_OF_MONTH, 3)
        if (studyPlan.endDateTime.toDate().before(calendar.time) && 
            studyPlan.endDateTime.toDate().after(currentDate) && 
            !studyPlan.isCompleted) {
            notificationManager?.showStudyPlanNotification(studyPlan, NotificationType.UPCOMING)
        }
        
        // Verificar si hay actividades pendientes
        if (!studyPlan.isCompleted && studyPlan.completionRate < 100) {
            notificationManager?.showStudyPlanNotification(studyPlan, NotificationType.INCOMPLETE)
        }
    }
    
    fun loadStudyPlans() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val plans = repository.getStudyPlans()
                _studyPlans.value = plans
                
                // Verificar estados pero con un retraso entre verificaciones
                checkStudyPlansStatusWithDelay(plans)
            } catch (e: Exception) {
                _error.value = "Error al cargar los planes de estudio: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun checkStudyPlansStatusWithDelay(plans: List<StudyPlan>) {
        viewModelScope.launch {
            plans.forEachIndexed { index, plan ->
                // Agregar un retraso de 1 segundo entre cada verificación
                if (index > 0) {
                    kotlinx.coroutines.delay(1000)
                }
                checkStudyPlanStatus(plan)
            }
        }
    }
    
    fun getStudyPlanById(planId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val plan = repository.getStudyPlanById(planId)
                _selectedPlan.value = plan
                
                // Verificar si el plan es generado por IA, tiene respuesta AI pero no tiene sesiones
                if (plan != null && plan.aiGenerated && plan.aiResponse.isNotBlank() && plan.sessions.isEmpty()) {
                    // Generar sesiones automáticamente
                    val sessionsFromAI = parseSessionsFromAIResponse(plan.aiResponse)
                    
                    if (sessionsFromAI.isNotEmpty()) {
                        // Actualizar el plan con las sesiones generadas
                        val updatedPlan = plan.copy(sessions = sessionsFromAI)
                        
                        // Actualizar en el repositorio
                        val success = repository.updateStudyPlan(updatedPlan)
                        if (success) {
                            // Actualizar el plan seleccionado
                            _selectedPlan.value = updatedPlan
                        }
                    }
                }
                
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al obtener el plan de estudio: ${e.message}"
                _selectedPlan.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun createStudyPlan(
        title: String,
        subject: String,
        description: String,
        startDateTime: Date,
        endDateTime: Date,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                // Calcular duración en minutos
                val durationMs = endDateTime.time - startDateTime.time
                val durationMinutes = (durationMs / (1000 * 60)).toInt()
                
                // Crear el objeto StudyPlan
                val studyPlan = StudyPlan(
                    title = title,
                    subject = subject,
                    description = description,
                    startDateTime = Timestamp(startDateTime),
                    endDateTime = Timestamp(endDateTime),
                    totalDuration = durationMinutes,
                    tags = listOf(subject.lowercase())
                )
                
                // Guardar en el repositorio
                val planId = repository.createStudyPlan(studyPlan)
                
                if (planId != null) {
                    // Recargar la lista de planes
                    loadStudyPlans()
                    onSuccess(planId)
                } else {
                    onError("No se pudo crear el plan de estudio")
                }
            } catch (e: Exception) {
                onError("Error al crear el plan de estudio: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun createAIStudyPlan(
        subject: String,
        startDateTime: Date,
        endDateTime: Date,
        aiResponse: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                // Calcular duración en minutos
                val durationMs = endDateTime.time - startDateTime.time
                val durationMinutes = (durationMs / (1000 * 60)).toInt()
                
                // Crear el objeto StudyPlan
                val studyPlan = StudyPlan(
                    title = "Estudio de $subject",
                    subject = subject,
                    description = "Plan de estudio generado con IA",
                    startDateTime = Timestamp(startDateTime),
                    endDateTime = Timestamp(endDateTime),
                    totalDuration = durationMinutes,
                    aiGenerated = true,
                    tags = listOf(subject.lowercase()),
                    color = "#4285F4",  // Color azul de Google
                    aiResponse = aiResponse
                )
                
                // Guardar en el repositorio
                val planId = repository.createStudyPlan(studyPlan)
                
                if (planId != null) {
                    // Recargar la lista de planes
                    loadStudyPlans()
                    onSuccess(planId)
                } else {
                    onError("No se pudo crear el plan de estudio con IA")
                }
            } catch (e: Exception) {
                onError("Error al crear el plan de estudio con IA: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun generateAIStudyPlan(
        subject: String,
        startDateTime: Date,
        endDateTime: Date
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _showAIResponse.value = false
            _aiResponse.value = null
            
            try {
                val response = repository.generateAIStudyPlan(subject, startDateTime, endDateTime)
                _aiResponse.value = response
                _showAIResponse.value = true
            } catch (e: Exception) {
                _error.value = "Error al generar el plan con IA: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateCompletionStatus(planId: String, isCompleted: Boolean, completionRate: Int) {
        viewModelScope.launch {
            try {
                val success = repository.updateCompletionStatus(planId, isCompleted, completionRate)
                if (success) {
                    // Actualizar el plan seleccionado si corresponde
                    _selectedPlan.value?.let { currentPlan ->
                        if (currentPlan.id == planId) {
                            _selectedPlan.value = currentPlan.copy(
                                isCompleted = isCompleted,
                                completionRate = completionRate
                            )
                        }
                    }
                    // Recargar la lista de planes
                    loadStudyPlans()
                }
            } catch (e: Exception) {
                _error.value = "Error al actualizar el estado del plan: ${e.message}"
            }
        }
    }
    
    fun deleteStudyPlan(planId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                val success = repository.deleteStudyPlan(planId)
                if (success) {
                    // Recargar la lista de planes
                    loadStudyPlans()
                    onSuccess()
                } else {
                    onError("No se pudo eliminar el plan de estudio")
                }
            } catch (e: Exception) {
                onError("Error al eliminar el plan de estudio: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Método para actualizar el estado de una sesión específica
    fun updateSessionStatus(planId: String, sessionId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            try {
                _selectedPlan.value?.let { currentPlan ->
                    if (currentPlan.id == planId) {
                        // Actualizar la sesión en la lista de sesiones
                        val updatedSessions = currentPlan.sessions.map { session ->
                            if (session.id == sessionId) {
                                session.copy(isCompleted = isCompleted)
                            } else {
                                session
                            }
                        }
                        
                        // Calcular el nuevo porcentaje de completado
                        val completedSessions = updatedSessions.count { it.isCompleted }
                        val totalSessions = updatedSessions.size
                        val newCompletionRate = if (totalSessions > 0) {
                            (completedSessions * 100) / totalSessions
                        } else {
                            0
                        }
                        
                        // Determinar si el plan está completado
                        val allCompleted = updatedSessions.all { it.isCompleted }
                        
                        // Crear el plan actualizado
                        val updatedPlan = currentPlan.copy(
                            sessions = updatedSessions,
                            completionRate = newCompletionRate,
                            isCompleted = allCompleted
                        )
                        
                        // Actualizar el plan en el repositorio
                        val success = repository.updateStudyPlan(updatedPlan)
                        if (success) {
                            // Actualizar el plan seleccionado
                            _selectedPlan.value = updatedPlan
                            
                            // También actualizar el estado general de completado
                            repository.updateCompletionStatus(planId, allCompleted, newCompletionRate)
                            
                            // Recargar la lista de planes
                            loadStudyPlans()
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error al actualizar el estado de la sesión: ${e.message}"
            }
        }
    }
    
    // Método para generar sesiones a partir del plan de estudio y la respuesta de IA
    fun generateSessionsFromAIResponse(planId: String) {
        viewModelScope.launch {
            try {
                _selectedPlan.value?.let { currentPlan ->
                    if (currentPlan.id == planId && currentPlan.aiGenerated && currentPlan.aiResponse.isNotBlank()) {
                        // Extraer sesiones de la respuesta de IA
                        val sessions = parseSessionsFromAIResponse(currentPlan.aiResponse)
                        
                        // Actualizar el plan con las sesiones generadas
                        val updatedPlan = currentPlan.copy(sessions = sessions)
                        
                        // Actualizar en el repositorio
                        val success = repository.updateStudyPlan(updatedPlan)
                        if (success) {
                            // Actualizar el plan seleccionado
                            _selectedPlan.value = updatedPlan
                            
                            // Recargar la lista de planes
                            loadStudyPlans()
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error al generar sesiones: ${e.message}"
            }
        }
    }
    
    // Método auxiliar para extraer las sesiones del texto generado por IA
    private fun parseSessionsFromAIResponse(aiResponse: String): List<StudySession> {
        val sessions = mutableListOf<StudySession>()
        
        try {
            // Buscar el formato específico de secciones "## Sesión X: [Título]"
            val sessionRegex = """##\s+Sesión\s+(\d+):\s+(.+?)(?=##\s+Sesión\s+\d+:|${'$'})""".toRegex(RegexOption.DOT_MATCHES_ALL)
            val matches = sessionRegex.findAll(aiResponse)
            
            matches.forEach { matchResult ->
                val sessionNumber = matchResult.groupValues[1]
                val fullContent = matchResult.groupValues[2].trim()
                
                // Extraer el título (primera línea) y el contenido (resto)
                val title = fullContent.substringBefore("\n").trim()
                val content = fullContent.substringAfter("\n", "").trim()
                
                val fullTitle = if (title.isNotBlank()) {
                    "Sesión $sessionNumber: $title"
                } else {
                    "Sesión $sessionNumber"
                }
                
                // Crear la sesión y añadirla a la lista
                val session = StudySession(
                    id = UUID.randomUUID().toString(),
                    title = fullTitle,
                    duration = 25, // Duración estándar de un pomodoro
                    isCompleted = false,
                    notes = if (content.isNotBlank()) content else fullContent
                )
                
                sessions.add(session)
            }
            
            // Si no se encontraron sesiones con el regex principal, intentar con el regex alternativo
            if (sessions.isEmpty()) {
                // Fallback al método anterior para mantener compatibilidad
                val fallbackRegex = """(?:Sesión|Pomodoro)\s+(\d+)(?:[:-]\s*|\n)(.+?)(?=(?:Sesión|Pomodoro)\s+\d+|${'$'})""".toRegex(RegexOption.DOT_MATCHES_ALL)
                val fallbackMatches = fallbackRegex.findAll(aiResponse)
                
                fallbackMatches.forEach { matchResult ->
                    val sessionNumber = matchResult.groupValues[1]
                    val sessionContent = matchResult.groupValues[2].trim()
                    
                    // Extraer el título de la sesión (primera línea o hasta el primer punto)
                    val title = if (sessionContent.contains("\n")) {
                        sessionContent.substringBefore("\n").trim()
                    } else {
                        sessionContent.substringBefore(".").trim()
                    }
                    
                    // Crear la sesión y añadirla a la lista
                    val session = StudySession(
                        id = UUID.randomUUID().toString(),
                        title = "Sesión $sessionNumber: $title",
                        duration = 25, // Duración estándar de un pomodoro
                        isCompleted = false,
                        notes = sessionContent
                    )
                    
                    sessions.add(session)
                }
            }
            
            // Si aún no se encontraron sesiones, intentar dividir por líneas como último recurso
            if (sessions.isEmpty()) {
                // Dividir por líneas y buscar patrones típicos de sesiones
                val lines = aiResponse.split("\n")
                var currentSessionContent = ""
                var currentSessionTitle = ""
                var currentSessionNumber = 0
                
                for (line in lines) {
                    val trimmedLine = line.trim()
                    
                    if (trimmedLine.matches(""".*?(?:Sesión|Pomodoro|SESIÓN|POMODORO)\s*\d+.*""".toRegex())) {
                        // Si ya teníamos una sesión anterior, guardarla
                        if (currentSessionTitle.isNotBlank()) {
                            sessions.add(
                                StudySession(
                                    id = UUID.randomUUID().toString(),
                                    title = currentSessionTitle,
                                    duration = 25,
                                    isCompleted = false,
                                    notes = currentSessionContent
                                )
                            )
                        }
                        
                        // Intentar extraer el número de sesión
                        val numberRegex = """(?:Sesión|Pomodoro|SESIÓN|POMODORO)\s*(\d+)""".toRegex()
                        val numberMatch = numberRegex.find(trimmedLine)
                        currentSessionNumber = numberMatch?.groupValues?.get(1)?.toIntOrNull() ?: (currentSessionNumber + 1)
                        
                        // Iniciar nueva sesión
                        currentSessionTitle = "Sesión $currentSessionNumber"
                        currentSessionContent = trimmedLine
                    } else if (currentSessionTitle.isNotBlank()) {
                        // Añadir línea a la sesión actual
                        currentSessionContent += "\n$trimmedLine"
                    }
                }
                
                // Añadir la última sesión si existe
                if (currentSessionTitle.isNotBlank()) {
                    sessions.add(
                        StudySession(
                            id = UUID.randomUUID().toString(),
                            title = currentSessionTitle,
                            duration = 25,
                            isCompleted = false,
                            notes = currentSessionContent
                        )
                    )
                }
            }
        } catch (e: Exception) {
            println("Error al analizar sesiones: ${e.message}")
            e.printStackTrace()
        }
        
        return sessions
    }
    
    // Métodos auxiliares para formatear datos
    
    fun formatTimestamp(timestamp: Timestamp?): String {
        if (timestamp == null) return ""
        
        val date = timestamp.toDate()
        val dateFormat = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale("es", "ES"))
        return dateFormat.format(date)
    }
    
    fun formatDate(date: Date?): String {
        if (date == null) return ""
        
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("es", "ES"))
        return dateFormat.format(date)
    }
    
    fun formatTime(date: Date?): String {
        if (date == null) return ""
        
        val timeFormat = SimpleDateFormat("HH:mm", Locale("es", "ES"))
        return timeFormat.format(date)
    }
    
    fun getChipTextFromPlan(plan: StudyPlan): String {
        return when {
            plan.isCompleted -> "Completado"
            plan.aiGenerated -> "Generado por IA"
            else -> "En progreso"
        }
    }
    
    fun getChipColorFromPlan(plan: StudyPlan): androidx.compose.ui.graphics.Color {
        return when {
            plan.isCompleted -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Verde
            plan.aiGenerated -> androidx.compose.ui.graphics.Color(0xFF2196F3) // Azul
            else -> androidx.compose.ui.graphics.Color(0xFFFFA000) // Ámbar
        }
    }
    
    // Método para formatear la respuesta de la IA en secciones
    fun formatAIResponse(response: String): List<ResponseSection> {
        return response.toResponseSections()
    }
    
    fun toggleAIResponseVisibility(show: Boolean) {
        _showAIResponse.value = show
    }
} 