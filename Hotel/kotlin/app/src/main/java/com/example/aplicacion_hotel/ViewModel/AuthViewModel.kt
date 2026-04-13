import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aplicacion_hotel.Repository.AuthRepository
import com.example.aplicacion_hotel.Repository.ClienteRepository
import com.example.aplicacion_hotel.View.navigation.Routes
import kotlinx.coroutines.launch

class AuthViewModel(
    private val sessionManager: com.example.aplicacion_hotel.utils.HotelSessionManager
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    private val repository = AuthRepository()

    var loginSuccess by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val clienteRepository = ClienteRepository()

    fun login(email: String, password: String) {
        Log.d("LOGIN_DEBUG", "Entrando al login con $email")

        viewModelScope.launch {

            try {
                isLoading = true
                errorMessage = null

                val response = repository.login(email, password)

                if (response.usuario.tipoUsuario != "Cliente") {
                    errorMessage = "Solo los clientes pueden iniciar sesión"
                    return@launch
                }

                sessionManager.saveToken(response.token)
                val clienteCompleto = clienteRepository.getClienteById(
                    response.usuario.id
                )
                sessionManager.saveCliente(clienteCompleto)

                loginSuccess = true

            } catch (e: Exception) {
                errorMessage = e.message
                Log.e("LOGIN_ERROR", e.toString())
                errorMessage = e.toString()
            }finally {
                isLoading = false
            }

        }

    }

}

