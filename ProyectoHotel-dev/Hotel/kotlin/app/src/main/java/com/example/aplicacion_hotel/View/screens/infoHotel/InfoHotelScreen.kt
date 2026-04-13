package com.example.aplicacion_hotel.View.screens.infoHotel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InfoHotelScreen() {

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background 
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Hotel Pere María",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Elegancia, confort y vistas al Mediterráneo",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Sobre nosotros", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                    Text(
                        "Ubicado en primera línea de playa, el Hotel Pere María combina diseño contemporáneo con una experiencia acogedora y personalizada. Nuestro objetivo es ofrecer una estancia inolvidable tanto para viajes de negocios como para escapadas vacacionales."
                    )
                }
            }

            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Servicios destacados", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                    Text("• Piscina panorámica en la azotea")
                    Text("• Restaurante gourmet con cocina mediterránea")
                    Text("• Spa & centro de bienestar")
                    Text("• WiFi gratuito en todas las instalaciones")
                    Text("• Recepción 24 horas")
                }
            }

            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Contacto", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                    Text("📍 Avenida del Mar 25, Benidorm, España")
                    Text("📞 +34 965 000 000")
                    Text("✉️ info@HotelPereMaria.com")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}