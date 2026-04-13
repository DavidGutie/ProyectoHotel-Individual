package com.example.aplicacion_hotel.View.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.aplicacion_hotel.View.screens.editarPerfil.EditarPerfilScreen
import com.example.aplicacion_hotel.View.screens.home.HomeScreen
import com.example.aplicacion_hotel.View.screens.infoHotel.InfoHotelScreen
import com.example.aplicacion_hotel.View.screens.login.LoginScreen
import com.example.aplicacion_hotel.View.screens.perfil.PerfilScreen
import com.example.aplicacion_hotel.View.screens.register.RegisterScreen
import com.example.aplicacion_hotel.View.screens.pago.PagoScreen
import com.example.aplicacion_hotel.View.screens.vip.VipScreen

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

        // --- RUTA CORREGIDA: Definimos los argumentos correctamente ---
        composable(
            route = Routes.Pago.route,
            arguments = listOf(
                navArgument("habitacionId") { type = NavType.StringType },
                navArgument("precioNoche") { type = NavType.FloatType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("habitacionId") ?: ""
            val precio = backStackEntry.arguments?.getFloat("precioNoche")?.toDouble() ?: 0.0

            // Llamamos a la nueva pantalla de Pago
            PagoScreen(navController, id, precio)
        }

        //IVAN
        composable(Routes.Vip.route) {
            VipScreen(navController)
        }
    }
}
