package com.example.aplicacion_hotel.Model

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val token: String,
    val usuario: Usuario
)
