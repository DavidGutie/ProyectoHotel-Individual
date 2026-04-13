package com.example.aplicacion_hotel.ViewModel

import com.example.hotel.ViewModel.AuthViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aplicacion_hotel.utils.SessionManager

class AuthViewModelFactory(
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AuthViewModel(sessionManager) as T
    }
}
