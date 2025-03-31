package com.example.studym8.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studym8.data.repository.StudyPlanRepository
import com.example.studym8.data.repository.UserRepository
import com.example.studym8.ui.viewmodel.AuthViewModel
import com.example.studym8.ui.viewmodel.StudyPlanViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * Componente principal que inicializa los repositorios y viewmodels
 * para la aplicación y configura la navegación
 */
@Composable
fun MainAppContent(
    onNavigateFromNotification: String? = null
) {
    // Inicializar Firebase
    val firebaseAuth = Firebase.auth
    val firestore = Firebase.firestore
    
    // Inicializar repositorios
    val userRepository = UserRepository()
    val studyPlanRepository = StudyPlanRepository()
    
    // Inicializar ViewModels usando composable viewModel
    val authViewModel: AuthViewModel = viewModel()
    val studyPlanViewModel: StudyPlanViewModel = viewModel()
    
    // Verificar el usuario actual al inicio
    authViewModel.checkCurrentUser()
    
    // Configurar la navegación de la aplicación
    AppNavigation(
        authViewModel = authViewModel,
        studyPlanViewModel = studyPlanViewModel,
        onNavigateFromNotification = onNavigateFromNotification
    )
} 