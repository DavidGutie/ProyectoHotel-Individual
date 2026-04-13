package com.example.aplicacion_hotel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.aplicacion_hotel.View.navigation.AppNavigation
import com.example.aplicacion_hotel.utils.SessionManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val sessionManager = SessionManager(this)
            val startDestination =
                if (sessionManager.getToken() != null) "home"
                else "login"

            AppNavigation()
        }
    }
}
