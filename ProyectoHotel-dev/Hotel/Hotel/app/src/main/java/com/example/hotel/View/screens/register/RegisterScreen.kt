package com.example.aplicacion_hotel.View.screens.register

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.aplicacion_hotel.View.navigation.Routes
import com.example.aplicacion_hotel.ViewModel.RegisterViewModel
import com.example.aplicacion_hotel.ViewModel.RegisterViewModelFactory
import com.example.aplicacion_hotel.utils.SessionManager

@Composable
fun RegisterScreen(navController: NavController) {


    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    val viewModel: RegisterViewModel = viewModel(
        factory = RegisterViewModelFactory(sessionManager)
    )

    var nombre by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var ciudad by remember { mutableStateOf("") }

    var sexo by remember { mutableStateOf("") }
    var txtSexo by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") }
        )

        OutlinedTextField(
            value = dni,
            onValueChange = { dni = it },
            label = { Text("DNI") }
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") }
        )

        OutlinedTextField(
            value = fechaNacimiento,
            onValueChange = { },
            readOnly = true,
            label = { Text("Fecha de nacimiento") },
            modifier = Modifier.clickable { showDatePicker = true }
        )

        OutlinedTextField(
            value = txtSexo,
            onValueChange = {},
            readOnly = true,
            label = { Text("Sexo") },
            modifier = Modifier.clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Hombre") },
                onClick = {
                    sexo = "M"
                    txtSexo = "Hombre"
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Mujer") },
                onClick = {
                    sexo = "F"
                    txtSexo = "Mujer"
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Otro") },
                onClick = {
                    sexo = "X"
                    txtSexo = "Otro"
                    expanded = false
                }
            )
        }

        OutlinedTextField(
            value = ciudad,
            onValueChange = { ciudad = it },
            label = { Text("Ciudad") }
        )

        Row {

            Button(onClick = {
                viewModel.register(
                    nombre,
                    dni,
                    email,
                    password,
                    fechaNacimiento,
                    sexo,
                    ciudad
                )
                if (viewModel.registerSuccess) {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Register.route) { inclusive = true }
                    }
                }
            },enabled = !viewModel.isLoading)
            {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Crear Cuenta")
                }
            }

            Button(onClick = {
                navController.popBackStack()
            }) {if (viewModel.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Volver")
                }
            }
        }

        if (viewModel.registerSuccess) {
            navController.navigate(Routes.Login.route) {
                popUpTo(Routes.Register.route) { inclusive = true }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(onClick = { showDatePicker = false }) {
                    Text("Aceptar")
                }
            }
        ) {
            val datePickerState = rememberDatePickerState()

            DatePicker(
                state = datePickerState
            )

            datePickerState.selectedDateMillis?.let { millis ->
                val date = java.text.SimpleDateFormat("dd/MM/yyyy")
                    .format(java.util.Date(millis))
                fechaNacimiento = date
            }
        }
    }
}