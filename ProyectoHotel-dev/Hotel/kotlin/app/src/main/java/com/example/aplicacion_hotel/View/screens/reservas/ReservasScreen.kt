package com.example.aplicacion_hotel.View.screens.reservas

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aplicacion_hotel.ViewModel.ReservaViewModel
import com.example.aplicacion_hotel.ViewModel.ResenaViewModel
import com.example.aplicacion_hotel.utils.HotelSessionManager
import com.example.aplicacion_hotel.Model.Reserva
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReservasScreen() {
    val context = LocalContext.current
    val sessionManager = HotelSessionManager(context)
    val clienteId = remember { sessionManager.getUserId() }

    val viewModel: ReservaViewModel = viewModel()
    val resenaViewModel: ResenaViewModel = viewModel()

    val reservas by viewModel.reservas
    val resenaEnviada by resenaViewModel.resenaEnviada

    var expandedReservaId by remember { mutableStateOf<String?>(null) }
    var reservaIdParaCancelar by remember { mutableStateOf<String?>(null) }
    var reservaParaReseña by remember { mutableStateOf<Reserva?>(null) }

    // Cargar reservas al iniciar
    LaunchedEffect(clienteId) {
        clienteId?.let { id -> viewModel.cargarReservas(id) }
    }
    LaunchedEffect(resenaEnviada) {
        if (resenaEnviada == true) {
            Toast.makeText(context, "¡Reseña enviada con éxito!", Toast.LENGTH_SHORT).show()
            resenaViewModel.resetEstado()
            reservaParaReseña = null
        } else if (resenaEnviada == false) {
            val msg = resenaViewModel.errorMessage.value ?: "Error al enviar la reseña"
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            resenaViewModel.resetEstado()
        }
    }
    if (reservaParaReseña != null) {
        ReseñaDialog(
            onDismiss = { reservaParaReseña = null },
            onConfirm = { comentario, puntuacion ->
                clienteId?.let { cid ->
                    // CORREGIDO: Pasamos clienteId, reservaId y habitacionId
                    resenaViewModel.enviarResena(
                        clienteId = cid,
                        reservaId = reservaParaReseña!!._id,
                        habitacionId = reservaParaReseña!!.habitacionId,
                        comentario = comentario,
                        puntuacion = puntuacion
                    )
                }
            }
        )
    }
    if (reservaIdParaCancelar != null) {
        AlertDialog(
            onDismissRequest = { reservaIdParaCancelar = null },
            title = { Text("Cancelar Reserva") },
            text = { Text("¿Seguro que quieres cancelar esta reserva?") },
            confirmButton = {
                TextButton(onClick = {
                    clienteId?.let { cid -> viewModel.cancelarReserva(reservaIdParaCancelar!!, cid) }
                    reservaIdParaCancelar = null
                }) { Text("Sí, Cancelar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { reservaIdParaCancelar = null }) { Text("Volver") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Mis Reservas", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        if (reservas.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No tienes reservas aún")
            }
        } else {
            LazyColumn {
                items(reservas) { reserva ->
                    val isExpanded = expandedReservaId == reserva._id
                    val terminada = haFinalizado(reserva.fechaSalida)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { expandedReservaId = if (isExpanded) null else reserva._id },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Habitación: ${reserva.habitacionId}", style = MaterialTheme.typography.titleMedium)
                            Text("Salida: ${reserva.fechaSalida}")

                            AnimatedVisibility(visible = isExpanded) {
                                Column {
                                    Spacer(Modifier.height(8.dp))
                                    Text("Total: ${reserva.precioTotal}€")
                                    Text("Estado: ${if (reserva.cancelacion) "Cancelada" else "Activa"}")

                                    if (!reserva.cancelacion) {
                                        if (terminada) {
                                            Button(
                                                onClick = { reservaParaReseña = reserva },
                                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                            ) {
                                                Text("Poner Reseña")
                                            }
                                        } else {
                                            Button(
                                                onClick = { reservaIdParaCancelar = reserva._id },
                                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                            ) {
                                                Text("Cancelar Reserva")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReseñaDialog(onDismiss: () -> Unit, onConfirm: (String, Int) -> Unit) {
    var comentario by remember { mutableStateOf("") }
    var puntuacion by remember { mutableStateOf(5) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Valorar Estancia") },
        text = {
            Column {
                Text("Puntuación: $puntuacion / 5")
                Slider(
                    value = puntuacion.toFloat(),
                    onValueChange = { puntuacion = it.toInt() },
                    valueRange = 1f..5f,
                    steps = 3
                )
                OutlinedTextField(
                    value = comentario,
                    onValueChange = { comentario = it },
                    label = { Text("Tu comentario") },
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(comentario, puntuacion) }) { Text("Enviar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

private fun haFinalizado(fechaSalida: String): Boolean {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateSalida = sdf.parse(fechaSalida)
        dateSalida?.before(Date()) ?: false
    } catch (e: Exception) {
        false
    }
}
