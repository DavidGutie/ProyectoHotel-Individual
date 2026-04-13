package com.example.hotel.ViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacion_hotel.Repository.AuthRepository
import com.example.aplicacion_hotel.Repository.ClienteRepository
import kotlinx.coroutines.launch

class AuthViewModel(
    private val sessionManager: com.example.aplicacion_hotel.utils.SessionManager
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    private val repository = AuthRepository()

    var loginSuccess by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val clienteRepository = ClienteRepository()

    fun login(email: String, password: String) {

        viewModelScope.launch {

            try {
                isLoading = true
                errorMessage = null

                val response = repository.login(email, password)

                if (response.usuario.tipoUsuario != "Cliente") {
                    errorMessage = "Solo los clientes pueden iniciar sesión"
                    return@launch
                }

                // Guardar token
                sessionManager.saveToken(response.token)

                // Obtener cliente completo usando ID
                val clienteCompleto = clienteRepository.getClienteById(
                    response.usuario.id
                )

                // Guardar cliente completo
                sessionManager.saveCliente(clienteCompleto)

                loginSuccess = true

            } catch (e: Exception) {
                errorMessage = e.message
            }finally {
                isLoading = false
            }
        }
    }
}
