package com.example.aplicacion_hotel.ViewModel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacion_hotel.Repository.ClienteRepository
import com.example.aplicacion_hotel.utils.MultipartUtils
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Locale
import com.example.aplicacion_hotel.utils.HotelSessionManager as HotelSessionManager



class EditarPerfilViewModel(
    private val sessionManager: HotelSessionManager,
    private val appContext: Context
) : ViewModel() {

    private val repo = ClienteRepository()

    var isLoading by mutableStateOf(false)
        private set

    var success by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set
    var nombre by mutableStateOf("")
    var dni by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("") 
    var fechaNacimiento by mutableStateOf("") 
    var sexo by mutableStateOf("")            
    var ciudad by mutableStateOf("")
    var vip by mutableStateOf(false)

    var fotoNuevaUri by mutableStateOf<Uri?>(null)
    var fotoActualPath by mutableStateOf<String?>(null) 
    var clienteId by mutableStateOf<String?>(null)

    fun loadFromSession() {
        val c = sessionManager.getCliente() ?: return

        clienteId = c._id
        nombre = c.nombre
        dni = c.dni
        email = c.email
        fechaNacimiento = normalizeToDdMmYyyy(c.fechaNacimiento)
        sexo = c.sexo
        ciudad = c.ciudad
        vip = c.vip
        fotoActualPath = c.foto
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

            val parser = isoFormats.firstNotNullOf { pattern ->
                try { java.text.SimpleDateFormat(pattern, Locale.getDefault()).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") } }
                catch (_: Exception) { null }
            }

            val date = parser.parse(input)
            java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date!!)
        } catch (_: Exception) {
            "" 
        }
    }

    fun setFoto(uri: Uri?) {
        fotoNuevaUri = uri
    }

    fun actualizar() {
        val id = clienteId ?: run {
            errorMessage = "No se encontró el id del cliente"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val partFoto = fotoNuevaUri?.let { uri ->
                    MultipartUtils.uriToMultipart(appContext, uri, "foto")
                }

                val actualizado = repo.actualizarCliente(
                    id = id,
                    nombre = nombre.toRequestBody("text/plain".toMediaType()),
                    dni = dni.toRequestBody("text/plain".toMediaType()),
                    email = email.toRequestBody("text/plain".toMediaType()),
                    fechaNacimiento = fechaNacimiento.toRequestBody("text/plain".toMediaType()),
                    sexo = sexo.toRequestBody("text/plain".toMediaType()),
                    ciudad = ciudad.toRequestBody("text/plain".toMediaType()),
                    vip = vip.toString().toRequestBody("text/plain".toMediaType()),
                    foto = partFoto
                )

                sessionManager.saveCliente(actualizado)
                success = true

            } catch (e: Exception) {
                errorMessage = e.message ?: e.toString()
            } finally {
                isLoading = false
            }
        }
    }
}


