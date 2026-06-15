package anuar.morabet.crewupnow.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun AuthController(
    uiState: AuthUiState,
    onAction: (AuthAction) -> Unit,
    navigateToHome: () -> Unit,
    viewModel: AuthViewModel
) {
    val context = LocalContext.current

    // Verificamos si debemos mostrar el popup
    val isConnectionError = uiState is AuthUiState.Error &&
            uiState.message.contains("No se pudo conectar")

    if (isConnectionError) {
        ConnectionErrorDialog(
            onConfirm = { newIp ->
                viewModel.updateServerIp(newIp, context)
            }
        )
    }

    // Lógica de navegación de pantallas
    when (uiState) {
        is AuthUiState.Login -> LoginScreen(onAction = onAction)
        is AuthUiState.Register -> RegisterScreen(onAction = onAction)
        is AuthUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is AuthUiState.Authenticated -> {
            LaunchedEffect(Unit) { navigateToHome() }
        }
        is AuthUiState.Error -> {
            // Solo mostramos el Login con error si NO es de conexión
            if (!isConnectionError) {
                LoginScreen(onAction = onAction, error = uiState.message)
            }
        }
    }
}
@Composable
fun ConnectionErrorDialog(
    onConfirm: (String) -> Unit
) {
    var tempIp by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { /* Bloqueamos cierre accidental */ },
        title = { Text("Error de conexión") },
        text = {
            Column {
                Text("No pudimos conectar con el servidor. Por favor, introduce la IP correcta:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = tempIp,
                    onValueChange = { tempIp = it },
                    label = { Text("Nueva IP (Ej: 192.168.1.50)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(tempIp) }) {
                Text("Reintentar")
            }
        }
    )
}