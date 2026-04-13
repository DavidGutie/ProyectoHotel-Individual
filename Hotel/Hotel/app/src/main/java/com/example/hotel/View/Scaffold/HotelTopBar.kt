package com.example.aplicacion_hotel.View.Scaffold

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.example.aplicacion_hotel.View.navigation.Routes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelTopBar(
    navController: NavController
) {

    var expanded by remember { mutableStateOf(false) }

    TopAppBar(

        title = { Text("Hotel App") },

        actions = {

            // Botón info hotel
            IconButton(onClick = {
                navController.navigate(Routes.InfoHotel.route)
            }) {
                Icon(Icons.Default.Info, contentDescription = "Info")
            }

            // Botón menú perfil
            IconButton(onClick = {
                expanded = true
            }) {
                Icon(Icons.Default.AccountCircle, contentDescription = "Perfil")
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {

                DropdownMenuItem(
                    text = { Text("Ver perfil") },
                    onClick = {
                        expanded = false
                        navController.navigate(Routes.Perfil.route)
                    }
                )

                DropdownMenuItem(
                    text = { Text("Editar perfil") },
                    onClick = {
                        expanded = false
                        navController.navigate(Routes.EditarPerfil.route)
                    }
                )

                DropdownMenuItem(
                    text = { Text("Cerrar sesión") },
                    onClick = {
                        expanded = false

                        navController.navigate(Routes.Login.route) {
                            popUpTo(Routes.Home.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    )
}
