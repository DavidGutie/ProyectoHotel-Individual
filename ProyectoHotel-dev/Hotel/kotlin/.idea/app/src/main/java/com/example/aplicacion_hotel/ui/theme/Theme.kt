package com.example.aplicacion_hotel.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HotelColorScheme = darkColorScheme(
    primary = HotelGold,         // Botones principales y destacados
    onPrimary = Black,
    
    secondary = HotelRed,        // Barras superiores, menús laterales
    onSecondary = White,
    
    background = Black,          // Fondo negro total como has pedido
    onBackground = White,        // Letras en blanco
    
    surface = HotelLightGrey,    // Tarjetas y elementos elevados
    onSurface = White,
    
    error = Color(0xFFCF6679),
    onError = Black
)

@Composable
fun Aplicacion_HotelTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = HotelColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
