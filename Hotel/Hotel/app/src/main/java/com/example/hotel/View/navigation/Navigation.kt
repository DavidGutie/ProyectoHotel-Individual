package com.example.aplicacion_hotel.View.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aplicacion_hotel.View.screens.editarPerfil.EditarPerfilScreen
import com.example.aplicacion_hotel.View.screens.home.HomeScreen
import com.example.aplicacion_hotel.View.screens.infoHotel.InfoHotelScreen
import com.example.aplicacion_hotel.View.screens.login.LoginScreen
import com.example.aplicacion_hotel.View.screens.perfil.PerfilScreen
import com.example.aplicacion_hotel.View.screens.register.RegisterScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Login.route
    ) {

        composable(Routes.Login.route) {
            LoginScreen(navController)
        }

        composable(Routes.Register.route) {
            RegisterScreen(navController)
        }

        composable(Routes.Home.route) {
            HomeScreen(navController)
        }

        composable(Routes.Perfil.route) {
            PerfilScreen(navController)
        }

        composable(Routes.EditarPerfil.route) {
            EditarPerfilScreen(navController)
        }

        composable(Routes.InfoHotel.route) {
            InfoHotelScreen()
        }
    }
}