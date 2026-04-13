package com.example.aplicacion_hotel.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aplicacion_hotel.utils.HotelSessionManager

class RegisterViewModelFactory(
    private val hotelSessionManager: HotelSessionManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RegisterViewModel(hotelSessionManager) as T
    }
}