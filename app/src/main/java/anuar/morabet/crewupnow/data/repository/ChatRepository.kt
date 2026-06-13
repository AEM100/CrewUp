package anuar.morabet.crewupnow.data.repository

import anuar.morabet.crewupnow.chat.ChatHeader
import anuar.morabet.crewupnow.network.Locator

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.collections.emptyList
import android.util.Log
import anuar.morabet.crewupnow.chat.RawMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

object ChatRepository {

    // 1. Estados reactivos idénticos a los de tus Eventos
    private val _chats = MutableStateFlow<List<ChatHeader>>(emptyList())
    val chats: StateFlow<List<ChatHeader>> = _chats.asStateFlow()

    private val _mensajesActuales = MutableStateFlow<List<RawMessage>>(emptyList())
    val mensajesActuales: StateFlow<List<RawMessage>> = _mensajesActuales.asStateFlow()

    // 2. Tu cliente de Sockets del Locator
    private val socketClient = Locator.socketClient

    /**
     * FETCH_CHAT_LIST: Obtiene los chats usando JSONObject nativo
     */
    suspend fun cargarMisChats(userId: Int) = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("action", "FETCH_CHAT_LIST")
                put("userId", userId)
            }

            val response = socketClient.sendRequest(json.toString())
            if (response.isNullOrBlank()) return@withContext

            val resJson = JSONObject(response)
            if (resJson.optString("status") == "SUCCESS") {
                val array = resJson.getJSONArray("chats")
                val listaParseada = mutableListOf<ChatHeader>()

                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    listaParseada.add(
                        ChatHeader(
                            chatId = obj.getInt("chatId").toString(),
                            chatTitle = obj.optString("title", "Chat Grupal")
                        )
                    )
                }
                _chats.value = listaParseada
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error al cargar chats en segundo plano", e)
        }
    }

    /**
     * FETCH_CHAT_MESSAGES: Protegido con IO y uso de 'optString' antibugs
     */
    suspend fun cargarMensajes(chatId: String) = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("action", "FETCH_CHAT_MESSAGES")
                put("chatId", chatId.toInt())
            }

            val response = socketClient.sendRequest(json.toString())
            if (response.isNullOrBlank()) return@withContext

            val resJson = JSONObject(response)
            if (resJson.optString("status") == "SUCCESS") {
                val array = resJson.getJSONArray("messages")
                val listaParseada = mutableListOf<RawMessage>()

                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    listaParseada.add(
                        RawMessage(
                            id = obj.optInt("id", 0),
                            senderId = obj.getInt("senderId"),
                            senderName = obj.optString("senderName", "Usuario"),
                            content = obj.optString("content", ""),
                            type = obj.optString("type", "text"),
                            timestamp = obj.optString("timestamp", "")
                        )
                    )
                }
                _mensajesActuales.value = listaParseada
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error al cargar mensajes en segundo plano", e)
        }
    }

    /**
     * SEND_CHAT_MESSAGE: Envío seguro por red
     */
    suspend fun enviarMensaje(chatId: String, userId: Int, contenido: String): Boolean = withContext(
        Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("action", "SEND_CHAT_MESSAGE")
                put("chatId", chatId.toInt())
                put("userId", userId)
                put("content", contenido)
            }

            val response = socketClient.sendRequest(json.toString())
            if (response.isNullOrBlank()) return@withContext false

            val resJson = JSONObject(response)
            val esExito = resJson.optString("status") == "SUCCESS"

            if (esExito) {
                cargarMensajes(chatId)
            }
            esExito
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error al enviar mensaje", e)
            false
        }
    }
    fun clear() {
        _chats.value = emptyList()
        _mensajesActuales.value = emptyList()
    }
}