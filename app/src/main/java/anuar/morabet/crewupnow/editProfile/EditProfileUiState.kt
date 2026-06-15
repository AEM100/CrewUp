package anuar.morabet.crewupnow.editProfile

data class EditProfileUiState(
    val isLoading: Boolean = false,
    val name: String = "",
    val email: String = "",
    val bio: String = "",
    val password: String = "",
    val fotoBase64: String = "",
    val isSaveSuccess: Boolean = false,
    val error: String? = null
)