package com.example.aplicacion_hotel.Repository

import com.example.aplicacion_hotel.Model.Cliente
import com.example.aplicacion_hotel.Network.RetrofitInstance

class ClienteRepository {

    suspend fun getClienteById(id: String): Cliente {
        return RetrofitInstance.api.getClienteById(id)
    }

    suspend fun crearCliente(cliente: Cliente): Cliente {
        return RetrofitInstance.api.crearCliente(cliente)
    }
}