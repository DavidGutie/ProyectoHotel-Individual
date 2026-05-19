package com.example.aplicacion_hotel.View.screens.reservas

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aplicacion_hotel.Model.Reserva
import com.example.aplicacion_hotel.ViewModel.ResenaViewModel
import com.example.aplicacion_hotel.ViewModel.ReservaViewModel
import com.example.aplicacion_hotel.utils.HotelSessionManager
import okhttp3.ResponseBody
import java.io.File
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
    val historiales by viewModel.historiales
    val facturaDescargada by viewModel.facturaDescargada
    val descargandoFactura by viewModel.descargandoFactura
    val resenaEnviada by resenaViewModel.resenaEnviada

    var expandedReservaId by remember { mutableStateOf<String?>(null) }
    var reservaIdParaCancelar by remember { mutableStateOf<String?>(null) }
    var reservaParaResena by remember { mutableStateOf<Reserva?>(null) }

    LaunchedEffect(clienteId) {
        clienteId?.let { id -> viewModel.cargarReservas(id) }
    }

    LaunchedEffect(resenaEnviada) {
        if (resenaEnviada == true) {
            Toast.makeText(context, "Resena enviada con exito", Toast.LENGTH_SHORT).show()
            resenaViewModel.resetEstado()
            reservaParaResena = null
        } else if (resenaEnviada == false) {
            val msg = resenaViewModel.errorMessage.value ?: "Error al enviar la resena"
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            resenaViewModel.resetEstado()
        }
    }

    LaunchedEffect(facturaDescargada) {
        facturaDescargada?.let { (reservaId, body) ->
            val abierta = guardarYAbrirFactura(context, reservaId, body)
            Toast.makeText(
                context,
                if (abierta) "Factura generada" else "Factura guardada, pero no hay visor de PDF instalado",
                Toast.LENGTH_LONG
            ).show()
            viewModel.facturaProcesada()
        }
    }

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    reservaParaResena?.let { reserva ->
        ResenaDialog(
            onDismiss = { reservaParaResena = null },
            onConfirm = { comentario, puntuacion ->
                clienteId?.let { cid ->
                    resenaViewModel.enviarResena(
                        clienteId = cid,
                        reservaId = reserva._id,
                        habitacionId = reserva.habitacionId,
                        comentario = comentario,
                        puntuacion = puntuacion
                    )
                }
            }
        )
    }

    reservaIdParaCancelar?.let { reservaId ->
        AlertDialog(
            onDismissRequest = { reservaIdParaCancelar = null },
            title = { Text("Cancelar reserva") },
            text = { Text("Seguro que quieres cancelar esta reserva?") },
            confirmButton = {
                TextButton(onClick = {
                    clienteId?.let { cid -> viewModel.cancelarReserva(reservaId, cid) }
                    reservaIdParaCancelar = null
                }) {
                    Text("Si, cancelar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { reservaIdParaCancelar = null }) { Text("Volver") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Mis reservas", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        if (reservas.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No tienes reservas aun")
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
                            Text("Habitacion: ${reserva.habitacionId}", style = MaterialTheme.typography.titleMedium)
                            Text("Salida: ${reserva.fechaSalida}")

                            AnimatedVisibility(visible = isExpanded) {
                                Column {
                                    Spacer(Modifier.height(8.dp))
                                    val suplementoMascota = reserva.suplementoMascotaReserva
                                    val baseReserva = (reserva.precioTotal - suplementoMascota).coerceAtLeast(0.0)
                                    Text("Estancia: ${String.format(Locale.US, "%.2f EUR", baseReserva)}")
                                    if (suplementoMascota > 0 || reserva.with_pet || reserva.mascotas > 0) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Pets, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("Suplemento mascota: ${String.format(Locale.US, "%.2f EUR", suplementoMascota)}")
                                        }
                                    }
                                    Text("Total: ${String.format(Locale.US, "%.2f EUR", reserva.precioTotal)}")
                                    Text("Estado: ${if (reserva.cancelacion) "Cancelada" else "Activa"}")

                                    Spacer(Modifier.height(12.dp))
                                    Text("Historial", style = MaterialTheme.typography.titleSmall)
                                    val historial = historiales[reserva._id] ?: listOf("Cargando historial...")
                                    historial.forEach { accion ->
                                        Text("- $accion", style = MaterialTheme.typography.bodyMedium)
                                    }

                                    if (!reserva.cancelacion) {
                                        Button(
                                            onClick = { viewModel.descargarFactura(reserva._id) },
                                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                                            enabled = descargandoFactura != reserva._id
                                        ) {
                                            if (descargandoFactura == reserva._id) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(18.dp),
                                                    strokeWidth = 2.dp,
                                                    color = MaterialTheme.colorScheme.onPrimary
                                                )
                                            } else {
                                                Icon(Icons.Default.Description, contentDescription = null)
                                            }
                                            Spacer(Modifier.width(8.dp))
                                            Text("Generar factura")
                                        }

                                        if (terminada) {
                                            Button(
                                                onClick = { reservaParaResena = reserva },
                                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                            ) {
                                                Text("Poner resena")
                                            }
                                        } else {
                                            Button(
                                                onClick = { reservaIdParaCancelar = reserva._id },
                                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                            ) {
                                                Text("Cancelar reserva")
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

private fun guardarYAbrirFactura(context: Context, reservaId: String, body: ResponseBody): Boolean {
    val facturasDir = File(context.cacheDir, "facturas").apply { mkdirs() }
    val factura = File(facturasDir, "factura_$reservaId.pdf")

    body.byteStream().use { input ->
        factura.outputStream().use { output ->
            input.copyTo(output)
        }
    }

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        factura
    )

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    return try {
        context.startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}

@Composable
fun ResenaDialog(onDismiss: () -> Unit, onConfirm: (String, Int) -> Unit) {
    var comentario by remember { mutableStateOf("") }
    var puntuacion by remember { mutableStateOf(5) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Valorar estancia") },
        text = {
            Column {
                Text("Puntuacion: $puntuacion / 5")
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
