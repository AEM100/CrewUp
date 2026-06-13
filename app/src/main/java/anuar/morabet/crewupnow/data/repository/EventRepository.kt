package anuar.morabet.crewupnow.data.repository

import android.util.Log
import anuar.morabet.crewupnow.EventPanel.UpcomingEventItem
import anuar.morabet.crewupnow.mapa.data.MapEvent
import anuar.morabet.crewupnow.network.Locator
import anuar.morabet.crewupnow.paneleUsuario.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONObject

/**
 * Única fuente de verdad para los eventos de la aplicación.
 * Permite que diferentes ViewModels (Mapa, Perfil, Lista) vean los mismos datos.
 */


object EventRepository {
    private val TAG = "DEBUG_MAP_VM"
    private val _events = MutableStateFlow<List<MapEvent>>(emptyList())
    val events: StateFlow<List<MapEvent>> = _events.asStateFlow()
    private val socketClient = Locator.socketClient

    fun addEvent(event: MapEvent) {
        Log.d(TAG, "Repo: Añadiendo evento localmente ID: ${event.id}")
        _events.update { currentList -> currentList + event }
    }

    fun toggleJoinEvent(eventId: String) {
        Log.d(TAG, "Repo: ToggleJoin local ID: $eventId")
        _events.update { list ->
            list.map { event ->
                if (event.id == eventId) {
                    val newJoinedState = !event.isUserJoined
                    event.copy(
                        isUserJoined = newJoinedState,
                        participantsCount = if (newJoinedState) event.participantsCount + 1 else event.participantsCount - 1
                    )
                } else event
            }
        }
    }

    fun clear() {
        Log.d(TAG, "Repo: Limpiando eventos")
        _events.value = emptyList()
    }

    suspend fun persistirNuevoEvento(title: String, desc: String, lat: Double, lng: Double, userId: Int, fecha: String): Int? {
        return try {
            val json = JSONObject().apply {
                put("action", "CREATE_EVENT")
                put("title", title)
                put("description", desc)
                put("ubicacion", "$lat, $lng")
                put("userId", userId)
                put("fecha", fecha)
            }
            Log.d(TAG, "Repo: Enviando CREATE_EVENT")
            val response = socketClient.sendRequest(json.toString())
            val resJson = JSONObject(response ?: "")

            if (resJson.getString("status") == "SUCCESS") {
                Log.d(TAG, "Repo: Evento creado con éxito. ID: ${resJson.getInt("id")}")
                resJson.getInt("id")
            } else {
                Log.e(TAG, "Repo: Error al crear evento: ${resJson.optString("message")}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Repo: Excepción en persistirNuevoEvento: ${e.message}")
            null
        }
    }

    suspend fun persistirBanUser(userIdToBan: Int, adminId: Int): Boolean {
        return try {
            Log.d(TAG, "Repo: Enviando BAN_USER (Target: $userIdToBan, Admin: $adminId)")
            val json = JSONObject().apply {
                put("action", "BAN_USER")
                put("userIdToBan", userIdToBan)
                put("adminId", adminId)
            }
            val response = socketClient.sendRequest(json.toString())
            val status = JSONObject(response ?: "").getString("status") == "SUCCESS"
            Log.d(TAG, "Repo: Resultado BAN_USER: $status")
            status
        } catch (e: Exception) {
            Log.e(TAG, "Repo: Excepción en persistirBanUser: ${e.message}")
            false
        }
    }

    suspend fun persistirToggleJoin(eventId: String, userId: Int, join: Boolean): Boolean {
        return try {
            Log.d(TAG, "Repo: Enviando TOGGLE_JOIN (Event: $eventId, Join: $join)")
            val json = JSONObject().apply {
                put("action", "TOGGLE_JOIN")
                put("eventId", eventId.toInt())
                put("userId", userId)
                put("join", join)
            }
            val response = socketClient.sendRequest(json.toString())
            JSONObject(response ?: "").getString("status") == "SUCCESS"
        } catch (e: Exception) {
            Log.e(TAG, "Repo: Excepción en persistirToggleJoin: ${e.message}")
            false
        }
    }

    suspend fun cargarEventosDelServidor() {
        try {
            val userId = SessionManager.currentUser?.id
            Log.d(TAG, "Repo: Cargando eventos para usuario: $userId")
            val json = JSONObject().apply {
                put("action", "FETCH_EVENTS")
                put("userId", userId)
            }
            val response = socketClient.sendRequest(json.toString())
            val resJson = JSONObject(response ?: "")

            if (resJson.getString("status") == "SUCCESS") {
                val array = resJson.getJSONArray("events")
                val listaParseada = mutableListOf<MapEvent>()

                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val ubicacionStr = obj.getString("ubicacion")
                    val partes = ubicacionStr.split(",")
                    val coords = com.google.android.gms.maps.model.LatLng(
                        partes[0].trim().toDouble(),
                        partes[1].trim().toDouble()
                    )

                    listaParseada.add(
                        MapEvent(
                            id = obj.getInt("id").toString(),
                            title = obj.getString("title"),
                            description = obj.getString("description"),
                            date = if (obj.has("fecha")) obj.getString("fecha") else "Hoy",
                            organizer = obj.getString("organizer"),
                            creatorId = obj.getInt("creatorId"),
                            coordinates = coords,
                            participantsCount = obj.getInt("participantsCount"),
                            isUserJoined = obj.optBoolean("isUserJoined", false)
                        )
                    )
                }
                Log.d(TAG, "Repo: Eventos cargados: ${listaParseada.size}")
                _events.value = listaParseada
            }
        } catch (e: Exception) {
            Log.e(TAG, "Repo: Error en cargarEventosDelServidor: ${e.message}")
        }
    }

    suspend fun cargarMisProximosEventos(userId: Int): List<UpcomingEventItem> {
        return try {
            Log.d(TAG, "Repo: Cargando mis eventos para usuario: $userId")
            val json = JSONObject().apply {
                put("action", "FETCH_MY_EVENTS")
                put("userId", userId)
            }
            val response = socketClient.sendRequest(json.toString())
            val resJson = JSONObject(response ?: "")
            val listaResult = mutableListOf<UpcomingEventItem>()

            if (resJson.getString("status") == "SUCCESS") {
                val array = resJson.getJSONArray("events")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)

                    val ubicaStr = obj.getString("ubicacion")
                    val partes = ubicaStr.split(",")
                    val lat = partes[0].trim().toDouble()
                    val lng = partes[1].trim().toDouble()
                    val rawDate = obj.getString("fecha")

                    val (fechaFormateada, horaFormateada) = procesarFechaYHora(rawDate)

                    listaResult.add(
                        UpcomingEventItem(
                            id = obj.get("id").toString(),
                            title = obj.getString("title"),
                            description = obj.getString("description"),
                            rawDate = rawDate,
                            formattedDate = fechaFormateada,
                            formattedTime = horaFormateada,
                            organizer = obj.getString("organizer"),
                            lat = lat,
                            lng = lng
                        )
                    )
                }
            }
            listaResult
        } catch (e: Exception) {
            Log.e(TAG, "Repo: Error en cargarMisProximosEventos: ${e.message}")
            emptyList()
        }
    }

    private fun procesarFechaYHora(fechaIso: String): Pair<String, String> {
        return try {
            val partes = fechaIso.split("T")
            val fechaPartes = partes[0].split("-")
            val horaPartes = partes[1].split(":")

            val meses = arrayOf("ENE", "FEB", "MAR", "ABR", "MAY", "JUN", "JUL", "AGO", "SEP", "OCT", "NOV", "DIC")
            val mesInt = fechaPartes[1].toInt() - 1
            val mesTexto = if (mesInt in 0..11) meses[mesInt] else "EVENTO"

            val fechaBonita = "${fechaPartes[2]}\n$mesTexto"
            val horaBonita = "${horaPartes[0]}:${horaPartes[1]}h"

            Pair(fechaBonita, horaBonita)
        } catch (e: Exception) {
            Pair("??\nEVT", "--:--h")
        }
    }

    suspend fun persistirEliminarEvento(eventId: String): Boolean {
        return try {
            Log.d(TAG, "Repo: Enviando DELETE_EVENT para ID: $eventId")
            val json = JSONObject().apply {
                put("action", "DELETE_EVENT")
                put("eventId", eventId.toInt())
            }
            val response = socketClient.sendRequest(json.toString())
            JSONObject(response ?: "").getString("status") == "SUCCESS"
        } catch (e: Exception) {
            Log.e(TAG, "Repo: Error en persistirEliminarEvento: ${e.message}")
            false
        }
    }

    fun removeEventLocal(eventId: String) {
        Log.d(TAG, "Repo: Eliminando evento localmente ID: $eventId")
        _events.update { currentList -> currentList.filterNot { it.id == eventId } }
    }
}