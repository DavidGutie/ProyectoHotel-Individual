package com.example.aplicacion_hotel.Model

import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class Usuario(
    val id: String,
    val nombre: String,
    val email: String,
    val tipoUsuario: String,
)
