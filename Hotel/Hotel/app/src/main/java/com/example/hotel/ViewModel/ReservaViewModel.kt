package com.example.hotel.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.hotel.Model.CrearReservaRequest
import com.example.hotel.Repository.ReservaRepository
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State

class ReservaViewModel : ViewModel() {

    private val repository = ReservaRepository()

    private val _reservaExitosa = mutableStateOf<Boolean?>(null)
    val reservaExitosa: State<Boolean?> = _reservaExitosa

    fun crearReserva(
        clienteId: String,
        habitacionId: String,
        fechaEntrada: String,
        fechaSalida: String,
        personas: Int,
        precioTotal: Double
    ) {
        viewModelScope.launch {
            val request = CrearReservaRequest(
                clienteId,
                habitacionId,
                fechaEntrada,
                fechaSalida,
                personas,
                precioTotal
            )

            val resultado = repository.crearReserva(request)
            _reservaExitosa.value = resultado
        }
    }
}