package com.example.aplicacion_hotel.Network

import com.example.aplicacion_hotel.Model.Cliente
import com.example.aplicacion_hotel.Model.Habitacion
import com.example.aplicacion_hotel.Model.LoginRequest
import com.example.aplicacion_hotel.Model.LoginResponse
import com.example.hotel.Model.CrearReservaRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {


    @POST("reservas")
    suspend fun crearReserva(
        @Body reserva: CrearReservaRequest
    ): Response<Unit>

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse


    @GET("habitaciones")
    suspend fun getHabitaciones(): List<Habitacion>

    @GET("clientes/{id}")
    suspend fun getClienteById(
        @Path("id") id: String
    ): Cliente

    @POST("clientes")
    suspend fun crearCliente(
        @Body cliente: Cliente
    ): Cliente
}