package com.example.studym8.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studym8.data.model.ResponseSection
import com.example.studym8.data.model.ResponseSectionType
import com.example.studym8.data.model.StudyPlan
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
    
    fun loadStudyPlans() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val plans = repository.getStudyPlans()
                _studyPlans.value = plans
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al cargar los planes de estudio: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun getStudyPlanById(planId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val plan = repository.getStudyPlanById(planId)
                _selectedPlan.value = plan
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
        val sections = mutableListOf<ResponseSection>()
        val lines = response.split("\n")
        var currentSection = ""
        var inList = false

        for (line in lines) {
            val trimmedLine = line.trim()
            
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
    
    fun toggleAIResponseVisibility(show: Boolean) {
        _showAIResponse.value = show
    }
} 