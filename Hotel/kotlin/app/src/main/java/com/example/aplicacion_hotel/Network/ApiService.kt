package com.example.aplicacion_hotel.Network

import com.example.aplicacion_hotel.Model.Cliente
import com.example.aplicacion_hotel.Model.Habitacion
import com.example.aplicacion_hotel.Model.LoginRequest
import com.example.aplicacion_hotel.Model.LoginResponse
import com.example.aplicacion_hotel.Model.CrearReservaRequest
import com.example.aplicacion_hotel.Model.Amenity
import com.example.aplicacion_hotel.Model.PetPolicy
import com.example.aplicacion_hotel.Model.Reserva
import com.example.aplicacion_hotel.Model.Resena
import com.example.aplicacion_hotel.Model.CrearResenaRequest
import com.example.aplicacion_hotel.Model.ReservaAuditEntry
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

    @GET("habitaciones")
    suspend fun getHabitaciones(
        @Query("pets") pets: Boolean? = null,
        @Query("amenities") amenities: String? = null
    ): List<Habitacion>

    @GET("amenities")
    suspend fun getAmenities(): List<Amenity>

    @GET("habitaciones/{id}/mascotas")
    suspend fun getPoliticaMascotas(
        @Path("id") id: String
    ): PetPolicy

    @GET("cliente/{id}")
    suspend fun getClienteById(
        @Path("id") id: String
    ): Cliente

    @POST("cliente")
    suspend fun crearCliente(
        @Body cliente: Cliente
    ): Cliente

    @POST("reservas")
    suspend fun crearReserva(
        @Body reserva: CrearReservaRequest
    ): Response<Unit>

    @GET("reservas")
    suspend fun obtenerTodasLasReservas(): Response<List<Reserva>>

    @GET("reservas")
    suspend fun obtenerReservasUsuario(
        @Query("clienteId") clienteId: String
    ): Response<List<Reserva>>

    @PUT("reservas/{id}/cancelar")
    suspend fun cancelarReserva(
        @Path("id") id: String
    ): Response<Reserva>

    @GET("reservas/{id}/audit")
    suspend fun obtenerHistorialReserva(
        @Path("id") id: String
    ): Response<List<ReservaAuditEntry>>

    @GET("bookings/{id}/audit")
    suspend fun obtenerHistorialBooking(
        @Path("id") id: String
    ): Response<List<ReservaAuditEntry>>

    @GET("reservas/{id}/invoice")
    suspend fun descargarFacturaReserva(
        @Path("id") id: String
    ): Response<ResponseBody>

    @GET("bookings/{id}/invoice")
    suspend fun descargarFacturaBooking(
        @Path("id") id: String
    ): Response<ResponseBody>

    @Multipart
    @PUT("cliente/{id}")
    suspend fun actualizarCliente(
        @Path("id") id: String,
        @Part("nombre") nombre: RequestBody,
        @Part("dni") dni: RequestBody,
        @Part("email") email: RequestBody,
        @Part("fechaNacimiento") fechaNacimiento: RequestBody,
        @Part("sexo") sexo: RequestBody,
        @Part("ciudad") ciudad: RequestBody,
        @Part("vip") vip: RequestBody,
        @Part foto: MultipartBody.Part? = null
    ): Cliente

    @GET("resenas")
    suspend fun obtenerResenas(): List<Resena>

    @POST("resenas")
    suspend fun crearResena(
        @Body resena: CrearResenaRequest
    ): Response<Resena>
}
