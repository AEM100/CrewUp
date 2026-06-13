package anuar.morabet.crewupnow.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import anuar.morabet.crewupnow.data.repository.ChatRepository
import anuar.morabet.crewupnow.paneleUsuario.SessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class ChatViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null
    private var currentChatId: String? = null

    /**
     * Inicializa el chat y arranca el bucle síncrono por Sockets (Polling)
     */
    fun initChat(chatId: String) {
        if (currentChatId == chatId) return // Evita reiniciar si ya estamos en este chat
        currentChatId = chatId

        // Cancelamos cualquier bucle de fondo previo por seguridad
        pollingJob?.cancel()

        // Arrancamos el bucle reactivo de actualización (Cada 3 segundos)
        pollingJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val currentUserId = SessionManager.currentUser?.id ?: return@launch

            while (true) {
                try {
                    // 1. 🔥 CORREGIDO: Le pedimos al repositorio nativo que cargue los mensajes
                    ChatRepository.cargarMensajes(chatId)

                    // 2. 🔥 CORREGIDO: Leemos los datos frescos del StateFlow del repositorio
                    val rawMessages = ChatRepository.mensajesActuales.value

                    // 3. Mapeamos la lista a MessageItem calculando el flag 'isMine' en tiempo real
                    val mappedMessages = rawMessages.map { msg ->
                        MessageItem(
                            id = msg.id,
                            senderId = msg.senderId,
                            senderName = msg.senderName,
                            content = msg.content,
                            isMine = msg.senderId == currentUserId,
                            timestamp = msg.timestamp
                        )
                    }

                    // 4. Actualizamos el estado de la UI para Jetpack Compose
                    _uiState.update {
                        it.copy(messages = mappedMessages, isLoading = false, error = null)
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(error = "Error al conectar con el servidor", isLoading = false)
                    }
                }
                delay(3000) // Espera 3 segundos antes de volver a preguntar a Java
            }
        }
    }

    /**
     * Centralizador de acciones que vienen desde la interfaz gráfica (ChatWindow)
     */
    fun onAction(action: ChatAction, onBackNav: () -> Unit) {
        when (action) {
            is ChatAction.SendMessage -> enviarMensajeTexto(action.text)
            is ChatAction.RefreshMessages -> currentChatId?.let { initChat(it) }
            is ChatAction.BackClicked -> {
                limpiarChat()
                onBackNav() // Ejecuta el popBackStack del NavController
            }
        }
    }

    /**
     * Envía un mensaje de texto al SocketServer
     */
    private fun enviarMensajeTexto(text: String) {
        val chatId = currentChatId ?: return
        val userId = SessionManager.currentUser?.id ?: return

        viewModelScope.launch {
            // Enviamos a través de la conexión activa de tu Locator
            val exito = ChatRepository.enviarMensaje(chatId, userId, text)

            if (exito) {
                // Al enviarse con éxito, el repositorio ya autocarga el histórico.
                // Simplemente recuperamos el nuevo valor del StateFlow para pintar nuestro mensaje al instante
                val rawMessages = ChatRepository.mensajesActuales.value
                val mappedMessages = rawMessages.map { msg ->
                    MessageItem(
                        id = msg.id,
                        senderId = msg.senderId,
                        senderName = msg.senderName,
                        content = msg.content,
                        isMine = msg.senderId == userId,
                        timestamp = msg.timestamp
                    )
                }
                _uiState.update { it.copy(messages = mappedMessages) }
            }
        }
    }

    /**
     * Limpia las variables de control y mata el hilo de fondo
     */
    private fun limpiarChat() {
        pollingJob?.cancel()
        pollingJob = null
        currentChatId = null
    }

    /**
     * Si el usuario destruye la pantalla por completo, nos aseguramos de no dejar
     * bucles infinitos gastando batería en segundo plano
     */
    override fun onCleared() {
        super.onCleared()
        limpiarChat()
    }
}