package com.example.studym8.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studym8.data.model.StudyPlan
import com.example.studym8.data.repository.StatsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar las estadísticas de la aplicación
 */
class StatsViewModel : ViewModel() {
    
    private val repository = StatsRepository()
    
    // StateFlow para los planes con mejores tiempos de completado
    private val _bestCompletionTimePlans = MutableStateFlow<List<StudyPlan>>(emptyList())
    val bestCompletionTimePlans: StateFlow<List<StudyPlan>> = _bestCompletionTimePlans
    
    // StateFlow para estadísticas diarias
    private val _dailyStats = MutableStateFlow<Map<String, Int>>(emptyMap())
    val dailyStats: StateFlow<Map<String, Int>> = _dailyStats
    
    // StateFlow para distribución por tema
    private val _subjectDistribution = MutableStateFlow<Map<String, Int>>(emptyMap())
    val subjectDistribution: StateFlow<Map<String, Int>> = _subjectDistribution
    
    // StateFlow para días consecutivos de estudio
    private val _consecutiveDays = MutableStateFlow(0)
    val consecutiveDays: StateFlow<Int> = _consecutiveDays
    
    // StateFlow para estadísticas generales
    private val _generalStats = MutableStateFlow<Map<String, Any>>(emptyMap())
    val generalStats: StateFlow<Map<String, Any>> = _generalStats
    
    // StateFlow para manejar estados de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    // StateFlow para manejar errores
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    /**
     * Carga todas las estadísticas necesarias para la pantalla
     */
    fun loadAllStats() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // Cargar todos los datos en paralelo
                loadBestCompletionTimePlans()
                loadDailyStats()
                loadSubjectDistribution()
                loadConsecutiveDays()
                loadGeneralStats()
            } catch (e: Exception) {
                _error.value = "Error al cargar las estadísticas: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Carga los planes con mejores tiempos de completado
     */
    private suspend fun loadBestCompletionTimePlans() {
        try {
            _bestCompletionTimePlans.value = repository.getBestCompletionTimePlans()
        } catch (e: Exception) {
            // Manejar error pero continuar con otras cargas
            e.printStackTrace()
        }
    }
    
    /**
     * Carga estadísticas diarias
     */
    private suspend fun loadDailyStats() {
        try {
            _dailyStats.value = repository.getDailyCompletionStats()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Carga distribución por tema
     */
    private suspend fun loadSubjectDistribution() {
        try {
            _subjectDistribution.value = repository.getSubjectDistribution()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Carga días consecutivos de estudio
     */
    private suspend fun loadConsecutiveDays() {
        try {
            _consecutiveDays.value = repository.getConsecutiveStudyDays()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Carga estadísticas generales
     */
    private suspend fun loadGeneralStats() {
        try {
            _generalStats.value = repository.getGeneralStats()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
} 