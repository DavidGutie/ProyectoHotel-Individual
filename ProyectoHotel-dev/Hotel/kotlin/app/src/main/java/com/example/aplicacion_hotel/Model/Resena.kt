package com.example.aplicacion_hotel.Model

import kotlinx.serialization.Serializable

@Serializable
data class Resena(
    val _id: String? = null,
    val clienteId: String,
    val reservaId: String,
    val habitacionId: String,
    val comentario: String,
    val puntuacion: Int,
    val fecha: String? = null
)

@Serializable
data class CrearResenaRequest(
    val clienteId: String,
    val reservaId: String,
    val habitacionId: String,
    val comentario: String,
    val puntuacion: Int
)
