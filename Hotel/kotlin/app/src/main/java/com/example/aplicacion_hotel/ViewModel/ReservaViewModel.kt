package com.example.aplicacion_hotel.ViewModel

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacion_hotel.Model.CrearReservaRequest
import com.example.aplicacion_hotel.Model.Reserva
import com.example.aplicacion_hotel.Repository.ReservaRepository
import kotlinx.coroutines.launch

class ReservaViewModel : ViewModel() {

    private val repository = ReservaRepository()

    private val _reservaExitosa = mutableStateOf<Boolean?>(null)
    val reservaExitosa: State<Boolean?> = _reservaExitosa

    private val _reservas = mutableStateOf<List<Reserva>>(emptyList())
    val reservas: State<List<Reserva>> = _reservas

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun crearReserva(
        clienteId: String,
        habitacionId: String,
        fechaEntrada: String,
        fechaSalida: String,
        personas: Int,
        precioTotal: Double
    ) {
        viewModelScope.launch {
            try {
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
            } catch (e: Exception) {
                _reservaExitosa.value = false
                errorMessage = "Error al crear la reserva: ${e.message}"
            }
        }
    }

    fun cargarReservas(clienteIdLoggeado: String) {
        viewModelScope.launch {
            try {
                errorMessage = null
                val todasLasReservas = repository.obtenerReservasUsuario(clienteIdLoggeado)

                _reservas.value = todasLasReservas?.filter { reserva ->
                    reserva.clienteId == clienteIdLoggeado
                } ?: emptyList()

            } catch (e: Exception) {
                errorMessage = "Error al cargar las reservas: ${e.message}"
            }
        }
    }

    fun cancelarReserva(reservaId: String, clienteId: String) {
        viewModelScope.launch {
            try {
                val exito = repository.cancelarReserva(reservaId)
                if (exito) {
                    cargarReservas(clienteId) 
                } else {
                    errorMessage = "Error del servidor al cancelar"
                }
            } catch (e: Exception) {
                errorMessage = "Fallo de conexión: ${e.message}"
            }
        }
    }
}
