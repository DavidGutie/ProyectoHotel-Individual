package com.example.hotel.Repository
import com.example.aplicacion_hotel.Network.RetrofitInstance
import com.example.hotel.Model.CrearReservaRequest


class ReservaRepository {

    private val api = RetrofitInstance.api

    suspend fun crearReserva(reserva: CrearReservaRequest): Boolean {
        return try {
            val response = api.crearReserva(reserva)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}