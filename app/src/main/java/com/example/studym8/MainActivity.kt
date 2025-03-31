package com.example.studym8

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studym8.ui.navigation.MainAppContent
import com.example.studym8.ui.theme.Studym8Theme
import com.example.studym8.ui.viewmodel.StudyPlanViewModel
import com.example.studym8.ui.navigation.NavigationRoutes
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permiso concedido, inicializar el NotificationManager
            initializeNotificationManager()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Solicitar permisos de notificación si es necesario
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permiso ya concedido, inicializar el NotificationManager
                    initializeNotificationManager()
                }
                else -> {
                    // Solicitar el permiso
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // Para versiones anteriores a Android 13, no se necesita permiso explícito
            initializeNotificationManager()
        }

        setContent {
            Studym8Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    // Obtener el ViewModel para manejar la navegación desde notificaciones
                    val studyPlanViewModel: StudyPlanViewModel = viewModel()
                    
                    // Verificar si se abrió desde una notificación
                    val studyPlanId = intent?.getStringExtra("studyPlanId")
                    val destination = intent?.getStringExtra("destination")
                    
                    // Usar MainAppContent que maneja la navegación y la autenticación
                    MainAppContent(
                        onNavigateFromNotification = if (studyPlanId != null && destination == "StudyPlanDetail") {
                            studyPlanId
                        } else null
                    )
                }
            }
        }
    }

    private fun initializeNotificationManager() {
        // Inicializar el NotificationManager en el ViewModel a través de ViewModelProvider
        // para asegurar que usamos la misma instancia del ViewModel
        val viewModel = androidx.lifecycle.ViewModelProvider(this).get(StudyPlanViewModel::class.java)
        viewModel.initializeNotificationManager(this)
    }
}