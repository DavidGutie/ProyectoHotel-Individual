package com.example.aplicacion_hotel.View.screens.pago

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.aplicacion_hotel.ViewModel.ReservaViewModel
import com.example.aplicacion_hotel.utils.HotelSessionManager
import java.text.SimpleDateFormat
import java.util.*

// Funciones de utilidad
fun calcularPrecioTotal(entrada: String, salida: String, precioNoche: Double): Double {
    if (entrada.isEmpty() || salida.isEmpty()) return 0.0
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    sdf.isLenient = false
    return try {
        val date1 = sdf.parse(entrada)
        val date2 = sdf.parse(salida)
        if (date1 != null && date2 != null) {
            // La salida debe ser posterior a la entrada para tener al menos una noche
            if (date2.time <= date1.time) return 0.0

            val diffInMillies = date2.time - date1.time
            // El número de noches es la diferencia de días entre entrada y salida
            val noches = (diffInMillies / (1000 * 60 * 60 * 24)).toInt()

            noches * precioNoche
        } else 0.0
    } catch (e: Exception) {
        0.0
    }
}

fun validarCampos(entrada: String, salida: String, personas: String, tarjeta: String, cvv: String, context: Context): Boolean {
    if (entrada.isEmpty() || salida.isEmpty() || personas.isEmpty() || tarjeta.isEmpty() || cvv.isEmpty()) {
        Toast.makeText(context, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
        return false
    }
    try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.isLenient = false
        val d1 = sdf.parse(entrada)
        val d2 = sdf.parse(salida)

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        if (d1 == null || d2 == null) return false

        if (d1.before(today)) {
            Toast.makeText(context, "No puedes reservar una fecha pasada", Toast.LENGTH_SHORT).show()
            return false
        }

        if (d2.time <= d1.time) {
            Toast.makeText(context, "La fecha de salida debe ser posterior a la de entrada", Toast.LENGTH_SHORT).show()
            return false
        }
    } catch (e: Exception) {
        return false
    }
    if (tarjeta.length < 16 || cvv.length < 3) {
        Toast.makeText(context, "Datos de tarjeta incompletos", Toast.LENGTH_SHORT).show()
        return false
    }
    return true
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagoScreen(
    navController: NavController,
    habitacionId: String,
    precioNoche: Double
) {
    val context = LocalContext.current
    val sessionManager = remember { HotelSessionManager(context) }
    val clienteId = remember { sessionManager.getUserId() }

    val viewModel: ReservaViewModel = viewModel()
    val reservaExitosa by viewModel.reservaExitosa

    var fechaEntrada by remember { mutableStateOf("") }
    var fechaSalida by remember { mutableStateOf("") }
    var personas by remember { mutableStateOf("1") }

    var nombreTarjeta by remember { mutableStateOf("") }
    var numeroTarjeta by remember { mutableStateOf("") }
    var fechaVencimiento by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }

    var showEntradaPicker by remember { mutableStateOf(false) }
    var showSalidaPicker by remember { mutableStateOf(false) }
    val entradaPickerState = rememberDatePickerState()
    val salidaPickerState = rememberDatePickerState()

    val precioTotal = remember(fechaEntrada, fechaSalida, precioNoche) {
        calcularPrecioTotal(fechaEntrada, fechaSalida, precioNoche)
    }

    LaunchedEffect(reservaExitosa) {
        if (reservaExitosa == true) {
            Toast.makeText(context, "Reserva realizada con éxito", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        } else if (reservaExitosa == false) {
            Toast.makeText(context, "Error al realizar la reserva", Toast.LENGTH_SHORT).show()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Finalizar Reserva",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Datos de la estancia", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = fechaEntrada,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha Entrada") },
                        trailingIcon = {
                            IconButton(onClick = { showEntradaPicker = true }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "Elegir fecha")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = fechaSalida,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha Salida") },
                        trailingIcon = {
                            IconButton(onClick = { showSalidaPicker = true }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "Elegir fecha")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = personas,
                        onValueChange = { personas = it },
                        label = { Text("Número de personas") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Método de Pago", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = nombreTarjeta,
                        onValueChange = { nombreTarjeta = it },
                        label = { Text("Nombre en la tarjeta") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = numeroTarjeta,
                        onValueChange = { if (it.length <= 16) numeroTarjeta = it },
                        label = { Text("Número de tarjeta") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = fechaVencimiento,
                            onValueChange = { fechaVencimiento = it },
                            label = { Text("MM/YY") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        OutlinedTextField(
                            value = cvv,
                            onValueChange = { if (it.length <= 3) cvv = it },
                            label = { Text("CVV") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            if (precioTotal > 0) {
                Text(
                    text = "Total a pagar: " + String.format(Locale.US, "%.2f", precioTotal) + " €",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (fechaEntrada.isNotEmpty() && fechaSalida.isNotEmpty()) {
                Text(
                    text = "Rango de fechas inválido",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (validarCampos(fechaEntrada, fechaSalida, personas, numeroTarjeta, cvv, context)) {
                        clienteId?.let { id ->
                            viewModel.crearReserva(
                                clienteId = id,
                                habitacionId = habitacionId,
                                fechaEntrada = fechaEntrada,
                                fechaSalida = fechaSalida,
                                personas = personas.toIntOrNull() ?: 1,
                                precioTotal = precioTotal
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = precioTotal > 0
            ) {
                Text("Pagar y Reservar")
            }
        }
    }

    if (showEntradaPicker) {
        DatePickerDialog(
            onDismissRequest = { showEntradaPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = entradaPickerState.selectedDateMillis
                    if (millis != null) {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        fechaEntrada = sdf.format(Date(millis))
                    }
                    showEntradaPicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showEntradaPicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = entradaPickerState)
        }
    }

    if (showSalidaPicker) {
        DatePickerDialog(
            onDismissRequest = { showSalidaPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = salidaPickerState.selectedDateMillis
                    if (millis != null) {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val selectedDate = sdf.format(Date(millis))
                        if (selectedDate == fechaEntrada) {
                            Toast.makeText(context, "La salida no puede ser el mismo día", Toast.LENGTH_SHORT).show()
                        } else {
                            fechaSalida = selectedDate
                        }
                    }
                    showSalidaPicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showSalidaPicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = salidaPickerState)
        }
    }
}
