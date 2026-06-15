package anuar.morabet.crewupnow.paneleUsuario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import anuar.morabet.crewupnow.data.repository.EventRepository
import anuar.morabet.crewupnow.mapa.data.MapEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import anuar.morabet.crewupnow.data.repository.ChatRepository

class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    init {
        // 1. Cargamos el perfil inicial
        loadProfile()
        // 2. Escuchamos de forma activa los eventos, chats... ¡y la sesión!
        observeData()
    }

    private fun observeData() {
        val currentUserId = SessionManager.currentUser?.id ?: return

        viewModelScope.launch {
            ChatRepository.cargarMisChats(currentUserId)
        }

        // 🔄 NUEVO ENFOQUE REACTIVO: Quitamos el parche de la vista.
        // Cada vez que el estado de los eventos cambie, aprovechamos para leer
        // los datos más frescos de SessionManager.currentUser (que ya tiene la nueva bio)
        viewModelScope.launch {
            EventRepository.events.collect { allEvents ->
                _uiState.update { state ->
                    val currentId = SessionManager.currentUser?.id ?: -1
                    val session = SessionManager.currentUser // 👈 Leemos la sesión actual aquí

                    val creadosFiltrados = allEvents
                        .filter { it.creatorId == currentId }
                        .map { it.toProfileEvent() }

                    val participadosFiltrados = allEvents
                        .filter { it.isUserJoined }
                        .map { it.toProfileEvent() }

                    state.copy(
                        createdEvents = creadosFiltrados,
                        // Actualizamos el usuario del estado con el nombre y bio más recientes de la sesión
                        user = state.user?.copy(
                            name = session?.name ?: state.user.name,
                            bio = session?.bio ?: state.user.bio,
                            organizedEventsCount = creadosFiltrados.size,
                            participatedEventsCount = participadosFiltrados.size
                        )
                    )
                }
            }
        }

        viewModelScope.launch {
            ChatRepository.chats.collect { listaChats ->
                _uiState.update { state -> state.copy(activeChats = listaChats) }
            }
        }
    }

    private fun MapEvent.toProfileEvent(): Event {
        return Event(
            id = this.id,
            title = this.title,
            date = this.date,
            location = "Ver en mapa",
            imageUrl = ""
        )
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val session = SessionManager.currentUser

            if (session != null) {
                val user = UserProfile(
                    name = session.name,
                    age = 24,
                    bio = session.bio,
                    avatarBase64 = session.fotoBase64,
                    organizedEventsCount = 0,
                    participatedEventsCount = 0
                )
                _uiState.update { it.copy(isLoading = false, user = user) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "No se encontraron datos de usuario.") }
            }
        }
    }

    fun onAction(action: ProfileAction, onNavigateToChat: (String, String) -> Unit = {_,_ ->}) {
        val currentUserId = SessionManager.currentUser?.id ?: return
        when (action) {
            ProfileAction.EditProfile -> { }
            ProfileAction.RefreshProfile -> {
                loadProfile()
                viewModelScope.launch { ChatRepository.cargarMisChats(currentUserId) }
            }
            is ProfileAction.EventClicked -> {
                val eventFull = EventRepository.events.value.find { it.id == action.eventId }
                _uiState.update { it.copy(selectedEventForDetails = eventFull) }
            }
            is ProfileAction.OnChatClicked -> {
                onNavigateToChat(action.chatId, action.chatTitle)
            }
        }
    }
    fun onDismissDetails() {
        _uiState.update { it.copy(selectedEventForDetails = null) }
    }
}