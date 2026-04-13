package com.example.aplicacion_hotel.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aplicacion_hotel.utils.HotelSessionManager

class HabitacionesViewModelFactory(
    private val sessionManager: HotelSessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HabitacionesViewModel(sessionManager) as T
    }
}