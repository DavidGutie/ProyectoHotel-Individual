package com.example.aplicacion_hotel.View.screens.habitaciones

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.aplicacion_hotel.Model.Amenity
import com.example.aplicacion_hotel.Model.Habitacion
import com.example.aplicacion_hotel.View.navigation.Routes
import com.example.aplicacion_hotel.ViewModel.HabitacionesViewModel
import com.example.aplicacion_hotel.ViewModel.HabitacionesViewModelFactory
import com.example.aplicacion_hotel.utils.HotelSessionManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HabitacionesScreen(navController: NavController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = remember { HotelSessionManager(context) }

    val vm: HabitacionesViewModel = viewModel(
        factory = HabitacionesViewModelFactory(sessionManager)
    )

    val habitaciones by vm.habitaciones
    val amenities by vm.amenities
    val cargando by vm.cargando
    val error by vm.error
    val idsCarrito by vm.idsCarrito

    var habitacionInfo by remember { mutableStateOf<Habitacion?>(null) }
    var mostrarComparador by remember { mutableStateOf(false) }
    var idsComparador by remember { mutableStateOf<Set<String>>(emptySet()) }
    var admiteMascotas by remember { mutableStateOf(false) }
    var amenitiesSeleccionados by remember { mutableStateOf<Set<String>>(emptySet()) }

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
        vm.cargarCatalogoAmenitiesSiHaceFalta()
        vm.cargarHabitaciones(soloDisponibles = true)
    }

    LaunchedEffect(entradaPressed) {
        if (entradaPressed) showInicioPicker = true
    }
    LaunchedEffect(salidaPressed) {
        if (salidaPressed) showFinPicker = true
    }

    fun ejecutarBusqueda() {
        if (fechaInicio.isNotBlank() && fechaFin.isNotBlank()) {
            vm.buscarDisponibles(fechaInicio, fechaFin)
        } else {
            vm.aplicarFiltros(admiteMascotas, amenitiesSeleccionados)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text("Busqueda avanzada", style = MaterialTheme.typography.titleLarge)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = fechaInicio,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Entrada") },
                        placeholder = { Text("Selecciona fecha") },
                        modifier = Modifier.weight(1f),
                        interactionSource = entradaInteraction
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = fechaFin,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Salida") },
                        placeholder = { Text("Selecciona fecha") },
                        modifier = Modifier.weight(1f),
                        interactionSource = salidaInteraction
                    )
                }

                FilterChip(
                    selected = admiteMascotas,
                    onClick = {
                        admiteMascotas = !admiteMascotas
                        vm.aplicarFiltros(admiteMascotas, amenitiesSeleccionados)
                    },
                    leadingIcon = { Icon(Icons.Default.Pets, contentDescription = null) },
                    label = { Text("Admite mascotas") }
                )

                if (amenities.isNotEmpty()) {
                    Text("Amenities", style = MaterialTheme.typography.titleSmall)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        amenities.forEach { amenity ->
                            val selected = amenitiesSeleccionados.contains(amenity.name)
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    amenitiesSeleccionados = if (selected) {
                                        amenitiesSeleccionados - amenity.name
                                    } else {
                                        amenitiesSeleccionados + amenity.name
                                    }
                                    vm.aplicarFiltros(admiteMascotas, amenitiesSeleccionados)
                                },
                                leadingIcon = {
                                    Icon(iconForAmenity(amenity), contentDescription = null)
                                },
                                label = { Text(amenity.name) }
                            )
                        }
                    }
                }

                Button(
                    onClick = { ejecutarBusqueda() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Buscar disponibilidad")
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Habitaciones encontradas",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            OutlinedButton(
                onClick = { mostrarComparador = true },
                enabled = idsComparador.size >= 2
            ) {
                Text("Comparar (${idsComparador.size})")
            }
        }

        Spacer(Modifier.height(8.dp))

        when {
            cargando -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            error != null -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(habitaciones, key = { it._id }) { hab ->
                    HabitacionCard(
                        habitacion = hab,
                        enCarrito = idsCarrito.contains(hab._id),
                        seleccionadaComparador = idsComparador.contains(hab._id),
                        onInfo = { habitacionInfo = hab },
                        onReservar = {
                            navController.navigate(Routes.Pago.createRoute(hab._id, hab.precionoche))
                        },
                        onToggleComparador = {
                            idsComparador = if (idsComparador.contains(hab._id)) {
                                idsComparador - hab._id
                            } else {
                                idsComparador + hab._id
                            }
                        },
                        onToggleCarrito = {
                            val estabaEnCarrito = idsCarrito.contains(hab._id)
                            vm.toggleCarrito(hab._id)
                            Toast.makeText(
                                context,
                                if (estabaEnCarrito) "Habitacion quitada del carrito" else "Habitacion anadida al carrito",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }
        }
    }

    habitacionInfo?.let { hab ->
        InfoHabitacionDialog(habitacion = hab, onDismiss = { habitacionInfo = null })
    }

    if (mostrarComparador) {
        ComparadorHabitacionesDialog(
            habitaciones = habitaciones.filter { idsComparador.contains(it._id) },
            onDismiss = { mostrarComparador = false }
        )
    }

    if (showInicioPicker) {
        DatePickerDialog(
            onDismissRequest = { showInicioPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    inicioPickerState.selectedDateMillis?.let { ms ->
                        fechaInicio = sdf.format(Date(ms))
                        if (fechaFin.isNotBlank()) {
                            val start = sdf.parse(fechaInicio)
                            val end = sdf.parse(fechaFin)
                            if (start != null && end != null && end.before(start)) fechaFin = ""
                        }
                    }
                    showInicioPicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showInicioPicker = false }) { Text("Cancelar") } }
        ) { DatePicker(state = inicioPickerState) }
    }

    if (showFinPicker) {
        DatePickerDialog(
            onDismissRequest = { showFinPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    finPickerState.selectedDateMillis?.let { ms ->
                        val nuevaFin = sdf.format(Date(ms))
                        val start = fechaInicio.takeIf { it.isNotBlank() }?.let { sdf.parse(it) }
                        val end = sdf.parse(nuevaFin)
                        if (start != null && end != null && end.before(start)) fechaFin = "" else fechaFin = nuevaFin
                    }
                    showFinPicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showFinPicker = false }) { Text("Cancelar") } }
        ) { DatePicker(state = finPickerState) }
    }
}

@Composable
private fun HabitacionCard(
    habitacion: Habitacion,
    enCarrito: Boolean,
    seleccionadaComparador: Boolean,
    onInfo: () -> Unit,
    onReservar: () -> Unit,
    onToggleComparador: () -> Unit,
    onToggleCarrito: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HabitacionMiniImagen(url = "https://loremflickr.com/600/400/hotel,room?random=${habitacion.numero}")
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Habitacion ${habitacion.numero} - ${habitacion.tipo}", style = MaterialTheme.typography.titleMedium)
                Text(String.format(Locale.US, "%.2f EUR / noche", habitacion.precionoche))
                if (habitacion.admiteMascotas) {
                    AssistChip(
                        onClick = {},
                        leadingIcon = { Icon(Icons.Default.Pets, contentDescription = null) },
                        label = { Text("Admite mascotas") }
                    )
                }
            }

            IconButton(onClick = onInfo) {
                Icon(Icons.Filled.Info, contentDescription = "Informacion")
            }
            IconButton(onClick = onToggleCarrito) {
                Icon(
                    imageVector = Icons.Filled.ShoppingCart,
                    contentDescription = if (enCarrito) "Quitar del carrito" else "Anadir al carrito",
                    tint = if (enCarrito) MaterialTheme.colorScheme.primary else LocalContentColor.current
                )
            }
            FilterChip(
                selected = seleccionadaComparador,
                onClick = onToggleComparador,
                label = { Text("Comparar") },
                leadingIcon = if (seleccionadaComparador) {
                    { Icon(Icons.Default.Check, contentDescription = null) }
                } else null
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = onReservar) { Text("Reservar") }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InfoHabitacionDialog(habitacion: Habitacion, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } },
        title = { Text("Habitacion ${habitacion.numero} - ${habitacion.tipo}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Precio/noche: ${String.format(Locale.US, "%.2f", habitacion.precionoche)} EUR")
                Text("Max. ocupantes: ${habitacion.max_ocupantes}")
                Text("Rate: ${habitacion.rate}")
                Text("Oferta: ${if (habitacion.oferta) "Si" else "No"}")
                Text("Descripcion: ${habitacion.descripcion}")
                PetPolicyLine(habitacion)
                AmenitiesPorCategoria(habitacion.amenities)
                if (habitacion.servicios.isNotEmpty()) {
                    Text("Servicios: ${habitacion.servicios.joinToString(", ")}")
                }
            }
        }
    )
}

@Composable
private fun PetPolicyLine(habitacion: Habitacion) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Pets, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            if (habitacion.admiteMascotas) {
                val extra = if (habitacion.suplementoMascotaNoche > 0) {
                    " - suplemento ${String.format(Locale.US, "%.2f", habitacion.suplementoMascotaNoche)} EUR/noche"
                } else ""
                "Admite mascotas$extra"
            } else {
                "No admite mascotas"
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AmenitiesPorCategoria(amenities: List<Amenity>) {
    if (amenities.isEmpty()) return
    amenities.groupBy { it.category.ifBlank { "General" } }.forEach { (categoria, items) ->
        Text(categoria, style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items.forEach { amenity ->
                AssistChip(
                    onClick = {},
                    leadingIcon = { Icon(iconForAmenity(amenity), contentDescription = null) },
                    label = { Text(amenity.name) }
                )
            }
        }
    }
}

@Composable
private fun ComparadorHabitacionesDialog(habitaciones: List<Habitacion>, onDismiss: () -> Unit) {
    val amenities = habitaciones.flatMap { it.amenities }.distinctBy { it.name }.sortedBy { it.name }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } },
        title = { Text("Comparador de habitaciones") },
        text = {
            Column(Modifier.horizontalScroll(rememberScrollState())) {
                CompareRow("Habitacion", habitaciones.map { "${it.numero} - ${it.tipo}" })
                CompareRow("Precio", habitaciones.map { String.format(Locale.US, "%.2f EUR", it.precionoche) })
                CompareRow("Mascotas", habitaciones.map { if (it.admiteMascotas) "Si" else "No" })
                amenities.forEach { amenity ->
                    CompareIconRow(
                        label = amenity.name,
                        values = habitaciones.map { hab -> hab.amenities.any { it.name == amenity.name } }
                    )
                }
            }
        }
    )
}

@Composable
private fun CompareRow(label: String, values: List<String>) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(130.dp), style = MaterialTheme.typography.labelLarge)
        values.forEach { value ->
            Surface(
                modifier = Modifier.width(130.dp).padding(2.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(value, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun CompareIconRow(label: String, values: List<Boolean>) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(130.dp), style = MaterialTheme.typography.labelLarge)
        values.forEach { hasAmenity ->
            Surface(
                modifier = Modifier.width(130.dp).padding(2.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(6.dp)
            ) {
                Box(Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (hasAmenity) Icons.Default.CheckCircle else Icons.Default.Close,
                        contentDescription = null,
                        tint = if (hasAmenity) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

private fun iconForAmenity(amenity: Amenity): ImageVector {
    val text = "${amenity.icon} ${amenity.name} ${amenity.category}".lowercase()
    return when {
        "wifi" in text || "internet" in text -> Icons.Default.Wifi
        "parking" in text || "aparc" in text -> Icons.Default.LocalParking
        "restaurant" in text || "desayuno" in text || "comida" in text -> Icons.Default.Restaurant
        "pool" in text || "piscina" in text -> Icons.Default.Pool
        "spa" in text -> Icons.Default.Spa
        "gym" in text || "fitness" in text -> Icons.Default.FitnessCenter
        "tv" in text || "tele" in text -> Icons.Default.Tv
        "air" in text || "clima" in text || "ac" in text -> Icons.Default.AcUnit
        "lavander" in text || "laundry" in text -> Icons.Default.LocalLaundryService
        "safe" in text || "segur" in text -> Icons.Default.Security
        "coffee" in text || "cafe" in text -> Icons.Default.Coffee
        "pet" in text || "mascota" in text -> Icons.Default.Pets
        else -> Icons.Default.CheckCircle
    }
}
