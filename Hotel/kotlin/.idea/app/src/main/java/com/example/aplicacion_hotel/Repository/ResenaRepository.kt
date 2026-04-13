package com.example.aplicacion_hotel.Repository

import com.example.aplicacion_hotel.Model.CrearResenaRequest
import com.example.aplicacion_hotel.Model.Resena
import com.example.aplicacion_hotel.Network.RetrofitInstance

class ResenaRepository {
    private val api = RetrofitInstance.api

    suspend fun obtenerResenas(): List<Resena> {
        return try {
            api.obtenerResenas()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun crearResena(request: CrearResenaRequest): Boolean {
        return try {
            val response = api.crearResena(request)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}
