package anuar.morabet.crewupnow.login

sealed interface LoginUiState {

    data object Idle : LoginUiState

    data object Loading : LoginUiState

    data class Success(
        val userId: String
    ) : LoginUiState

    data class Error(
        val message: String
    ) : LoginUiState
}