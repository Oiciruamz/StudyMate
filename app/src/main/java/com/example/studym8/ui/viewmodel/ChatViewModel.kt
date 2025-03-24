package com.example.studym8.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studym8.data.model.ChatMessage
import com.example.studym8.data.model.StudyPlan
import com.example.studym8.data.repository.StudyPlanRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ChatViewModel(initialPlanId: String? = null) : ViewModel() {
    
    private val repository = StudyPlanRepository()
    private val firestore = FirebaseFirestore.getInstance()
    
    // Estado para el plan de estudio seleccionado
    private val _studyPlan = MutableStateFlow<StudyPlan?>(null)
    val studyPlan: StateFlow<StudyPlan?> = _studyPlan
    
    // Estado para los mensajes del chat
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages
    
    // Estado para indicar si está cargando
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    // Estado para errores
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    // ID del plan de estudio actual
    private var currentPlanId: String? = initialPlanId
    
    init {
        // Si se proporciona un ID inicial, cargar su plan de estudio
        initialPlanId?.let { planId ->
            loadStudyPlan(planId)
        }
    }
    
    // Limpiar mensajes (útil al cambiar entre chats)
    fun clearMessages() {
        _messages.value = emptyList()
    }
    
    // Cargar el plan de estudio
    fun loadStudyPlan(planId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val plan = repository.getStudyPlanById(planId)
                _studyPlan.value = plan
                currentPlanId = planId
                
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al cargar el plan de estudio: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Cargar mensajes específicos del plan
    fun loadMessages(planId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val messagesRef = firestore.collection("chat_messages")
                    .whereEqualTo("planId", planId)
                    .orderBy("timestamp")
                    .get()
                    .await()
                
                val chatMessages = messagesRef.documents.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        ChatMessage(
                            id = doc.id,
                            content = data["content"] as? String ?: "",
                            isFromUser = data["isFromUser"] as? Boolean ?: true,
                            timestamp = data["timestamp"] as? com.google.firebase.Timestamp 
                                ?: com.google.firebase.Timestamp.now()
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                
                _messages.value = chatMessages
                
                // Si no hay mensajes, añadir un mensaje de bienvenida
                if (chatMessages.isEmpty() && _studyPlan.value != null) {
                    val plan = _studyPlan.value!!
                    val welcomeMessage = "¡Hola! Soy tu asistente para el plan de estudio '${plan.title}'. Puedes preguntarme cualquier cosa sobre este plan, las sesiones, o cómo sacar el máximo provecho de tus estudios."
                    addSystemMessage(welcomeMessage, planId)
                }
                
            } catch (e: Exception) {
                _error.value = "Error al cargar los mensajes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Enviar un mensaje al chat
    fun sendMessage(content: String, planId: String) {
        // Añadir el mensaje del usuario
        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            content = content,
            isFromUser = true
        )
        _messages.value = _messages.value + userMessage
        
        // Guardar mensaje en Firestore
        viewModelScope.launch {
            try {
                val messageData = hashMapOf(
                    "id" to userMessage.id,
                    "content" to userMessage.content,
                    "isFromUser" to userMessage.isFromUser,
                    "timestamp" to userMessage.timestamp,
                    "planId" to planId
                )
                
                firestore.collection("chat_messages")
                    .document(userMessage.id)
                    .set(messageData)
                    .await()
                
                // Procesar y generar respuesta
                val currentPlan = _studyPlan.value
                if (currentPlan != null) {
                    generateResponse(userMessage.content, currentPlan, planId)
                }
            } catch (e: Exception) {
                _error.value = "Error al guardar el mensaje: ${e.message}"
            }
        }
    }
    
    // Añadir un mensaje del sistema (IA)
    private fun addSystemMessage(content: String, planId: String) {
        val systemMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            content = content,
            isFromUser = false
        )
        _messages.value = _messages.value + systemMessage
        
        // Guardar mensaje en Firestore
        viewModelScope.launch {
            try {
                val messageData = hashMapOf(
                    "id" to systemMessage.id,
                    "content" to systemMessage.content,
                    "isFromUser" to systemMessage.isFromUser,
                    "timestamp" to systemMessage.timestamp,
                    "planId" to planId
                )
                
                firestore.collection("chat_messages")
                    .document(systemMessage.id)
                    .set(messageData)
                    .await()
            } catch (e: Exception) {
                _error.value = "Error al guardar la respuesta: ${e.message}"
            }
        }
    }
    
    // Generar respuesta utilizando IA
    private fun generateResponse(userQuery: String, plan: StudyPlan, planId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                // Crear un prompt con el contexto del plan y la pregunta del usuario
                val prompt = createPromptWithContext(userQuery, plan)
                
                // Llamar a la función que interactúa con la API de IA
                val response = repository.generateChatResponse(prompt)
                
                // Añadir la respuesta al chat
                if (response != null) {
                    addSystemMessage(response, planId)
                } else {
                    addSystemMessage("Lo siento, no pude generar una respuesta en este momento. Por favor, intenta de nuevo más tarde.", planId)
                }
            } catch (e: Exception) {
                addSystemMessage("Lo siento, ocurrió un error al procesar tu pregunta: ${e.message}", planId)
                _error.value = "Error al generar respuesta: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Crear un prompt con contexto para la IA
    private fun createPromptWithContext(userQuery: String, plan: StudyPlan): String {
        // Creamos un contexto con la información del plan
        val sessionInfo = plan.sessions.joinToString("\n\n") { session ->
            """
            ## Sesión: ${session.title}
            - Duración: ${session.duration} minutos
            - Estado: ${if (session.isCompleted) "Completada" else "Pendiente"}
            - Notas: ${session.notes}
            """.trimIndent()
        }
        
        return """
            Eres un asistente especializado en planes de estudio. Tienes información sobre el siguiente plan de estudio:
            
            # Plan de Estudio
            - Título: ${plan.title}
            - Asignatura: ${plan.subject}
            - Descripción: ${plan.description}
            - Duración total: ${plan.totalDuration} minutos
            - Estado: ${if (plan.isCompleted) "Completado" else "En progreso (${plan.completionRate}% completado)"}
            
            # Sesiones
            $sessionInfo
            
            ${if (plan.aiResponse.isNotBlank()) "\n# Plan generado por IA\n${plan.aiResponse}\n" else ""}
            
            El usuario te ha hecho la siguiente pregunta:
            "$userQuery"
            
            Responde de manera clara, concisa y útil a la pregunta del usuario. Usa formato markdown para hacer tu respuesta más legible.
        """.trimIndent()
    }
} 