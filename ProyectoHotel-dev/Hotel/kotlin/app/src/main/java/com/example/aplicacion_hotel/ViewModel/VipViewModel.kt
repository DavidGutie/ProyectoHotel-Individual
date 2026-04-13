package com.example.aplicacion_hotel.ViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacion_hotel.Repository.ClienteRepository
import com.example.aplicacion_hotel.utils.HotelSessionManager
import com.example.aplicacion_hotel.utils.MultipartUtils
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Locale

class VipViewModel(
    private val session: HotelSessionManager
) : ViewModel() {

    private val repo = ClienteRepository()

    var isLoading by mutableStateOf(false)
        private set

    var successDialog by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun esVip(): Boolean {
        return session.getCliente()?.vip == true
    }

    fun hacerVip() {
        val cliente = session.getCliente()
        if (cliente == null) {
            errorMessage = "No hay sesión iniciada"
            return
        }
        if (cliente.vip) return
        val fechaOk = normalizeToDdMmYyyy(cliente.fechaNacimiento)

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val actualizado = repo.actualizarCliente(
                    id = cliente._id ?: return@launch,
                    nombre = cliente.nombre.trim().toRequestBody("text/plain".toMediaType()),
                    dni = cliente.dni.trim().toRequestBody("text/plain".toMediaType()),
                    email = cliente.email.trim().toRequestBody("text/plain".toMediaType()),
                    fechaNacimiento = fechaOk.toRequestBody("text/plain".toMediaType()),
                    sexo = cliente.sexo.trim().toRequestBody("text/plain".toMediaType()),
                    ciudad = cliente.ciudad.trim().toRequestBody("text/plain".toMediaType()),
                    vip = "true".toRequestBody("text/plain".toMediaType()),
                    foto = null
                )

                session.saveCliente(actualizado)
                successDialog = true

            } catch (e: Exception) {
                errorMessage = e.message ?: e.toString()
            } finally {
                isLoading = false
            }
        }
    }

    fun cerrarDialog() {
        successDialog = false
    }

    private fun normalizeToDdMmYyyy(input: String): String {
        val ddmmyyyy = Regex("""\d{2}/\d{2}/\d{4}""")
        if (ddmmyyyy.matches(input)) return input

        return try {
            val isoFormats = listOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd"
            )

            val parser = isoFormats.firstNotNullOfOrNull { pattern ->
                try {
                    java.text.SimpleDateFormat(pattern, Locale.getDefault()).apply {
                        timeZone = java.util.TimeZone.getTimeZone("UTC")
                    }
                } catch (_: Exception) { null }
            }

            val date = parser?.parse(input)
            if (date != null) {
                java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
            } else {
                "" 
            }
        } catch (_: Exception) {
            ""
        }
    }
}