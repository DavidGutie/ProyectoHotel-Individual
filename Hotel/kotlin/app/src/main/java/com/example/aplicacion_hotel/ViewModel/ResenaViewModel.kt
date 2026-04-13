package com.example.aplicacion_hotel.ViewModel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacion_hotel.Model.CrearResenaRequest
import com.example.aplicacion_hotel.Repository.ResenaRepository
import kotlinx.coroutines.launch

class ResenaViewModel : ViewModel() {
    private val repository = ResenaRepository()

    private val _resenaEnviada = mutableStateOf<Boolean?>(null)
    val resenaEnviada: State<Boolean?> = _resenaEnviada

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    fun enviarResena(clienteId: String, reservaId: String, habitacionId: String, comentario: String, puntuacion: Int) {
        viewModelScope.launch {
            _errorMessage.value = null
            val request = CrearResenaRequest(clienteId, reservaId, habitacionId, comentario, puntuacion)
            val exito = repository.crearResena(request)

            if (exito) {
                _resenaEnviada.value = true
            } else {
                _resenaEnviada.value = false
                _errorMessage.value = "Error al enviar la reseña"
            }
        }
    }

    fun resetEstado() {
        _resenaEnviada.value = null
        _errorMessage.value = null
    }
}
