package View.screens.reservas

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hotel.ViewModel.ReservaViewModel
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.aplicacion_hotel.utils.SessionManager

@Composable
fun ReservasScreen(
    habitacionId: String
) {

    val viewModel: ReservaViewModel = viewModel()
    val context = LocalContext.current
    val sessionManager = SessionManager(context)

    var fechaEntrada by remember { mutableStateOf("") }
    var fechaSalida by remember { mutableStateOf("") }
    var personas by remember { mutableStateOf("1") }

    val reservaExitosa by viewModel.reservaExitosa

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text("Nueva Reserva", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = fechaEntrada,
            onValueChange = { fechaEntrada = it },
            label = { Text("Fecha Entrada (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = fechaSalida,
            onValueChange = { fechaSalida = it },
            label = { Text("Fecha Salida (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = personas,
            onValueChange = { personas = it },
            label = { Text("Número de personas") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val clienteId = sessionManager.getUserId()

                if (clienteId != null) {
                    viewModel.crearReserva(
                        clienteId = clienteId,
                        habitacionId = habitacionId,
                        fechaEntrada = fechaEntrada,
                        fechaSalida = fechaSalida,
                        personas = personas.toIntOrNull() ?: 1,
                        precioTotal = 0.0
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reservar")
        }

        reservaExitosa?.let {
            if (it) {
                Toast.makeText(context, "Reserva realizada correctamente", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Error al crear la reserva", Toast.LENGTH_SHORT).show()
            }
        }
    }
}