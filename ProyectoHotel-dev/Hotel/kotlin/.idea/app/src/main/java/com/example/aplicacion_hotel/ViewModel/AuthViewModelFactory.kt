package com.example.aplicacion_hotel.ViewModel

import AuthViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aplicacion_hotel.utils.HotelSessionManager

class AuthViewModelFactory(
    private val sessionManager: HotelSessionManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AuthViewModel(sessionManager) as T
    }
}
