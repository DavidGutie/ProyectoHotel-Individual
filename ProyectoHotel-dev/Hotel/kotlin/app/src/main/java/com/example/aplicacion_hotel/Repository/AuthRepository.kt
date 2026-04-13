package com.example.aplicacion_hotel.Repository

import com.example.aplicacion_hotel.Model.LoginRequest
import com.example.aplicacion_hotel.Model.LoginResponse
import com.example.aplicacion_hotel.Network.RetrofitInstance

class AuthRepository {

    suspend fun login(email: String, password: String): LoginResponse {
        return RetrofitInstance.api.login(
            LoginRequest(email, password)
        )
    }
}
