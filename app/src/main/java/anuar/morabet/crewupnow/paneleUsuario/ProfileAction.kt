package anuar.morabet.crewupnow.paneleUsuario

sealed interface ProfileAction {

    data object EditProfile : ProfileAction
    data object RefreshProfile : ProfileAction
    data class OnChatClicked(val chatId: String, val chatTitle: String) : ProfileAction
    data class EventClicked(
        val eventId: String
    ) : ProfileAction
}