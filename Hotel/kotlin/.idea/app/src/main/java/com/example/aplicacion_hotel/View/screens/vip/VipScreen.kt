package com.example.aplicacion_hotel.View.screens.vip

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.aplicacion_hotel.ViewModel.VipViewModel
import com.example.aplicacion_hotel.ViewModel.VipViewModelFactory
import com.example.aplicacion_hotel.utils.HotelSessionManager

@Composable
fun VipScreen(navController: NavController) {

    val context = LocalContext.current
    val session = remember { HotelSessionManager(context) }

    val vm: VipViewModel = viewModel(
        factory = VipViewModelFactory(session)
    )

    val yaEsVip = vm.esVip()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(12.dp))

        Text("HAZTE VIP", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Ventajas de ser VIP:",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                Text("• Descuentos en reservas")
                Text("• Prioridad en ofertas")
                Text("• Beneficios exclusivos")
            }
        }

        Spacer(Modifier.height(16.dp))

        vm.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = { vm.hacerVip() },
            enabled = !vm.isLoading && !yaEsVip,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (vm.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
                Text("Procesando...")
            } else {
                Text(if (yaEsVip) "Ya eres VIP" else "Hacerme VIP")
            }
        }
    }

    if (vm.successDialog) {
        AlertDialog(
            onDismissRequest = { vm.cerrarDialog() },
            confirmButton = {
                TextButton(onClick = { vm.cerrarDialog() }) { Text("OK") }
            },
            title = { Text("¡Enhorabuena!") },
            text = { Text("Te has hecho VIP correctamente ✅") }
        )
    }
}