package com.example.aplicacion_hotel.Repository

import com.example.aplicacion_hotel.Model.CrearReservaRequest
import com.example.aplicacion_hotel.Model.Reserva
import com.example.aplicacion_hotel.Model.ReservaAuditEntry
import com.example.aplicacion_hotel.Network.RetrofitInstance
import okhttp3.ResponseBody

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

    suspend fun obtenerHistorialReserva(id: String): List<ReservaAuditEntry>? {
        return try {
            val response = api.obtenerHistorialReserva(id)
            if (response.isSuccessful) response.body() else obtenerHistorialBooking(id)
        } catch (e: Exception) {
            obtenerHistorialBooking(id)
        }
    }

    private suspend fun obtenerHistorialBooking(id: String): List<ReservaAuditEntry>? {
        return try {
            val response = api.obtenerHistorialBooking(id)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun descargarFactura(id: String): ResponseBody? {
        return try {
            val response = api.descargarFacturaReserva(id)
            if (response.isSuccessful) response.body() else descargarFacturaBooking(id)
        } catch (e: Exception) {
            descargarFacturaBooking(id)
        }
    }

    private suspend fun descargarFacturaBooking(id: String): ResponseBody? {
        return try {
            val response = api.descargarFacturaBooking(id)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }
}
