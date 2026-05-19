package com.example.aplicacion_hotel.Model

data class Reserva(
    val _id: String,
    val clienteId: String,
    val habitacionId: String,
    val fechaEntrada: String,
    val fechaSalida: String,
    val personas: Int,
    val mascotas: Int = 0,
    val suplementoMascotas: Double = 0.0,
    val with_pet: Boolean = false,
    val pet_supplement_total: Double = 0.0,
    val precioTotal: Double,
    val cancelacion: Boolean
) {
    val suplementoMascotaReserva: Double
        get() = listOf(suplementoMascotas, pet_supplement_total).firstOrNull { it > 0 } ?: 0.0
}
