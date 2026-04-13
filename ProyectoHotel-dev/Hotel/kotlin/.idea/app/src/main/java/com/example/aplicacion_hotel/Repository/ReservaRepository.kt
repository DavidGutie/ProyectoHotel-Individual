package com.example.aplicacion_hotel.Repository

import com.example.aplicacion_hotel.Model.CrearReservaRequest
import com.example.aplicacion_hotel.Model.Reserva
import com.example.aplicacion_hotel.Network.RetrofitInstance

class ReservaRepository {

    private val api = RetrofitInstance.api

    suspend fun obtenerReservasUsuario(clienteId: String): List<Reserva>? {
        return try {
            val response = api.obtenerReservasUsuario(clienteId)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun crearReserva(request: CrearReservaRequest): Boolean {
        return try {
            val response = api.crearReserva(request)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun cancelarReserva(id: String): Boolean {
        return try {
            val response = api.cancelarReserva(id)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}

