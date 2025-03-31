package com.example.studym8.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studym8.ui.components.StudyMateBottomNavigation
import com.example.studym8.ui.components.StudyMateTopBar
import com.example.studym8.ui.screens.HomeScreen
import com.example.studym8.ui.screens.ItineraryScreen
import com.example.studym8.ui.screens.StatsScreen
import com.example.studym8.ui.viewmodel.AuthViewModel
import com.example.studym8.ui.viewmodel.StudyPlanViewModel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

/**
 * Este componente maneja la navegación entre las pantallas principales
 * que comparten la barra de navegación inferior
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenNavigator(
    onNavigateToStudyPlanDetail: (String) -> Unit,
    onLogout: () -> Unit,
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    studyPlanViewModel: StudyPlanViewModel = viewModel()
) {
    // Obtener la ruta actual para resaltar el elemento correcto en la barra de navegación
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavigationRoutes.HOME
    
    // Obtener el usuario actual
    val currentUser by authViewModel.currentUser.collectAsState(initial = null)
    
    // Variable para controlar si se muestra el diálogo de notificaciones
    var showNotificationsDialog by remember { mutableStateOf(false) }
    
    // Obtener el título según la ruta actual
    val screenTitle = when (currentRoute) {
        NavigationRoutes.HOME -> "Inicio"
        NavigationRoutes.ITINERARY -> "Itinerarios"
        NavigationRoutes.STATS -> "Estadísticas"
        else -> ""
    }
    
    // Diálogo de notificaciones
    if (showNotificationsDialog) {
        // Aquí se implementaría el diálogo de notificaciones
        // Por ahora, simplemente mostraremos un diálogo vacío
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            title = { Text("Notificaciones") },
            text = { Text("Aquí se mostrarían tus notificaciones.") },
            confirmButton = {
                TextButton(onClick = { showNotificationsDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
    
    // Scaffold con barra de navegación inferior y TopBar compartida
    Scaffold(
        topBar = {
            StudyMateTopBar(
                user = currentUser,
                title = screenTitle,
                onLogoutClick = onLogout,
                onNotificationsClick = { showNotificationsDialog = true }
            )
        },
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
            
            composable(NavigationRoutes.STATS) {
                StatsScreen(
                    onLogout = onLogout
                )
            }
        }
    }
} 