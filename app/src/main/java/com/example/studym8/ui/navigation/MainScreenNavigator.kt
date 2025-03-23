package com.example.studym8.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.studym8.ui.components.StudyMateBottomNavigation
import com.example.studym8.ui.screens.HomeScreen
import com.example.studym8.ui.screens.ItineraryScreen

/**
 * Este componente maneja la navegación entre las pantallas principales
 * que comparten la barra de navegación inferior
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenNavigator(
    onNavigateToStudyPlanDetail: (String) -> Unit,
    onLogout: () -> Unit,
    navController: NavHostController = rememberNavController()
) {
    // Obtener la ruta actual para resaltar el elemento correcto en la barra de navegación
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavigationRoutes.HOME
    
    // Scaffold con barra de navegación inferior
    Scaffold(
        bottomBar = {
            StudyMateBottomNavigation(
                currentRoute = currentRoute,
                onRouteSelected = { route ->
                    // Navegar a la ruta seleccionada
                    navController.navigate(route) {
                        // Evitar múltiples copias de la misma ruta en el back stack
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        // Restaurar el estado al regresar a esta pantalla
                        restoreState = true
                        // Evitar múltiples clicks en el mismo botón
                        launchSingleTop = true
                    }
                }
            )
        }
    ) { innerPadding ->
        // Host de navegación para las pantallas principales
        NavHost(
            navController = navController,
            startDestination = NavigationRoutes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavigationRoutes.HOME) {
                HomeScreen(
                    onNavigateToStudyPlanDetail = onNavigateToStudyPlanDetail,
                    onLogout = onLogout
                )
            }
            
            composable(NavigationRoutes.ITINERARY) {
                ItineraryScreen(
                    onNavigateToStudyPlanDetail = onNavigateToStudyPlanDetail,
                    onLogout = onLogout
                )
            }
            
            // Aquí se podría agregar la pantalla de estadísticas cuando esté implementada
            composable(NavigationRoutes.STATS) {
                // StatisticsScreen() - A implementar en el futuro
                HomeScreen( // Por ahora usamos HomeScreen como placeholder
                    onNavigateToStudyPlanDetail = onNavigateToStudyPlanDetail,
                    onLogout = onLogout
                )
            }
        }
    }
} 