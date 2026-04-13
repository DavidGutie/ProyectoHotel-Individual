package com.example.aplicacion_hotel.ViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacion_hotel.Model.Cliente
import com.example.aplicacion_hotel.Repository.AuthRepository
import com.example.aplicacion_hotel.Repository.ClienteRepository
import com.example.aplicacion_hotel.utils.SessionManager
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    private val clienteRepository = ClienteRepository()
    private val authRepository = AuthRepository()

    var registerSuccess by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun register(
        nombre: String,
        dni: String,
        email: String,
        password: String,
        fechaNacimiento: String,
        sexo: String,
        ciudad: String
    ) {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null


                // 1️⃣ Crear cliente
                val nuevoCliente = Cliente(
                    nombre = nombre,
                    dni = dni,
                    email = email,
                    password = password,
                    fechaNacimiento = fechaNacimiento,
                    sexo = sexo,
                    ciudad = ciudad,
                    vip = false
                )

                clienteRepository.crearCliente(nuevoCliente)

                // 2️⃣ Login automático
                val loginResponse = authRepository.login(email, password)

                if (loginResponse.usuario.tipoUsuario != "Cliente") {
                    errorMessage = "Error al iniciar sesión automática"
                    return@launch
                }

                // 3️⃣ Guardar token
                sessionManager.saveToken(loginResponse.token)

                // 4️⃣ Obtener cliente completo
                val clienteCompleto = clienteRepository.getClienteById(
                    loginResponse.usuario.id
                )

                sessionManager.saveCliente(clienteCompleto)

                registerSuccess = true

            } catch (e: Exception) {
                errorMessage = e.message
            }finally {
                isLoading = false
            }
        }
    }
}