package anuar.morabet.crewupnow.editProfile

data class EditProfileUiState(
    val isLoading: Boolean = false,
    val name: String = "",
    val bio: String = "",
    val avatarUrl: String = "", // Reservado para la foto más adelante
    val isSaveSuccess: Boolean = false, // Nos indicará cuándo volver atrás
    val error: String? = null
)

