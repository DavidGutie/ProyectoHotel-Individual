package com.example.aplicacion_hotel.ViewModel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacion_hotel.Model.Habitacion
import com.example.aplicacion_hotel.Repository.HabitacionRepository
import com.example.aplicacion_hotel.utils.HotelSessionManager
import kotlinx.coroutines.launch

class CarritoViewModel(
    private val sessionManager: HotelSessionManager,
    private val repository: HabitacionRepository = HabitacionRepository()
) : ViewModel() {

    private val _habitacionesCarrito = mutableStateOf<List<Habitacion>>(emptyList())
    val habitacionesCarrito: State<List<Habitacion>> = _habitacionesCarrito

    private val _cargando = mutableStateOf(false)
    val cargando: State<Boolean> = _cargando

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    fun cargarCarrito() {
        viewModelScope.launch {
            try {
                _cargando.value = true
                _error.value = null

                val ids = sessionManager.getCarritoIds()
                val todas = repository.getHabitaciones()
                _habitacionesCarrito.value = todas.filter { ids.contains(it._id) }

            } catch (e: Exception) {
                _error.value = "Error al cargar carrito: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun eliminarDelCarrito(idHabitacion: String) {
        sessionManager.toggleCarrito(idHabitacion)
        cargarCarrito()
    }

    fun vaciarCarrito() {
        sessionManager.clearCarrito()
        cargarCarrito()
    }
}