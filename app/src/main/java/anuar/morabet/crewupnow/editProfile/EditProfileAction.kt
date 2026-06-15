package anuar.morabet.crewupnow.editProfile

sealed interface EditProfileDataAction {
    data class OnNameChanged(val newName: String) : EditProfileDataAction
    data class OnEmailChanged(val newEmail: String) : EditProfileDataAction
    data class OnBioChanged(val newBio: String) : EditProfileDataAction
    data class OnPasswordChanged(val newPassword: String) : EditProfileDataAction
    data class OnAvatarSelected(val base64: String) : EditProfileDataAction // Nueva acción para imagen
    data object OnSaveClicked : EditProfileDataAction
    data object OnChangeAvatarClicked : EditProfileDataAction
}