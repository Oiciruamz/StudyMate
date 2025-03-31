package com.example.studym8.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.ime
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.studym8.ui.components.ChatView
import com.example.studym8.ui.viewmodel.ChatViewModel
import com.example.studym8.ui.viewmodel.ChatViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyPlanChatScreen(
    planId: String,
    onBackClick: () -> Unit
) {
    // Crear un ViewModel específico para este plan de estudio
    val chatViewModel = viewModel<ChatViewModel>(
        factory = ChatViewModelFactory(planId),
        key = "chat_$planId" // Clave única para este ViewModel
    )

    // Estados
    val studyPlan by chatViewModel.studyPlan.collectAsState()
    val messages by chatViewModel.messages.collectAsState()
    val isLoading by chatViewModel.isLoading.collectAsState()
    val error by chatViewModel.error.collectAsState()

    // Configuración para el comportamiento de scroll y topAppBar
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // Cargar el plan de estudio cuando se muestra la pantalla
    LaunchedEffect(planId) {
        // Limpiar mensajes anteriores y cargar los específicos para este plan
        chatViewModel.clearMessages()
        chatViewModel.loadStudyPlan(planId)
        chatViewModel.loadMessages(planId)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "StudyMate Chat",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                scrollBehavior = scrollBehavior,
                modifier = Modifier.shadow(4.dp)
            )
        },
        // Configurar los WindowInsets para mantener la barra superior fija
        contentWindowInsets = WindowInsets.ime.exclude(WindowInsets(0)),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (studyPlan == null && isLoading) {
                // Mostrar indicador de carga mientras se carga el plan
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                // Mostrar el chat con el plan de estudio
                ChatView(
                    studyPlan = studyPlan,
                    messages = messages,
                    isLoading = isLoading,
                    onSendMessage = { message ->
                        chatViewModel.sendMessage(message, planId)
                    }
                )
            }
        }
    }
}

