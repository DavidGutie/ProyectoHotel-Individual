package com.example.aplicacion_hotel.View.screens.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aplicacion_hotel.View.Scaffold.HotelTopBar
import com.example.aplicacion_hotel.View.navigation.BottomNavigationBar
import com.example.aplicacion_hotel.View.navigation.Routes
import com.example.aplicacion_hotel.View.screens.carrito.CarritoScreen
import com.example.aplicacion_hotel.View.screens.habitaciones.HabitacionesScreen
import com.example.aplicacion_hotel.View.screens.reservas.ReservasScreen
import com.example.aplicacion_hotel.View.screens.vip.VipScreen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    val bottomNavController = rememberNavController()

    Scaffold(
        topBar = {
            HotelTopBar(navController)
        },
        bottomBar = {
            BottomNavigationBar(bottomNavController)
        }
    ) { padding ->

        NavHost(
            navController = bottomNavController,
            startDestination = Routes.Habitaciones.route,
            modifier = Modifier.padding(padding)
        ) {

            composable(Routes.Habitaciones.route) {
                // Pasamos el navController principal para poder navegar al Pago
                HabitacionesScreen(navController)
            }

            composable(Routes.Reservas.route) {
                ReservasScreen()
            }

            composable(Routes.Vip.route) {
                VipScreen(navController)
            }
        }
    }
}
