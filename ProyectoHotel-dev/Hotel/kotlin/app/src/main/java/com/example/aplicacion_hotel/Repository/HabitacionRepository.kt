package com.example.aplicacion_hotel.Repository

import com.example.aplicacion_hotel.Model.Habitacion
import com.example.aplicacion_hotel.Network.RetrofitInstance

class HabitacionRepository {

    private val api = RetrofitInstance.api

    suspend fun getHabitaciones(): List<Habitacion> {
        return api.getHabitaciones()
    }
}