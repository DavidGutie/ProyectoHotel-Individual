package com.example.aplicacion_hotel.Repository

import com.example.aplicacion_hotel.Model.Habitacion
import com.example.aplicacion_hotel.Network.RetrofitInstance

class HabitacionRepository {

    private val api = RetrofitInstance.api

    suspend fun getHabitaciones(
        admiteMascotas: Boolean = false,
        amenities: List<String> = emptyList()
    ): List<Habitacion> {
        return api.getHabitaciones(
            pets = true.takeIf { admiteMascotas },
            amenities = amenities.joinToString(",").takeIf { it.isNotBlank() }
        )
    }

    suspend fun getAmenities() = api.getAmenities()

    suspend fun getPoliticaMascotas(habitacionId: String) = api.getPoliticaMascotas(habitacionId)
}
