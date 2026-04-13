package com.example.aplicacion_hotel.View.navigation

sealed class Routes(val route: String) {
    object Pago : Routes("pago/{habitacionId}/{precioNoche}") {
        fun createRoute(habitacionId: String, precioNoche: Double) = "pago/$habitacionId/$precioNoche"
    }
    object Login : Routes("login")
    object Register : Routes("register")
    object Home : Routes("home")

    object Habitaciones : Routes("habitaciones")
    object Reservas : Routes("reservas")
    object Carrito : Routes("carrito")

    object Perfil : Routes("perfil")
    object EditarPerfil : Routes("editarPerfil")
    object InfoHotel : Routes("infoHotel")

    object Vip : Routes("vip")
}