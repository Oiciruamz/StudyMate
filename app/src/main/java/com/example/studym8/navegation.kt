package com.example.studym8

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

// Rutas de navegación
object NavigationRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home" // Esta es tu pantalla de inicio
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavigationRoutes.LOGIN
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(NavigationRoutes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    // Navegar a la pantalla de inicio después de un login exitoso
                    navController.navigate(NavigationRoutes.HOME) {
                        // Limpiar el back stack para que no puedan volver a la pantalla de login
                        popUpTo(NavigationRoutes.LOGIN) { inclusive = true }
                    }
                },
                navigateToRegister = {
                    navController.navigate(NavigationRoutes.REGISTER)
                }
            )
        }

        composable(NavigationRoutes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    // Navegar a la pantalla de inicio después de un registro exitoso
                    navController.navigate(NavigationRoutes.HOME) {
                        // Limpiar el back stack para que no puedan volver a las pantallas de autenticación
                        popUpTo(NavigationRoutes.LOGIN) { inclusive = true }
                    }
                },
                navigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(NavigationRoutes.HOME) {
            // Usamos StudyMateApp y le pasamos la función para manejar el cierre de sesión
            StudyMateApp(
                onLogout = {
                    // Al cerrar sesión, navegamos de vuelta a la pantalla de login
                    navController.navigate(NavigationRoutes.LOGIN) {
                        // Limpiamos todo el back stack para que no puedan volver a las pantallas anteriores
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
fun MainAppContent() {
    // Determinar la pantalla inicial basada en si el usuario ya está autenticado
    val startDestination = if (Firebase.auth.currentUser != null) {
        NavigationRoutes.HOME
    } else {
        NavigationRoutes.LOGIN
    }

    // Iniciar la navegación con la ruta determinada
    AppNavigation(startDestination = startDestination)
}