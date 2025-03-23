package com.example.studym8.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.studym8.ui.screens.HomeScreen
import com.example.studym8.ui.screens.ItineraryScreen
import com.example.studym8.ui.screens.StudyPlanDetailScreen
import com.example.studym8.ui.viewmodel.AuthViewModel
import com.example.studym8.ui.viewmodel.StudyPlanViewModel

/**
 * Rutas de navegación de la aplicación
 */
object NavigationRoutes {
    // Rutas principales
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val ITINERARY = "itinerary"
    const val STATS = "stats"
    
    // Rutas con parámetros
    const val STUDY_PLAN_DETAIL_BASE = "study_plan_detail"
    const val STUDY_PLAN_DETAIL = "$STUDY_PLAN_DETAIL_BASE/{planId}"
    
    // Ruta principal que contiene las pantallas con navegación inferior
    const val MAIN = "main"
    
    // Función para crear la ruta de detalle de plan de estudio con un ID específico
    fun studyPlanDetail(planId: String): String = "$STUDY_PLAN_DETAIL_BASE/$planId"
}

/**
 * Componente principal de navegación que gestiona las rutas de la aplicación
 */
@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    studyPlanViewModel: StudyPlanViewModel,
    navController: NavHostController = rememberNavController()
) {
    // Observar el estado de autenticación
    val currentUser by authViewModel.currentUser.collectAsState(initial = null)
    
    // Determinar la ruta inicial según el estado de autenticación
    val startDestination = if (currentUser != null) NavigationRoutes.MAIN else NavigationRoutes.LOGIN
    
    // Efecto para navegar según el estado de autenticación
    LaunchedEffect(currentUser) {
        if (currentUser == null && navController.currentDestination?.route != NavigationRoutes.LOGIN
            && navController.currentDestination?.route != NavigationRoutes.REGISTER) {
            // Si el usuario cierra sesión, regresar a la pantalla de login
            navController.navigate(NavigationRoutes.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        } else if (currentUser != null && navController.currentDestination?.route == NavigationRoutes.LOGIN) {
            // Si el usuario inicia sesión, navegar a la pantalla principal
            navController.navigate(NavigationRoutes.MAIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
    
    NavHost(navController = navController, startDestination = startDestination) {
        // Pantalla de inicio de sesión
        composable(NavigationRoutes.LOGIN) {
            // Temporalmente usamos HomeScreen en lugar de LoginScreen hasta implementarla
            HomeScreen(
                onNavigateToStudyPlanDetail = { planId ->
                    navController.navigate(NavigationRoutes.studyPlanDetail(planId))
                },
                onLogout = {
                    authViewModel.signOut()
                }
            )
        }
        
        // Pantalla de registro
        composable(NavigationRoutes.REGISTER) {
            // Temporalmente usamos HomeScreen en lugar de RegisterScreen hasta implementarla
            HomeScreen(
                onNavigateToStudyPlanDetail = { planId ->
                    navController.navigate(NavigationRoutes.studyPlanDetail(planId))
                },
                onLogout = {
                    authViewModel.signOut()
                }
            )
        }
        
        // Pantalla principal con navegación inferior
        composable(NavigationRoutes.MAIN) {
            MainScreenNavigator(
                onNavigateToStudyPlanDetail = { planId ->
                    navController.navigate(NavigationRoutes.studyPlanDetail(planId))
                },
                onLogout = {
                    authViewModel.signOut()
                }
            )
        }
        
        // Pantalla de detalle del plan de estudio
        composable(
            route = NavigationRoutes.STUDY_PLAN_DETAIL,
            arguments = listOf(navArgument("planId") { type = NavType.StringType })
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getString("planId") ?: ""
            StudyPlanDetailScreen(
                planId = planId,
                authViewModel = authViewModel,
                studyPlanViewModel = studyPlanViewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
