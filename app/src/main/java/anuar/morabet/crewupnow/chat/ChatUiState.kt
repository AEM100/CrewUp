package anuar.morabet.crewupnow.chat


data class ChatUiState(
    val messages: List<MessageItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)




