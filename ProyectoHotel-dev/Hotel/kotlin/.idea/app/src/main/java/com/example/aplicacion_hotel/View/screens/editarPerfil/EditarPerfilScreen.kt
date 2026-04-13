package com.example.aplicacion_hotel.View.screens.editarPerfil

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.aplicacion_hotel.ViewModel.EditarPerfilViewModel
import com.example.aplicacion_hotel.ViewModel.EditarPerfilViewModelFactory
import com.example.aplicacion_hotel.utils.HotelSessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarPerfilScreen(navController: NavController) {
    val context = LocalContext.current
    val sessionManager = remember { HotelSessionManager(context) }
    val viewModel: EditarPerfilViewModel = viewModel(
        factory = EditarPerfilViewModelFactory(sessionManager, context.applicationContext)
    )

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> viewModel.setFoto(uri) }
    )

    // Cargar datos iniciales del usuario
    LaunchedEffect(Unit) {
        viewModel.loadFromSession()
    }

    // Volver atrás si la actualización fue exitosa
    LaunchedEffect(viewModel.success) {
        if (viewModel.success) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            // Selector de Foto
            Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                if (viewModel.fotoNuevaUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(viewModel.fotoNuevaUri),
                        contentDescription = "Nueva foto",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.fillMaxSize(), tint = Color.Gray)
                }
            }
            Button(onClick = { launcher.launch("image/*") }) {
                Text("Elegir Foto")
            }
            
            Spacer(Modifier.height(24.dp))

            // Campos de texto
            OutlinedTextField(value = viewModel.nombre, onValueChange = { viewModel.nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = viewModel.dni, onValueChange = { viewModel.dni = it }, label = { Text("DNI") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = viewModel.email, onValueChange = { viewModel.email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = viewModel.ciudad, onValueChange = { viewModel.ciudad = it }, label = { Text("Ciudad") }, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(16.dp))
            
            // Switch VIP
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Es VIP", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.weight(1f))
                Switch(checked = viewModel.vip, onCheckedChange = { viewModel.vip = it })
            }

            Spacer(Modifier.height(32.dp))
            
            // Botones de acción
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
                Button(
                    onClick = { viewModel.actualizar() },
                    modifier = Modifier.weight(1f),
                    enabled = !viewModel.isLoading
                ) {
                    if (viewModel.isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text("Guardar")
                }
            }

            viewModel.errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp))
            }
        }
    }
}
