package anuar.morabet.crewupnow.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun AuthController(
    uiState: AuthUiState,
    onAction: (AuthAction) -> Unit,
    navigateToHome: () -> Unit
) {

    when (uiState) {

        AuthUiState.Login -> {
            LoginScreen(
                onAction = onAction,
            )
        }

        AuthUiState.Register -> {
            RegisterScreen(
                onAction = onAction
            )
        }

        AuthUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is AuthUiState.Authenticated -> {

            LaunchedEffect(Unit) {
                navigateToHome()
            }
        }

        is AuthUiState.Error -> {

            LoginScreen(
                onAction = onAction,
                error = uiState.message
            )
        }
    }
}