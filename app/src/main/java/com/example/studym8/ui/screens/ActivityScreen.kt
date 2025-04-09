package com.example.studym8.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studym8.data.model.ActivityQuestion
import com.example.studym8.ui.components.ActivityList
import com.example.studym8.ui.components.ActivityProgressCard
import com.example.studym8.ui.components.ActivityQuestionView
import com.example.studym8.ui.viewmodel.ActivityViewModel
import com.example.studym8.ui.viewmodel.StudyPlanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    studyPlanId: String,
    sessionId: String,
    onBackClick: () -> Unit,
    activityViewModel: ActivityViewModel = viewModel(),
    studyPlanViewModel: StudyPlanViewModel = viewModel()
) {
    // Estados
    val activities by activityViewModel.activities.collectAsState()
    val selectedActivity by activityViewModel.selectedActivity.collectAsState()
    val isLoading by activityViewModel.isLoading.collectAsState()
    val error by activityViewModel.error.collectAsState()
    val userAnswer by activityViewModel.userAnswer.collectAsState()
    val evaluationResult by activityViewModel.evaluationResult.collectAsState()
    val activityProgress by activityViewModel.activityProgress.collectAsState()
    val selectedPlan by studyPlanViewModel.selectedPlan.collectAsState()
    
    // Estado local para controlar qué vista mostrar
    var showActivityList by remember { mutableStateOf(true) }
    var currentActivityId by remember { mutableStateOf<String?>(null) }
    
    // Cargar actividades cuando se muestra la pantalla
    LaunchedEffect(studyPlanId, sessionId) {
        activityViewModel.loadActivitiesForSession(studyPlanId, sessionId)
        studyPlanViewModel.getStudyPlanById(studyPlanId)
    }
    
    // Cargar actividad seleccionada cuando cambia
    LaunchedEffect(currentActivityId) {
        currentActivityId?.let {
            activityViewModel.getActivityById(it)
            showActivityList = false
        }
    }
    
    // Título de la sesión
    val sessionTitle = selectedPlan?.sessions?.find { it.id == sessionId }?.title ?: "Actividades"
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (showActivityList) "Actividades: $sessionTitle" else "Pregunta",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (!showActivityList) {
                            showActivityList = true
                            currentActivityId = null
                            activityViewModel.resetEvaluationResult()
                        } else {
                            onBackClick()
                        }
                    }) {
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
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading && activities.isEmpty() && selectedActivity == null) {
                // Mostrar indicador de carga
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (error != null && activities.isEmpty()) {
                // Mostrar error
                Text(
                    text = error ?: "Error desconocido",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            } else {
                // Mostrar contenido
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    if (showActivityList) {
                        // Mostrar tarjeta de progreso
                        ActivityProgressCard(
                            progress = activityProgress
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Mostrar lista de actividades
                        ActivityList(
                            activities = activities,
                            onActivitySelected = { activity ->
                                currentActivityId = activity.id
                            },
                            isLoading = isLoading
                        )
                    } else {
                        // Mostrar actividad seleccionada
                        selectedActivity?.let { activity ->
                            ActivityQuestionView(
                                activity = activity,
                                userAnswer = userAnswer,
                                onAnswerChanged = activityViewModel::setUserAnswer,
                                onSubmit = activityViewModel::submitAnswer,
                                isLoading = isLoading,
                                evaluationResult = evaluationResult,
                                onDismissResult = {
                                    activityViewModel.resetEvaluationResult()
                                    // Si hay más actividades sin responder, mostrar la siguiente
                                    val nextActivity = activityViewModel.getNextUnansweredActivity()
                                    if (nextActivity != null && nextActivity.id != activity.id) {
                                        currentActivityId = nextActivity.id
                                    } else {
                                        // Volver a la lista
                                        showActivityList = true
                                        currentActivityId = null
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
