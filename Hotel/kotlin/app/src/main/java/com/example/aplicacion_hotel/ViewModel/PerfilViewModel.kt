package com.example.aplicacion_hotel.ViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.aplicacion_hotel.Model.Cliente
import com.example.aplicacion_hotel.utils.HotelSessionManager
import java.text.SimpleDateFormat
import java.util.*
class PerfilViewModel(
    private val hotelSessionManager: HotelSessionManager
) : ViewModel() {

    var cliente by mutableStateOf<Cliente?>(null)
        private set

    fun loadCliente() {
        cliente = hotelSessionManager.getCliente()
    }

    fun formatearFecha(fechaApi: String): String {
        return try {

            val parser = SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                Locale.getDefault()
            )

            parser.timeZone = TimeZone.getTimeZone("UTC")

            val fecha = parser.parse(fechaApi)

            val formatter = SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
            )

            formatter.format(fecha!!)

        } catch (e: Exception) {
            fechaApi
        }
    }
    fun sexoTexto(sexo: String): String {
        return when(sexo) {
            "M" -> "Hombre"
            "F" -> "Mujer"
            "X" -> "Otro"
            else -> sexo
        }
    }
}