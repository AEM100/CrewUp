package anuar.morabet.crewupnow.auth

sealed interface AuthUiState {

    data object Login : AuthUiState

    data object Register : AuthUiState

    data object Loading : AuthUiState

    data class Authenticated(
        val user: UserSession
    ) : AuthUiState

    data class Error(
        val message: String
    ) : AuthUiState
}