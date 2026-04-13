package com.example.aplicacion_hotel.Model

data class Reserva(
    val _id: String,
    val clienteId: String, // Añadido para poder filtrar en la App
    val habitacionId: String,
    val fechaEntrada: String,
    val fechaSalida: String,
    val personas: Int,
    val precioTotal: Double,
    val cancelacion: Boolean
)
