package com.example.aplicacion_hotel.View.screens.home

import View.screens.reservas.ReservasScreen
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.aplicacion_hotel.View.navigation.Routes
import androidx.navigation.compose.rememberNavController
import com.example.aplicacion_hotel.View.Scaffold.HotelTopBar
import com.example.aplicacion_hotel.View.navigation.BottomNavigationBar
import com.example.aplicacion_hotel.View.screens.carrito.CarritoScreen
import com.example.aplicacion_hotel.View.screens.habitaciones.HabitacionesScreen



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
                HabitacionesScreen()
            }

            composable(Routes.Reservas.route) {
                ReservasScreen("1")
            }

            composable(Routes.Carrito.route) {
                CarritoScreen()
            }
        }
    }
}
