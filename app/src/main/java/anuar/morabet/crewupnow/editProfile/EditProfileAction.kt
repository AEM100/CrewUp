package anuar.morabet.crewupnow.editProfile

sealed interface EditProfileDataAction {
    data class OnNameChanged(val newName: String) : EditProfileDataAction
    data class OnBioChanged(val newBio: String) : EditProfileDataAction
    data object OnSaveClicked : EditProfileDataAction
    data object OnChangeAvatarClicked : EditProfileDataAction // Marcador para la foto
}