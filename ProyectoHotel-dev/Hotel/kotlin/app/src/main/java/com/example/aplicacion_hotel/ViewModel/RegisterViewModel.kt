package com.example.aplicacion_hotel.ViewModel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacion_hotel.Model.Cliente
import com.example.aplicacion_hotel.Model.Reserva
import com.example.aplicacion_hotel.Repository.AuthRepository
import com.example.aplicacion_hotel.Repository.ClienteRepository
import com.example.aplicacion_hotel.Repository.ReservaRepository
import com.example.aplicacion_hotel.utils.HotelSessionManager
import com.example.aplicacion_hotel.utils.httpErrorMessage
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class RegisterViewModel(
    private val hotelSessionManager: HotelSessionManager
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var registerSuccess by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val clienteRepository = ClienteRepository()
    private val authRepository = AuthRepository()
    private val reservaRepository = ReservaRepository()

    private val _reservas = mutableStateOf<List<Reserva>>(emptyList())
    val reservas: State<List<Reserva>> = _reservas

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
            isLoading = true
            errorMessage = null
            registerSuccess = false

            try {
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
                val loginResponse = authRepository.login(email, password)

                if (loginResponse.usuario.tipoUsuario != "Cliente") {
                    errorMessage = "Solo los clientes pueden iniciar sesión"
                    return@launch
                }
                hotelSessionManager.saveToken(loginResponse.token)
                val clienteCompleto = clienteRepository.getClienteById(loginResponse.usuario.id)
                hotelSessionManager.saveCliente(clienteCompleto)

                registerSuccess = true

            } catch (e: HttpException) {
                errorMessage = httpErrorMessage(e)
            } catch (e: IOException) {
                errorMessage = "Error de red. Revisa tu conexión."
            } catch (e: Exception) {
                errorMessage = "Error inesperado: ${e.message ?: e.toString()}"
            } finally {
                isLoading = false
            }
        }
    }

    fun cargarReservas(clienteIdLoggeado: String) {
        viewModelScope.launch {
            try {
                errorMessage = null
                val todas = reservaRepository.obtenerReservasUsuario(clienteIdLoggeado)

                _reservas.value = todas?.filter { it.clienteId == clienteIdLoggeado } ?: emptyList()

            } catch (e: HttpException) {
                errorMessage = httpErrorMessage(e)
            } catch (e: IOException) {
                errorMessage = "Error de red al cargar reservas."
            } catch (e: Exception) {
                errorMessage = "Error al cargar reservas: ${e.message ?: e.toString()}"
            }
        }
    }
}
