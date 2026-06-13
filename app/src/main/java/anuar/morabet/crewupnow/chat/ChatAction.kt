package anuar.morabet.crewupnow.chat

sealed class ChatAction {
    data class SendMessage(val text: String) : ChatAction()
    object RefreshMessages : ChatAction()
    object BackClicked : ChatAction()
}