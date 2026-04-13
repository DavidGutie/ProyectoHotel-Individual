package com.example.aplicacion_hotel.Repository

import com.example.aplicacion_hotel.Model.Cliente
import com.example.aplicacion_hotel.Network.RetrofitInstance
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ClienteRepository {

    suspend fun getClienteById(id: String): Cliente {
        return RetrofitInstance.api.getClienteById(id)
    }

    suspend fun crearCliente(cliente: Cliente): Cliente {
        return RetrofitInstance.api.crearCliente(cliente)
    }

    suspend fun actualizarCliente(
        id: String,
        nombre: RequestBody,
        dni: RequestBody,
        email: RequestBody,
        fechaNacimiento: RequestBody,
        sexo: RequestBody,
        ciudad: RequestBody,
        vip: RequestBody,
        foto: MultipartBody.Part?
    ): Cliente {
        return RetrofitInstance.api.actualizarCliente(
            id, nombre, dni, email, fechaNacimiento, sexo, ciudad, vip, foto
        )
    }
}