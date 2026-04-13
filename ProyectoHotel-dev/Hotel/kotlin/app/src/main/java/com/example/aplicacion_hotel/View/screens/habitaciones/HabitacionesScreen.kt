package com.example.aplicacion_hotel.View.screens.habitaciones

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.aplicacion_hotel.Model.Habitacion
import com.example.aplicacion_hotel.ViewModel.HabitacionesViewModel
import com.example.aplicacion_hotel.ViewModel.HabitacionesViewModelFactory
import com.example.aplicacion_hotel.View.navigation.Routes
import com.example.aplicacion_hotel.utils.HotelSessionManager
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HabitacionesScreen(navController: NavController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = remember { HotelSessionManager(context) }

    val vm: HabitacionesViewModel = viewModel(
        factory = HabitacionesViewModelFactory(sessionManager)
    )

    val habitaciones by vm.habitaciones
    val cargando by vm.cargando
    val error by vm.error

    var habitacionInfo by remember { mutableStateOf<Habitacion?>(null) }

    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    var fechaInicio by remember { mutableStateOf("") }
    var fechaFin by remember { mutableStateOf("") }

    var showInicioPicker by remember { mutableStateOf(false) }
    var showFinPicker by remember { mutableStateOf(false) }

    val inicioPickerState = rememberDatePickerState()
    val finPickerState = rememberDatePickerState()

    val entradaInteraction = remember { MutableInteractionSource() }
    val salidaInteraction = remember { MutableInteractionSource() }
    val entradaPressed by entradaInteraction.collectIsPressedAsState()
    val salidaPressed by salidaInteraction.collectIsPressedAsState()

    LaunchedEffect(Unit) {
        vm.cargarHabitaciones(soloDisponibles = true)
    }

    LaunchedEffect(entradaPressed) {
        if (entradaPressed) showInicioPicker = true
    }
    LaunchedEffect(salidaPressed) {
        if (salidaPressed) showFinPicker = true
    }

    if (showInicioPicker) {
        DatePickerDialog(
            onDismissRequest = { showInicioPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val ms = inicioPickerState.selectedDateMillis
                    if (ms != null) {
                        fechaInicio = sdf.format(Date(ms))
                        if (fechaFin.isNotBlank()) {
                            val start = sdf.parse(fechaInicio)
                            val end = sdf.parse(fechaFin)
                            if (start != null && end != null && end.before(start)) {
                                fechaFin = ""
                            }
                        }
                    }
                    showInicioPicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showInicioPicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = inicioPickerState)
        }
    }

    if (showFinPicker) {
        DatePickerDialog(
            onDismissRequest = { showFinPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val ms = finPickerState.selectedDateMillis
                    if (ms != null) {
                        val nuevaFin = sdf.format(Date(ms))
                        if (fechaInicio.isNotBlank()) {
                            val start = sdf.parse(fechaInicio)
                            val end = sdf.parse(nuevaFin)
                            if (start != null && end != null && end.before(start)) {
                                fechaFin = ""
                            } else {
                                fechaFin = nuevaFin
                            }
                        } else {
                            fechaFin = nuevaFin
                        }
                    }
                    showFinPicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showFinPicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = finPickerState)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text("Buscar por fechas", style = MaterialTheme.typography.titleLarge)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.padding(12.dp)) {

                Row(Modifier.fillMaxWidth()) {

                    OutlinedTextField(
                        value = fechaInicio,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Entrada") },
                        placeholder = { Text("Selecciona fecha") },
                        modifier = Modifier.weight(1f),
                        interactionSource = entradaInteraction,
                        trailingIcon = {
                            TextButton(onClick = { showInicioPicker = true }) { Text("📅") }
                        }
                    )

                    Spacer(Modifier.width(8.dp))

                    OutlinedTextField(
                        value = fechaFin,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Salida") },
                        placeholder = { Text("Selecciona fecha") },
                        modifier = Modifier.weight(1f),
                        interactionSource = salidaInteraction,
                        trailingIcon = {
                            TextButton(onClick = { showFinPicker = true }) { Text("📅") }
                        }
                    )
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (fechaInicio.isNotBlank() && fechaFin.isNotBlank()) {
                            vm.buscarDisponibles(fechaInicio, fechaFin)
                        } else {
                            vm.cargarHabitaciones(true)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Buscar Disponibilidad")
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Text("Habitaciones encontradas", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        when {
            cargando -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        habitaciones,
                        key = { it._id ?: "${it.numero}-${it.tipo}" }
                    ) { hab ->
                        HabitacionCard(
                            habitacion = hab,
                            onInfo = { habitacionInfo = hab },
                            onSelect = {
                                navController.navigate(
                                    Routes.Pago.createRoute(hab._id, hab.precionoche)
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    habitacionInfo?.let { hab ->
        InfoHabitacionDialog(
            habitacion = hab,
            onDismiss = { habitacionInfo = null }
        )
    }
}

@Composable
private fun HabitacionCard(
    habitacion: Habitacion,
    onInfo: () -> Unit,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HabitacionMiniImagen(
                url = "https://loremflickr.com/600/400/hotel,room?random=3"
            )

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Habitación ${habitacion.numero} · ${habitacion.tipo}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${habitacion.precionoche} € / noche",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            IconButton(onClick = onInfo) {
                Icon(Icons.Filled.Info, contentDescription = "Información")
            }
        }
    }
}

@Composable
private fun InfoHabitacionDialog(
    habitacion: Habitacion,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        },
        title = {
            Text("Habitación ${habitacion.numero} · ${habitacion.tipo}")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Precio/noche: ${habitacion.precionoche} €")
                Text("Máx. ocupantes: ${habitacion.max_ocupantes}")
                Text("Rate: ${habitacion.rate}")
                Text("Oferta: ${if (habitacion.oferta) "Sí" else "No"}")
                Text("Descripción: ${habitacion.descripcion}")
                if (habitacion.servicios.isNotEmpty()) {
                    Text("Servicios: ${habitacion.servicios.joinToString(", ")}")
                }
            }
        }
    )
}