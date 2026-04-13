package com.example.aplicacion_hotel.Model

import kotlinx.serialization.Serializable

@Serializable
data class Cliente(
    val _id: String? = null,
    val nombre: String,
    val dni: String,
    val email: String,
    val password: String? = null,
    val fechaNacimiento: String,
    val sexo: String,
    val foto: String? = null,
    val ciudad: String,
    val vip: Boolean
)