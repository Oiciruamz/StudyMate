package com.example.studym8.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studym8.data.model.StudyPlan
import com.example.studym8.ui.components.StudyMateTopBar
import com.example.studym8.ui.viewmodel.StatsViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.min

/**
 * Pantalla de estadísticas que muestra datos sobre los planes de estudio del usuario
 */
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = viewModel(),
    onLogout: () -> Unit
) {
    // Obtener datos del ViewModel
    val bestCompletionTimePlans by viewModel.bestCompletionTimePlans.collectAsState()
    val subjectDistribution by viewModel.subjectDistribution.collectAsState()
    val consecutiveDays by viewModel.consecutiveDays.collectAsState()
    val generalStats by viewModel.generalStats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // Cargar estadísticas al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadAllStats()
    }
    
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        if (isLoading) {
            // Mostrar indicador de carga
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            // Mostrar error
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = error ?: "Error desconocido",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            // Mostrar contenido
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Sección de resumen
                Text(
                    text = "Resumen de tu progreso",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Tarjetas con estadísticas clave
                KeyStatsRow(generalStats = generalStats)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Sección de mejores tiempos
                Text(
                    text = "Mejores tiempos de completado",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Gráfico de barras de mejores tiempos
                BestCompletionTimesChart(studyPlans = bestCompletionTimePlans)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Sección de actividad semanal
                Text(
                    text = "Actividad semanal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Gráfico de actividad semanal
                WeeklyActivityChart(viewModel = viewModel)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Sección de itinerarios por tema
                Text(
                    text = "Itinerarios por tema",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Gráfico circular de distribución por tema
                SubjectDistributionChart(subjectDistribution = subjectDistribution)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Tarjeta de logros
                AchievementsCard(
                    generalStats = generalStats,
                    consecutiveDays = consecutiveDays
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Fila de tarjetas con estadísticas clave
 */
@Composable
fun KeyStatsRow(generalStats: Map<String, Any>) {
    val completedPlans = generalStats["completedPlans"] as? Int ?: 0
    val totalPlans = generalStats["totalPlans"] as? Int ?: 0
    val averageCompletionRate = (generalStats["averageCompletionRate"] as? Double ?: 0.0).toInt()
    val totalDuration = generalStats["totalDuration"] as? Int ?: 0
    val mostStudiedSubject = generalStats["mostStudiedSubject"] as? String ?: "-"
    
    val formattedStudyTime = if (totalDuration > 60) {
        "${totalDuration / 60}h ${totalDuration % 60}m"
    } else {
        "${totalDuration}m"
    }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Planes completados
        KeyStatCard(
            icon = Icons.Default.Checklist,
            title = "Completados",
            value = "$completedPlans/$totalPlans",
            modifier = Modifier.weight(1f),
            backgroundColor = MaterialTheme.colorScheme.primaryContainer
        )
        
        // Tiempo de estudio
        KeyStatCard(
            icon = Icons.Default.AccessTime,
            title = "Tiempo total",
            value = formattedStudyTime,
            modifier = Modifier.weight(1f),
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer
        )
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Promedio de completado
        KeyStatCard(
            icon = Icons.Default.Star,
            title = "Promedio",
            value = "$averageCompletionRate%",
            modifier = Modifier.weight(1f),
            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer
        )
        
        // Tema más estudiado            
        KeyStatCard(
            icon = Icons.Default.EmojiEvents,
            title = "Tema principal",
            value = mostStudiedSubject,
            modifier = Modifier.weight(1f),
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

/**
 * Tarjeta individual para una estadística clave
 */
@Composable
fun KeyStatCard(
    icon: ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface
) {
    Card(
        modifier = modifier
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Gráfico de barras para mostrar los mejores tiempos de completado
 */
@Composable
fun BestCompletionTimesChart(studyPlans: List<StudyPlan>) {
    if (studyPlans.isEmpty()) {
        EmptyDataMessage("No hay datos disponibles para mostrar tiempos de completado")
        return
    }
    
    // Obtener los 5 mejores planes completados más rápidamente
    val bestCompletionTimePlans = studyPlans
        .filter { it.isCompleted }
        .sortedBy { it.totalDuration }
        .take(5)
        
    if (bestCompletionTimePlans.isEmpty()) {
        EmptyDataMessage("Completa algún plan de estudio para ver tus mejores tiempos")
        return
    }
    
    // Color base para las barras
    val baseColor = MaterialTheme.colorScheme.primary
    
    // Encontrar el tiempo máximo para escalado
    val maxDuration = bestCompletionTimePlans.maxOfOrNull { it.totalDuration }?.coerceAtLeast(1) ?: 1
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        bestCompletionTimePlans.forEach { plan ->
            // Asegurar que totalDuration sea al menos 1 para evitar divisiones por cero
            val planDuration = plan.totalDuration.coerceAtLeast(1)
            // Calcular ancho de barra entre 10% y 100%
            val barWidth = ((planDuration.toFloat() / maxDuration) * 100).coerceIn(10f, 100f)
            val percentage = (barWidth / 100)
            val barColor = baseColor.copy(alpha = 0.4f + 0.6f * percentage)
            
            Text(
                text = plan.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Barra del gráfico
                Box(
                    modifier = Modifier
                        .weight(maxOf(0.01f, barWidth / 100))
                        .height(24.dp)
                        .background(barColor, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = formatDuration(planDuration),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
                
                // Espacio restante
                Spacer(
                    modifier = Modifier
                        .weight(maxOf(0.01f, 1 - barWidth / 100))
                        .padding(start = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * Gráfico de actividad semanal
 */
@Composable
fun WeeklyActivityChart(viewModel: StatsViewModel) {
    val dailyStats by viewModel.dailyStats.collectAsState()
    
    if (dailyStats.isEmpty()) {
        EmptyDataMessage("No hay datos disponibles para mostrar actividad semanal")
        return
    }
    
    // Obtener el día de la semana actual
    val calendar = Calendar.getInstance()
    val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    
    // Crear lista con nombres de días de la semana
    val dayNames = listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")
    
    // Mapear datos a días de la semana
    val plansPerDay = mutableMapOf<Int, Int>()
    for (i in Calendar.SUNDAY..Calendar.SATURDAY) {
        plansPerDay[i] = 0
    }
    
    // Convertir el mapa de fechas a días de la semana
    dailyStats.forEach { (dateStr, count) ->
        try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
            if (date != null) {
                val cal = Calendar.getInstance()
                cal.time = date
                val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                plansPerDay[dayOfWeek] = count
            }
        } catch (e: Exception) {
            // Ignorar fechas con formato incorrecto
        }
    }
    
    // Encontrar el máximo para escalado
    val maxPlansPerDay = plansPerDay.values.maxOrNull() ?: 1
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        // Mostrar gráfico de barras para cada día
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Mostrar barras para cada día de la semana
            for (dayOfWeek in Calendar.SUNDAY..Calendar.SATURDAY) {
                val count = plansPerDay[dayOfWeek] ?: 0
                val barHeight = if (maxPlansPerDay > 0) {
                    (count.toFloat() / maxPlansPerDay) * 100
                } else 0f
                
                val isToday = dayOfWeek == currentDayOfWeek
                
                DayActivityBar(
                    dayName = dayNames[dayOfWeek - 1],
                    barHeight = barHeight,
                    count = count,
                    isHighlighted = isToday,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Barra individual para el gráfico de actividad semanal
 */
@Composable
fun DayActivityBar(
    dayName: String,
    barHeight: Float,
    count: Int,
    isHighlighted: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodySmall
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Barra
        Box(
            modifier = Modifier
                .height(100.dp)
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Contenedor vacío de fondo
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                    )
            )
            
            // Barra real
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((barHeight * 0.01f * 100).dp)
                    .background(
                        if (isHighlighted) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                        RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                    )
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = dayName,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/**
 * Gráfico de distribución de planes por tema
 */
@Composable
fun SubjectDistributionChart(subjectDistribution: Map<String, Int>) {
    if (subjectDistribution.isEmpty()) {
        EmptyDataMessage("No hay datos disponibles para mostrar la distribución por temas")
        return
    }
    
    // Obtener los 5 temas más frecuentes
    val subjectCount = subjectDistribution
        .toList()
        .sortedByDescending { (_, count) -> count }
        .take(5)
    
    // Colores para cada tema
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.primaryContainer
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        subjectCount.forEachIndexed { index, (subject, count) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                // Indicador de color
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .height(16.dp)
                        .width(16.dp)
                        .background(colors[index % colors.size], RoundedCornerShape(4.dp))
                )
                
                // Nombre del tema
                Text(
                    text = subject,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                
                // Contador
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (index < subjectCount.size - 1) {
                Divider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
            }
        }
    }
}

/**
 * Tarjeta de logros
 */
@Composable
fun AchievementsCard(
    generalStats: Map<String, Any>,
    consecutiveDays: Int
) {
    val completedCount = generalStats["completedPlans"] as? Int ?: 0
    val perfectCount = generalStats["perfectCompletions"] as? Int ?: 0
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Tus logros",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            AchievementItem(
                title = "Planes completados",
                value = completedCount,
                target = 10,
                icon = Icons.Default.Checklist
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            AchievementItem(
                title = "Planes con 100% de completado",
                value = perfectCount,
                target = 5,
                icon = Icons.Default.Star
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            AchievementItem(
                title = "Días consecutivos de estudio",
                value = consecutiveDays,
                target = 7,
                icon = Icons.Default.CalendarMonth
            )
        }
    }
}

/**
 * Item individual de logro
 */
@Composable
fun AchievementItem(
    title: String,
    value: Int,
    target: Int,
    icon: ImageVector
) {
    val progress = (value.toFloat() / target).coerceIn(0f, 1f)
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSecondaryContainer
        )
        
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Barra de progreso
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(
                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f),
                        RoundedCornerShape(4.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(8.dp)
                        .background(
                            MaterialTheme.colorScheme.onSecondaryContainer,
                            RoundedCornerShape(4.dp)
                        )
                )
            }
        }
        
        Text(
            text = "$value/$target",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Mensaje para cuando no hay datos disponibles
 */
@Composable
fun EmptyDataMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Funciones de utilidad para cálculos
 */
private fun formatDuration(minutes: Int): String {
    return if (minutes >= 60) {
        "${minutes / 60}h ${minutes % 60}m"
    } else {
        "${minutes}m"
    }
}

private fun StudyPlan.completionDate(): Date? {
    return if (isCompleted) updatedAt.toDate() else null
}

private fun calculateConsecutiveDays(studyPlans: List<StudyPlan>): Int {
    if (studyPlans.isEmpty()) return 0
    
    // Obtener fechas de planes completados
    val completedDates = studyPlans
        .filter { it.isCompleted }
        .mapNotNull { it.completionDate() }
        .map {
            // Convertir a fecha sin hora
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            formatter.format(it)
        }
        .distinct()
        .sorted()
        
    if (completedDates.isEmpty()) return 0
    
    // Verificar si hay un plan completado hoy
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    
    // Si no hay estudio hoy, devolver 0
    if (completedDates.last() != today) return 0
    
    // Contar días consecutivos
    var consecutiveDays = 1
    
    for (i in completedDates.size - 2 downTo 0) {
        // Verificar si la fecha anterior es un día antes
        val cal1 = Calendar.getInstance().apply {
            time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(completedDates[i + 1])!!
            add(Calendar.DAY_OF_YEAR, -1)
        }
        val expectedPreviousDay = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal1.time)
        
        if (completedDates[i] == expectedPreviousDay) {
            consecutiveDays++
        } else {
            break
        }
    }
    
    return consecutiveDays
} 