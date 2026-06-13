package anuar.morabet.crewupnow.chat

data class MessageItem(
    val id: Int,
    val senderId: Int,
    val senderName: String,
    val content: String,
    val isMine: Boolean,
    val timestamp: String
)