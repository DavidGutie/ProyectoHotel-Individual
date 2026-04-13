package com.example.aplicacion_hotel.View.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun BottomNavigationBar(navController: NavController) {

    NavigationBar {

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(Routes.Habitaciones.route) },
            label = { Text("Habitaciones") },
            icon = { Icon(Icons.Default.Home, null) }
        )

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(Routes.Reservas.route) },
            label = { Text("Reservas") },
            icon = { Icon(Icons.Default.Star, null) }
        )

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(Routes.Vip.route) },
            label = { Text("VIP") },
            icon = { Icon(Icons.Default.Star, null) }
        )
    }
}
