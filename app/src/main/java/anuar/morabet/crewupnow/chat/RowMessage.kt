package anuar.morabet.crewupnow.chat


data class RawMessage(
    val id: Int,
    val senderId: Int,
    val senderName: String,
    val content: String,
    val type: String,
    val timestamp: String
)