package com.example.studym8



import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyMateApp() {
    var selectedItem by remember { mutableStateOf(0) }
    val navItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Itinerary,
        BottomNavItem.Stats
    )

    Scaffold(
        topBar = {
            TopBarUserInfo(
                name = "Mauricio Muñiz",
                email = "mauricio09aguirre@gmail.com"
            )
        },
        bottomBar = {
            NavigationBar {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedItem) {
                0 -> HomeScreen()
                1 -> ItineraryScreen()
                2 -> StatsScreen()
            }
        }
    }
}

/**
 * Modelo para la navegación inferior
 */
sealed class BottomNavItem(
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
) {
    object Home : BottomNavItem("home", Icons.Default.Home, "Inicio")
    object Itinerary : BottomNavItem("itinerary", Icons.Default.CalendarMonth, "Itinerario")
    object Stats : BottomNavItem("stats", Icons.Default.BarChart, "Estadísticas")
}

/**
 * TopBar con la información del usuario
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarUserInfo(name: String, email: String) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "StudyMate de $name",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = email,
                    fontSize = 14.sp
                )
            }
        }
    )
}

/**
 * Pantalla principal (Home) que muestra el mockup:
 * - Sección "Mis itinerarios"
 * - Sección "Actividades Pendientes"
 */
@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Mis itinerarios",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Ejemplo de dos tarjetas de itinerarios
        ItineraryCard(
            title = "Programación Web",
            time = "Hoy a las 12:00 PM",
            chipText = "Estudios",
            chipColor = Color(0xFFB3E5FC) // Azul claro
        )
        Spacer(modifier = Modifier.height(8.dp))
        ItineraryCard(
            title = "Dieta Balanceada",
            time = "Hoy a las 01:00 PM",
            chipText = "Hábitos",
            chipColor = Color(0xFFC8E6C9) // Verde claro
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Actividades Pendientes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Fechas en forma de "chips"
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            DateChip("Feb 20", "Lun")
            DateChip("Feb 21", "Mar")
            DateChip("Feb 22", "Mié")
            DateChip("Feb 23", "Jue", selected = true)
            DateChip("Feb 24", "Vie")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filtros de tareas
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip("Todas", selected = true)
            FilterChip("To do", selected = false)
            FilterChip("En Progreso", selected = false)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tareas
        ActivityCard(
            title = "Centrar un DIV",
            project = "Itinerario Programación Web",
            status = "En Progreso"
        )
        Spacer(modifier = Modifier.height(8.dp))
        ActivityCard(
            title = "Crear un Header",
            project = "Itinerario Programación Web",
            status = "En Progreso"
        )
    }
}

/**
 * Tarjeta individual para un itinerario
 */
@Composable
fun ItineraryCard(
    title: String,
    time: String,
    chipText: String,
    chipColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = time, fontSize = 14.sp, color = Color.Gray)
            }
            Box(
                modifier = Modifier
                    .background(chipColor, shape = MaterialTheme.shapes.medium)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(text = chipText, fontSize = 14.sp)
            }
        }
    }
}

/**
 * Chip para fechas
 */
@Composable
fun DateChip(date: String, day: String, selected: Boolean = false) {
    val backgroundColor = if (selected) MaterialTheme.colorScheme.primary else Color(0xFFF5F5F5)
    val contentColor = if (selected) Color.White else Color.Black

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(48.dp)
            .background(backgroundColor, shape = MaterialTheme.shapes.medium)
            .padding(vertical = 4.dp)
    ) {
        Text(text = date, color = contentColor, fontSize = 12.sp)
        Text(text = day, color = contentColor, fontSize = 12.sp)
    }
}

/**
 * Chip para filtros
 */
@Composable
fun FilterChip(text: String, selected: Boolean) {
    val backgroundColor = if (selected) MaterialTheme.colorScheme.primary else Color(0xFFF5F5F5)
    val contentColor = if (selected) Color.White else Color.Black

    Box(
        modifier = Modifier
            .background(backgroundColor, shape = MaterialTheme.shapes.medium)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = text, color = contentColor, fontSize = 14.sp)
    }
}

/**
 * Tarjeta para una actividad pendiente
 */
@Composable
fun ActivityCard(
    title: String,
    project: String,
    status: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = project, fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))

            // Etiqueta de estado
            Box(
                modifier = Modifier
                    .background(Color(0xFFFFF9C4), shape = MaterialTheme.shapes.medium)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(text = status, fontSize = 14.sp)
            }
        }
    }
}

/**
 * Pantalla de Itinerario (placeholder)
 */
@Composable
fun ItineraryScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Pantalla de Itinerario")
    }
}

/**
 * Pantalla de Estadísticas (placeholder)
 */
@Composable
fun StatsScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Pantalla de Estadísticas")
    }
}

/**
 * Tema personalizado (puedes ajustarlo a tu paleta de colores)
 */
@Composable
fun StudyMateTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(),
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

/**
 * Definición rápida de un colorScheme claro
 */
@Composable
fun lightColorSchemeCustom() = lightColorScheme(
    primary = Color(0xFF6200EE),   // Morado principal
    secondary = Color(0xFF03DAC6)  // Turquesa
    // Puedes agregar más colores personalizados si es necesario
)
/**
 * Tipografía y shapes por defecto
 */
val Typography = Typography()
val Shapes = Shapes()

@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    StudyMateTheme {
        StudyMateApp()
    }
}