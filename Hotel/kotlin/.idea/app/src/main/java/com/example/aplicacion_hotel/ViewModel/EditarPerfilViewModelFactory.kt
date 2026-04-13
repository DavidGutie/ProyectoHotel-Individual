package com.example.aplicacion_hotel.ViewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aplicacion_hotel.utils.HotelSessionManager

class EditarPerfilViewModelFactory(
    private val hotelSessionManager: HotelSessionManager,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditarPerfilViewModel(hotelSessionManager, context.applicationContext) as T
    }
}