package com.example.aplicacion_hotel.Model

data class ReservaAuditEntry(
    val _id: String? = null,
    val booking_id: String? = null,
    val bookingId: String? = null,
    val action: String = "",
    val actor_id: String? = null,
    val actorId: String? = null,
    val actor_type: String? = null,
    val actorType: String? = null,
    val timestamp: String? = null,
    val createdAt: String? = null
)
