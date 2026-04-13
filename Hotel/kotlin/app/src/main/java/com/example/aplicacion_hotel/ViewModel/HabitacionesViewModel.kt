package com.example.aplicacion_hotel.ViewModel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacion_hotel.Model.Habitacion
import com.example.aplicacion_hotel.Model.Reserva
import com.example.aplicacion_hotel.Network.RetrofitInstance
import com.example.aplicacion_hotel.Repository.HabitacionRepository
import com.example.aplicacion_hotel.utils.HotelSessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HabitacionesViewModel(
    private val sessionManager: HotelSessionManager,
    private val repository: HabitacionRepository = HabitacionRepository()
) : ViewModel() {

    private val _habitaciones = mutableStateOf<List<Habitacion>>(emptyList())
    val habitaciones: State<List<Habitacion>> = _habitaciones

    private val _cargando = mutableStateOf(false)
    val cargando: State<Boolean> = _cargando

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _idsCarrito = mutableStateOf(sessionManager.getCarritoIds())
    val idsCarrito: State<Set<String>> = _idsCarrito

    fun cargarHabitaciones(soloDisponibles: Boolean = true) {
        viewModelScope.launch {
            try {
                _cargando.value = true
                _error.value = null

                val todas = repository.getHabitaciones()
                _habitaciones.value = if (soloDisponibles) {
                    todas.filter { it.disponible }
                } else {
                    todas
                }

                _idsCarrito.value = sessionManager.getCarritoIds()

            } catch (e: Exception) {
                _error.value = "Error al cargar habitaciones: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun buscarDisponibles(fechaEntrada: String, fechaSalida: String) {
        viewModelScope.launch {
            try {
                _cargando.value = true
                _error.value = null
                val rooms = repository.getHabitaciones()
                
                val resResponse = RetrofitInstance.api.obtenerTodasLasReservas()
                val allRes = if (resResponse.isSuccessful) resResponse.body() ?: emptyList() else emptyList()

                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val start = sdf.parse(fechaEntrada)
                val end = sdf.parse(fechaSalida)

                if (start == null || end == null || end.before(start)) {
                    _error.value = "Rango de fechas inválido"
                    return@launch
                }

                _habitaciones.value = rooms.filter { hab ->
                    val reservasDeEstaHab = allRes.filter { it.habitacionId == hab._id && !it.cancelacion }

                    val solapada = reservasDeEstaHab.any { res ->
                        val resStart = sdf.parse(res.fechaEntrada)
                        val resEnd = sdf.parse(res.fechaSalida)
                        
                        if (resStart != null && resEnd != null) {
                            !(end <= resStart || start >= resEnd)
                        } else false
                    }
                    !solapada
                }
            } catch (e: Exception) {
                _error.value = "Error al buscar: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun toggleCarrito(idHabitacion: String) {
        _idsCarrito.value = sessionManager.toggleCarrito(idHabitacion)
    }
}
