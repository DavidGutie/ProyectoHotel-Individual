package com.example.aplicacion_hotel.Model

data class CrearReservaRequest(
    val clienteId: String,
    val habitacionId: String,
    val fechaEntrada: String,
    val fechaSalida: String,
    val personas: Int,
    val precioTotal: Double
)