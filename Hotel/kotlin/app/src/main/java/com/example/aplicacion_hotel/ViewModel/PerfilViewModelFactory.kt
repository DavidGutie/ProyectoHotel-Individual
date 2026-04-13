package com.example.aplicacion_hotel.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aplicacion_hotel.utils.HotelSessionManager

class PerfilViewModelFactory(
    private val hotelSessionManager: HotelSessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PerfilViewModel(hotelSessionManager) as T
    }
}