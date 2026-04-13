package com.example.aplicacion_hotel.Model

data class Habitacion(
    val _id: String,
    val numero: Int,
    val tipo: String,
    val descripcion: String,
    val imagen: String,
    val precionoche: Double,
    val rate: Double,
    val max_ocupantes: Int,
    val disponible: Boolean,
    val oferta: Boolean,
    val servicios: List<String>
)
