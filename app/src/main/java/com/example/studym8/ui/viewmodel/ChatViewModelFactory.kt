package com.example.studym8.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Fábrica para crear instancias de ChatViewModel con un ID de plan específico.
 * Esto asegura que cada plan de estudio tenga su propio ViewModel independiente.
 */
class ChatViewModelFactory(private val planId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(planId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 