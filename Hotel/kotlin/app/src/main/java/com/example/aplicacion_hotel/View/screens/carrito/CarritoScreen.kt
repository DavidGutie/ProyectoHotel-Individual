package com.example.aplicacion_hotel.View.screens.carrito


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aplicacion_hotel.Model.Habitacion
import com.example.aplicacion_hotel.ViewModel.CarritoViewModel
import com.example.aplicacion_hotel.ViewModel.CarritoViewModelFactory
import com.example.aplicacion_hotel.utils.HotelSessionManager

@Composable
fun CarritoScreen() {
    val context = LocalContext.current
    val sessionManager = remember { HotelSessionManager(context) }

    val vm: CarritoViewModel = viewModel(factory = CarritoViewModelFactory(sessionManager))

    val habitaciones by vm.habitacionesCarrito
    val cargando by vm.cargando
    val error by vm.error

    var habitacionInfo by remember { mutableStateOf<Habitacion?>(null) }

    LaunchedEffect(Unit) {
        vm.cargarCarrito()
    }

    Column(Modifier.fillMaxSize().padding(12.dp)) {

        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Reserva (carrito)", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            TextButton(onClick = { vm.vaciarCarrito() }) {
                Text("Vaciar")
            }
        }

        Spacer(Modifier.height(12.dp))

        if (cargando) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        if (error != null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(error ?: "Error")
            }
            return@Column
        }

        if (habitaciones.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No has añadido habitaciones aún")
            }
            return@Column
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(habitaciones, key = { it._id }) { hab ->
                Card(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Habitación ${hab.numero} · ${hab.tipo}", style = MaterialTheme.typography.titleMedium)
                            Text("${hab.precionoche} € / noche")
                        }

                        IconButton(onClick = { habitacionInfo = hab }) {
                            Icon(Icons.Default.Info, contentDescription = "Info")
                        }

                        IconButton(onClick = { vm.eliminarDelCarrito(hab._id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Quitar")
                        }
                    }
                }
            }
        }
    }

    habitacionInfo?.let { hab ->
        AlertDialog(
            onDismissRequest = { habitacionInfo = null },
            confirmButton = {
                TextButton(onClick = { habitacionInfo = null }) { Text("Cerrar") }
            },
            title = { Text("Habitación ${hab.numero} · ${hab.tipo}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Precio/noche: ${hab.precionoche} €")
                    Text("Máx. ocupantes: ${hab.max_ocupantes}")
                    Text("Rate: ${hab.rate}")
                    Text("Oferta: ${if (hab.oferta) "Sí" else "No"}")
                    Text("Descripción:")
                    Text(hab.descripcion)
                    if (hab.servicios.isNotEmpty()) {
                        Text("Servicios:")
                        Text(hab.servicios.joinToString(", "))
                    }
                }
            }
        )
    }
}