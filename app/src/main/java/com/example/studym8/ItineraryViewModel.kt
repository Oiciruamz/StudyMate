package com.example.studym8

import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*
import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import androidx.compose.ui.graphics.Color
import com.google.firebase.firestore.FirebaseFirestoreException


// Clase ViewModel para los itinerarios
class ItineraryViewModel : androidx.lifecycle.ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // StateFlow para observar los planes de estudio
    private val _studyPlans = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val studyPlans: StateFlow<List<Map<String, Any>>> = _studyPlans

    // Inicializar cargando los planes de estudio
    init {
        viewModelScope.launch {
            loadStudyPlans()
        }
    }

    // Función para cargar los planes de estudio y actualizar el StateFlow
    suspend fun loadStudyPlans() {
        try {
            val plans = getStudyPlans()
            _studyPlans.value = plans
        } catch (e: Exception) {
            Log.e("ItineraryViewModel", "Error al cargar planes de estudio", e)
        }
    }

    // Función para obtener el ID del usuario actual
    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("No hay una sesión de usuario activa")
    }

    // Función para generar un itinerario usando la IA
    suspend fun generateAIStudyPlan(
        subject: String,
        startDateTime: Date,
        endDateTime: Date
    ): String? {
        val db = FirebaseFirestore.getInstance()
        val generateCollectionRef = db.collection("generate")

        try {
            // Calculamos la duración en horas y minutos
            val durationMs = endDateTime.time - startDateTime.time
            val durationHours = (durationMs / (1000 * 60 * 60)).toInt()
            val durationMinutes = ((durationMs % (1000 * 60 * 60)) / (1000 * 60)).toInt()

            // Crear el prompt para la IA
            val prompt = """
                Crea un plan de estudio detallado para el tema '$subject' con duración de $durationHours horas y $durationMinutes minutos.
                El plan debe seguir la metodología Pomodoro (25 minutos de estudio y 5 minutos de descanso).
                Incluye actividades específicas para cada sesión de estudio, como lecturas, ejercicios prácticos, y repasos.
                Proporciona una estructura clara dividida en sesiones, con objetivos específicos para cada una.
            """.trimIndent()

            // Crear el documento para la extensión de Gemini con el ID del usuario actual
            val documentId = UUID.randomUUID().toString()
            val requestData = hashMapOf(
                "prompt" to prompt,
                "timestamp" to Timestamp.now(),
                "userId" to getCurrentUserId() // Asociar la solicitud con el usuario actual
            )

            // Guardar el documento en la colección "generate"
            generateCollectionRef.document(documentId).set(requestData).await()

            // Esperar a que la IA genere la respuesta (con timeout)
            var attempts = 0
            val maxAttempts = 10

            while (attempts < maxAttempts) {
                // Esperar un momento para que la extensión procese
                kotlinx.coroutines.delay(2000) // 2 segundos

                // Obtener el documento actualizado
                val document = generateCollectionRef.document(documentId).get().await()

                // Verificar si ya contiene la respuesta
                if (document.contains("response")) {
                    return document.getString("response")
                }

                attempts++
            }

            return "No se pudo generar el plan de estudio con IA después de varios intentos."
        } catch (e: Exception) {
            Log.e("ItineraryViewModel", "Error al generar plan con IA", e)
            return null
        }
    }

    // Función para crear un plan de estudio basado en la respuesta de la IA
    fun createAIStudyPlan(
        subject: String,
        startDateTime: Date,
        endDateTime: Date,
        aiResponse: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        // Obtener el ID del usuario actual en el momento de crear el plan
        val currentUserId = getCurrentUserId()

        // Calcular duración total en minutos
        val durationMs = endDateTime.time - startDateTime.time
        val durationMinutes = (durationMs / (1000 * 60)).toInt()

        // Crear modelo de datos para el plan de estudio
        val studyPlan = hashMapOf(
            "userId" to currentUserId,
            "title" to "Estudio de $subject",
            "subject" to subject,
            "description" to "Plan de estudio generado con IA",
            "startDateTime" to Timestamp(startDateTime),
            "endDateTime" to Timestamp(endDateTime),
            "totalDuration" to durationMinutes,
            "isCompleted" to false,
            "completionRate" to 0,
            "aiGenerated" to true,
            "createdAt" to Timestamp.now(),
            "updatedAt" to Timestamp.now(),
            "tags" to listOf(subject.lowercase()),
            "color" to "#4285F4",  // Color azul de Google
            "aiResponse" to aiResponse  // Guardamos la respuesta completa de la IA
        )

        // Guardar en Firestore
        db.collection("studyPlans")
            .add(studyPlan)
            .addOnSuccessListener { documentReference ->
                // Recargar los planes después de añadir uno nuevo
                viewModelScope.launch {
                    loadStudyPlans()
                }
                onSuccess(documentReference.id)
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

    // Función para obtener los itinerarios del usuario actual
    suspend fun getStudyPlans(): List<Map<String, Any>> {
        return try {
            val currentUserId = getCurrentUserId()

            // Opción 1: Solo filtrar por userId sin ordenar (solución temporal)
            val snapshot = db.collection("studyPlans")
                .whereEqualTo("userId", currentUserId)
                .get()
                .await()

            // Ordenar los resultados después de obtenerlos
            val results = snapshot.documents.mapNotNull { it.data }

            // Ordenar manualmente por startDateTime
            results.sortedBy {
                val timestamp = it["startDateTime"] as? Timestamp
                timestamp?.seconds ?: 0
            }
        } catch (e: Exception) {
            Log.e("ItineraryViewModel", "Error al obtener planes de estudio", e)
            if (e is FirebaseFirestoreException && e.message?.contains("FAILED_PRECONDITION") == true) {
                // Mostrar un mensaje más específico para ayudar a solucionar el problema
                Log.e("ItineraryViewModel", "Se requiere crear un índice en Firestore. " +
                        "Sigue el enlace que aparece en el mensaje de error o ve a la consola de Firebase para crearlo.", e)
            }
            emptyList()
        }
    }

    // Función para obtener el email del usuario actual
    fun getCurrentUserEmail(): String {
        return auth.currentUser?.email ?: throw IllegalStateException("No hay una sesión de usuario activa")
    }

    // Utilidades para formatear fechas y trabajar con los datos

    // Formatear timestamp para mostrar en la UI
    fun formatTimestamp(timestamp: Timestamp?): String {
        if (timestamp == null) return "Fecha no disponible"

        val date = timestamp.toDate()
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        return if (isToday(date)) {
            "Hoy a las ${timeFormat.format(date)}"
        } else {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            "${dateFormat.format(date)} a las ${timeFormat.format(date)}"
        }
    }

    // Comprobar si una fecha es hoy
    fun isToday(date: Date): Boolean {
        val today = Calendar.getInstance()
        val dateCalendar = Calendar.getInstance().apply { time = date }

        return today.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == dateCalendar.get(Calendar.DAY_OF_YEAR)
    }

    // Determinar el color del chip basado en las etiquetas o tema
    fun getChipColorFromPlan(plan: Map<String, Any>): Color {
        val colorHex = plan["color"] as? String ?: "#B3E5FC"
        return try {
            Color(android.graphics.Color.parseColor(colorHex))
        } catch (e: Exception) {
            Color(0xFFB3E5FC) // Azul claro por defecto
        }
    }

    // Determinar el texto del chip basado en las etiquetas o tema
    fun getChipTextFromPlan(plan: Map<String, Any>): String {
        val tags = plan["tags"] as? List<String> ?: emptyList()
        val subject = plan["subject"] as? String ?: ""

        return when {
            tags.isNotEmpty() -> tags.first().replaceFirstChar { it.uppercase() }
            subject.isNotEmpty() -> subject.replaceFirstChar { it.uppercase() }
            else -> "Estudios"
        }
    }

    // StateFlow para almacenar un plan específico seleccionado
    private val _selectedPlan = MutableStateFlow<Map<String, Any>?>(null)
    val selectedPlan: StateFlow<Map<String, Any>?> = _selectedPlan

    // Función para obtener un plan específico por ID
    suspend fun getStudyPlanById(planId: String) {
        try {
            val documentSnapshot = db.collection("studyPlans")
                .document(planId)
                .get()
                .await()

            if (documentSnapshot.exists()) {
                // Crear un mapa que incluya el ID del documento y sus datos
                val planData = documentSnapshot.data ?: emptyMap()
                val planWithId = planData.toMutableMap().apply {
                    this["id"] = planId
                }
                _selectedPlan.value = planWithId
            } else {
                Log.e("ItineraryViewModel", "Plan de estudio no encontrado con ID: $planId")
                _selectedPlan.value = null
            }
        } catch (e: Exception) {
            Log.e("ItineraryViewModel", "Error al obtener plan de estudio", e)
            _selectedPlan.value = null
        }
    }

    // Función para establecer el plan seleccionado directamente
    fun setSelectedPlan(plan: Map<String, Any>) {
        _selectedPlan.value = plan
    }

    // Función para limpiar el plan seleccionado
    fun clearSelectedPlan() {
        _selectedPlan.value = null
    }

    // Función para marcar un plan como completado o pendiente (toggle)
    fun togglePlanCompletion(planId: String) {
        viewModelScope.launch {
            try {
                // Obtener el documento actual
                val docRef = db.collection("studyPlans").document(planId)
                val snapshot = docRef.get().await()

                if (snapshot.exists()) {
                    // Obtener el estado actual
                    val isCurrentlyCompleted = snapshot.getBoolean("isCompleted") ?: false

                    // Actualizar al estado opuesto
                    docRef.update(
                        mapOf(
                            "isCompleted" to !isCurrentlyCompleted,
                            "updatedAt" to Timestamp.now()
                        )
                    ).await()

                    // Si estamos viendo este plan, actualizar también el selectedPlan
                    _selectedPlan.value?.let { currentPlan ->
                        if (currentPlan["id"] == planId) {
                            val updatedPlan = currentPlan.toMutableMap().apply {
                                this["isCompleted"] = !isCurrentlyCompleted
                                this["updatedAt"] = Timestamp.now()
                            }
                            _selectedPlan.value = updatedPlan
                        }
                    }

                    // Recargar la lista completa
                    loadStudyPlans()
                }
            } catch (e: Exception) {
                Log.e("ItineraryViewModel", "Error al cambiar estado de completado", e)
            }
        }
    }
}

