package com.example.aplicacion_hotel.View.screens.perfil

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.aplicacion_hotel.Network.RetrofitInstance
import com.example.aplicacion_hotel.ViewModel.PerfilViewModel
import com.example.aplicacion_hotel.ViewModel.PerfilViewModelFactory
import com.example.aplicacion_hotel.utils.HotelSessionManager

@Composable
fun PerfilScreen(navController: NavHostController) {

    val context = LocalContext.current
    val hotelSessionManager = remember { HotelSessionManager(context) }

    val viewModel: PerfilViewModel = viewModel(
        factory = PerfilViewModelFactory(hotelSessionManager)
    )

    LaunchedEffect(Unit) {
        viewModel.loadCliente()
    }

    val cliente = viewModel.cliente

    if (cliente == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay sesión iniciada", color = Color.White)
        }
        return
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))


            if (!cliente.foto.isNullOrEmpty()) {
                AsyncImage(
                    model = "${RetrofitInstance.BASE_URL.dropLast(1)}${cliente.foto}",
                    contentDescription = "Foto perfil",
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape),
                    alignment = Alignment.Center
                )
            } else {
                Surface(
                    modifier = Modifier.size(140.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Usuario",
                            modifier = Modifier.size(80.dp),
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = cliente.nombre,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 28.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            PerfilItemLocal("Email", cliente.email)
            PerfilItemLocal("DNI", cliente.dni)
            PerfilItemLocal(
                "Fecha nacimiento",
                viewModel.formatearFecha(cliente.fechaNacimiento)
            )
            PerfilItemLocal(
                "Sexo",
                viewModel.sexoTexto(cliente.sexo)
            )
            PerfilItemLocal("Ciudad", cliente.ciudad)
            PerfilItemLocal("VIP", if (cliente.vip) "Sí" else "No")
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PerfilItemLocal(label: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
        }
    }
}
