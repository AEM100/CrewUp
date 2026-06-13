package anuar.morabet.crewupnow.paneleUsuario

import anuar.morabet.crewupnow.chat.ChatHeader
import anuar.morabet.crewupnow.mapa.data.MapEvent

data class ProfileUiState(

    val isLoading: Boolean = false,

    val user: UserProfile? = null,

    val isOwnProfile: Boolean = true,

    val isFollowing: Boolean = false,

    val createdEvents: List<Event> = emptyList(),

    val participatedEvents: List<Event> = emptyList(),
    val activeChats: List<ChatHeader> = emptyList(),
    val selectedEventForDetails: MapEvent? = null,
    val error: String? = null
)