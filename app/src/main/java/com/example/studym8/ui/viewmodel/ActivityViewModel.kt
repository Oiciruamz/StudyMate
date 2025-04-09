package com.example.studym8.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studym8.data.model.ActivityQuestion
import com.example.studym8.data.model.QuestionType
import com.example.studym8.data.repository.ActivityRepository
import com.example.studym8.data.repository.StudyPlanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ActivityViewModel : ViewModel() {

    private val activityRepository = ActivityRepository()
    private val studyPlanRepository = StudyPlanRepository()

    // Estado para la lista de actividades
    private val _activities = MutableStateFlow<List<ActivityQuestion>>(emptyList())
    val activities: StateFlow<List<ActivityQuestion>> = _activities

    // Estado para la actividad seleccionada
    private val _selectedActivity = MutableStateFlow<ActivityQuestion?>(null)
    val selectedActivity: StateFlow<ActivityQuestion?> = _selectedActivity

    // Estado para manejar estados de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Estado para manejar errores
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Estado para manejar la respuesta del usuario
    private val _userAnswer = MutableStateFlow("")
    val userAnswer: StateFlow<String> = _userAnswer

    // Estado para manejar el resultado de la evaluación
    private val _evaluationResult = MutableStateFlow<Pair<Boolean, String>?>(null)
    val evaluationResult: StateFlow<Pair<Boolean, String>?> = _evaluationResult

    // Estado para el progreso general de las actividades
    private val _activityProgress = MutableStateFlow(0)
    val activityProgress: StateFlow<Int> = _activityProgress

    fun loadActivitiesForSession(studyPlanId: String, sessionId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val activities = activityRepository.getActivitiesForSession(studyPlanId, sessionId)
                _activities.value = activities

                // Calcular progreso
                calculateProgress(activities)

                // Si no hay actividades, mostrar un indicador de carga mientras se generan
                // Las actividades ya deberían haberse generado automáticamente cuando se creó el plan
                // o cuando se generaron las sesiones, pero por si acaso, verificamos
                if (activities.isEmpty()) {
                    _isLoading.value = true

                    // Esperar un momento para dar tiempo a que se generen las actividades
                    kotlinx.coroutines.delay(2000)

                    // Intentar cargar las actividades nuevamente
                    val refreshedActivities = activityRepository.getActivitiesForSession(studyPlanId, sessionId)

                    // Si aún no hay actividades, generarlas
                    if (refreshedActivities.isEmpty()) {
                        // Obtener detalles de la sesión para generar actividades relevantes
                        val studyPlan = studyPlanRepository.getStudyPlanById(studyPlanId)
                        studyPlan?.let { plan ->
                            val session = plan.sessions.find { it.id == sessionId }
                            session?.let {
                                generateActivitiesForSession(
                                    studyPlanId = studyPlanId,
                                    sessionId = sessionId,
                                    sessionTitle = it.title,
                                    sessionContent = it.notes
                                )
                            }
                        }
                    } else {
                        // Si se encontraron actividades en el segundo intento, actualizarlas
                        _activities.value = refreshedActivities
                        calculateProgress(refreshedActivities)
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error al cargar las actividades: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getActivityById(activityId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val activity = activityRepository.getActivityById(activityId)
                _selectedActivity.value = activity

                // Si la actividad ya tiene una respuesta, cargarla
                activity?.let {
                    if (it.isAnswered) {
                        _userAnswer.value = it.userAnswer
                        _evaluationResult.value = Pair(it.isCorrect, it.feedback)
                    } else {
                        _userAnswer.value = ""
                        _evaluationResult.value = null
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error al cargar la actividad: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setUserAnswer(answer: String) {
        _userAnswer.value = answer
    }

    fun submitAnswer() {
        viewModelScope.launch {
            _selectedActivity.value?.let { activity ->
                _isLoading.value = true
                _error.value = null

                try {
                    val result = activityRepository.submitAnswer(activity.id, _userAnswer.value)
                    _evaluationResult.value = result

                    // Recargar la actividad para obtener los cambios
                    getActivityById(activity.id)

                    // Recargar todas las actividades para actualizar el progreso
                    loadActivitiesForSession(activity.studyPlanId, activity.sessionId)
                } catch (e: Exception) {
                    _error.value = "Error al enviar la respuesta: ${e.message}"
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun generateActivitiesForSession(
        studyPlanId: String,
        sessionId: String,
        sessionTitle: String,
        sessionContent: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val newActivities = activityRepository.generateActivitiesForSession(
                    studyPlanId = studyPlanId,
                    sessionId = sessionId,
                    sessionTitle = sessionTitle,
                    sessionContent = sessionContent
                )

                if (newActivities.isNotEmpty()) {
                    _activities.value = newActivities
                    calculateProgress(newActivities)
                } else {
                    _error.value = "No se pudieron generar actividades"
                }
            } catch (e: Exception) {
                _error.value = "Error al generar actividades: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun calculateProgress(activities: List<ActivityQuestion>) {
        if (activities.isEmpty()) {
            _activityProgress.value = 0
            return
        }

        val answeredCount = activities.count { it.isAnswered }
        _activityProgress.value = (answeredCount * 100) / activities.size
    }

    fun resetEvaluationResult() {
        _evaluationResult.value = null
    }

    fun getNextUnansweredActivity(): ActivityQuestion? {
        return _activities.value.firstOrNull { !it.isAnswered }
    }

    fun getActivityTypeText(type: QuestionType): String {
        return when (type) {
            QuestionType.MULTIPLE_CHOICE -> "Opción múltiple"
            QuestionType.TRUE_FALSE -> "Verdadero/Falso"
            QuestionType.SHORT_ANSWER -> "Respuesta corta"
            QuestionType.ESSAY -> "Ensayo"
        }
    }
}
