package com.example.aplicacion_hotel.ViewModel

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacion_hotel.Model.CrearReservaRequest
import com.example.aplicacion_hotel.Model.Reserva
import com.example.aplicacion_hotel.Model.ReservaAuditEntry
import com.example.aplicacion_hotel.Repository.ReservaRepository
import kotlinx.coroutines.launch
import okhttp3.ResponseBody

class ReservaViewModel : ViewModel() {

    private val repository = ReservaRepository()

    private val _reservaExitosa = mutableStateOf<Boolean?>(null)
    val reservaExitosa: State<Boolean?> = _reservaExitosa

    private val _reservas = mutableStateOf<List<Reserva>>(emptyList())
    val reservas: State<List<Reserva>> = _reservas

    private val _historiales = mutableStateOf<Map<String, List<String>>>(emptyMap())
    val historiales: State<Map<String, List<String>>> = _historiales

    private val _facturaDescargada = mutableStateOf<Pair<String, ResponseBody>?>(null)
    val facturaDescargada: State<Pair<String, ResponseBody>?> = _facturaDescargada

    private val _descargandoFactura = mutableStateOf<String?>(null)
    val descargandoFactura: State<String?> = _descargandoFactura

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun crearReserva(
        clienteId: String,
        habitacionId: String,
        fechaEntrada: String,
        fechaSalida: String,
        personas: Int,
        precioTotal: Double,
        mascotas: Int = 0
    ) {
        viewModelScope.launch {
            try {
                val request = CrearReservaRequest(
                    clienteId,
                    habitacionId,
                    fechaEntrada,
                    fechaSalida,
                    personas,
                    precioTotal,
                    mascotas
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

                _reservas.value.forEach { reserva ->
                    cargarHistorialReserva(reserva)
                }
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
                errorMessage = "Fallo de conexion: ${e.message}"
            }
        }
    }

    fun cargarHistorialReserva(reserva: Reserva) {
        viewModelScope.launch {
            val historial = repository.obtenerHistorialReserva(reserva._id)
                ?.mapNotNull { it.toTextoCliente() }
                ?.ifEmpty { null }
                ?: historialLocal(reserva)

            _historiales.value = _historiales.value + (reserva._id to historial)
        }
    }

    fun descargarFactura(reservaId: String) {
        viewModelScope.launch {
            try {
                _descargandoFactura.value = reservaId
                errorMessage = null
                val body = repository.descargarFactura(reservaId)
                if (body != null) {
                    _facturaDescargada.value = reservaId to body
                } else {
                    errorMessage = "No se pudo generar la factura"
                }
            } catch (e: Exception) {
                errorMessage = "Error al descargar factura: ${e.message}"
            } finally {
                _descargandoFactura.value = null
            }
        }
    }

    fun facturaProcesada() {
        _facturaDescargada.value = null
    }

    private fun ReservaAuditEntry.toTextoCliente(): String? {
        return when (action.trim().lowercase()) {
            "created", "create", "reserva_creada", "booking_created" -> "Reserva creada"
            "payment_received", "paid", "pago_recibido" -> "Pago recibido"
            "check_in", "checkin", "checked_in" -> "Check-in realizado"
            "service_added", "servicio_anadido" -> "Servicio anadido"
            "cancelled", "canceled", "reserva_cancelada" -> "Reserva cancelada"
            else -> action.takeIf { it.isNotBlank() }?.replaceFirstChar { it.uppercase() }
        }
    }

    private fun historialLocal(reserva: Reserva): List<String> {
        val acciones = mutableListOf("Reserva creada", "Pago recibido")
        if (reserva.cancelacion) {
            acciones += "Reserva cancelada"
        }
        return acciones
    }
}
