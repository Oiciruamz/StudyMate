package com.example.studym8.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.example.studym8.ui.navigation.NavigationRoutes

/**
 * Componente de barra de navegación inferior para la aplicación StudyMate
 */
@Composable
fun StudyMateBottomNavigation(
    currentRoute: String,
    onRouteSelected: (String) -> Unit
) {
    // Lista de destinos para la barra de navegación
    val items = remember {
        listOf(
            BottomNavItem(
                route = NavigationRoutes.HOME,
                title = "Inicio",
                icon = Icons.Default.Home
            ),
            BottomNavItem(
                route = NavigationRoutes.ITINERARY,
                title = "Itinerarios",
                icon = Icons.Default.CalendarMonth
            ),
            BottomNavItem(
                route = NavigationRoutes.STATS,
                title = "Estadísticas",
                icon = Icons.Default.BarChart
            )
        )
    }
    
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                selected = selected,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                onClick = {
                    if (!selected) {
                        onRouteSelected(item.route)
                    }
                }
            )
        }
    }
}

/**
 * Clase para representar un elemento de la barra de navegación inferior
 */
data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) 