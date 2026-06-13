package anuar.morabet.crewupnow.mapa

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import anuar.morabet.crewupnow.data.repository.EventRepository
import anuar.morabet.crewupnow.mapa.data.MapEvent
import anuar.morabet.crewupnow.mapa.ui.MapUiState
import anuar.morabet.crewupnow.network.SocketClient
import anuar.morabet.crewupnow.paneleUsuario.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class MapViewModel : ViewModel() {

    private val TAG = "DEBUG_MAP_VM"
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState = _uiState.asStateFlow()

    init {
        Log.d(TAG, "MapViewModel inicializado")
        observeEvents()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            EventRepository.events.collect { events ->
                Log.d(TAG, "Eventos actualizados. Cantidad recibida: ${events.size}")
                _uiState.update { state ->
                    // 🔥 Si hay un evento abierto en detalles, buscamos su versión más fresca de la lista
                    val updatedDetails = state.selectedEventForDetails?.let { currentDetails ->
                        events.find { it.id == currentDetails.id }
                    }
                    state.copy(
                        eventsList = events,
                        selectedEventForDetails = updatedDetails // Mantendrá el ✅ o el número real
                    )
                }
            }
        }
    }

    fun onAction(action: MapUiAction) {
        Log.d(TAG, "Acción recibida: ${action::class.simpleName}")

        when (action) {
            is MapUiAction.OnTabSelected -> {
                _uiState.update { it.copy(selectedTab = action.tabIndex) }
            }
            is MapUiAction.OnMapClicked -> {
                val state = _uiState.value
                if (state.isSelectionModeActive) {
                    _uiState.update {
                        it.copy(
                            pendingEventCoords = action.coordinates,
                            showCreateDialog = true,
                            isSelectionModeActive = false
                        )
                    }
                }
            }
            MapUiAction.OnToggleSelectionMode -> {
                _uiState.update { it.copy(isSelectionModeActive = !it.isSelectionModeActive) }
            }
            MapUiAction.OnCancelSelectionMode -> {
                _uiState.update { it.copy(isSelectionModeActive = false) }
            }
            is MapUiAction.OnEventTagClicked -> {
                _uiState.update { it.copy(selectedEventForDetails = action.event) }
            }
            is MapUiAction.OnNewTitleChanged -> {
                _uiState.update { it.copy(newEventTitle = action.title) }
            }
            is MapUiAction.OnNewDescChanged -> {
                _uiState.update { it.copy(newEventDesc = action.description) }
            }
            is MapUiAction.OnDeleteEvent -> {
                viewModelScope.launch {
                    Log.d(TAG, "Intentando eliminar evento ID: ${action.eventId}")
                    // 1. Solicitamos borrar en MySQL mediante el socket
                    val exito = EventRepository.persistirEliminarEvento(action.eventId)

                    if (exito) {
                        Log.d(TAG, "Eliminación exitosa en servidor")
                        // 2. Si el servidor lo borró con éxito, lo quitamos de la UI local
                        EventRepository.removeEventLocal(action.eventId)

                        // 3. Cerramos el diálogo de detalles puesto que el evento ya no existe
                        _uiState.update { it.copy(selectedEventForDetails = null) }
                    } else {
                        Log.e(TAG, "Error al eliminar evento en servidor")
                    }
                }
            }
            MapUiAction.OnCancelCreateEvent -> {
                _uiState.update {
                    it.copy(
                        showCreateDialog = false,
                        pendingEventCoords = null,
                        newEventTitle = "",
                        newEventDesc = ""
                    )
                }
            }

            MapUiAction.OnConfirmCreateEvent -> {
                val state = _uiState.value
                val coords = state.pendingEventCoords
                val currentUser = SessionManager.currentUser

                if (coords != null && state.newEventTitle.isNotBlank() && state.newEventDate.isNotBlank() && currentUser != null) {
                    viewModelScope.launch {
                        Log.d(TAG, "Confirmando creación de evento por usuario: ${currentUser.id}")
                        // 1. Enviamos a Java y guardamos el ID real que nos devuelve MySQL
                        val realId = EventRepository.persistirNuevoEvento(
                            title = state.newEventTitle,
                            desc = state.newEventDesc,
                            lat = coords.latitude,
                            lng = coords.longitude,
                            userId = currentUser.id,
                            fecha = state.newEventDate
                        )

                        // 2. Si el servidor respondió con éxito (tenemos ID real)
                        if (realId != null) {
                            Log.d(TAG, "Evento creado con ID: $realId")
                            // 🔥 AQUÍ ESTÁ EL CAMBIO: Creamos el objeto con los datos exactos del servidor
                            val newEvent = MapEvent(
                                id = realId.toString(), // Usamos el ID de la base de datos, NO el timeMillis
                                title = state.newEventTitle,
                                description = state.newEventDesc,
                                date = state.newEventDate, // Usamos la fecha real, no "Hoy"
                                organizer = currentUser.name,
                                creatorId = currentUser.id, // 🔥 CRUCIAL: Añadimos el creatorId real
                                coordinates = coords,
                                participantsCount = 1,
                                isUserJoined = true
                            )

                            // Añadimos el evento real al repositorio
                            EventRepository.addEvent(newEvent)
                        } else {
                            Log.e(TAG, "Error: El servidor no devolvió un ID válido")
                        }

                        // 3. Reseteamos el estado
                        _uiState.update {
                            it.copy(
                                showCreateDialog = false,
                                pendingEventCoords = null,
                                newEventTitle = "",
                                newEventDesc = "",
                                newEventDate = ""
                            )
                        }
                    }
                }
            }

            MapUiAction.OnDismissDetails -> {
                _uiState.update { it.copy(selectedEventForDetails = null) }
            }

            // 🔥 CAMBIO 2: UNIRSE / SALIR DEL EVENTO REAL
            is MapUiAction.OnToggleJoinEvent -> {
                val currentUser = SessionManager.currentUser
                Log.d(TAG, "Toggle Join. Usuario: ${currentUser?.id}, Evento: ${action.event.id}")

                if (currentUser != null) {
                    viewModelScope.launch {
                        // Comprobamos si ya asistía para mandarle la acción inversa a Java
                        val actualmenteUnido = action.event.isUserJoined

                        // 1. Avisamos al servidor a través del socket
                        val exito = EventRepository.persistirToggleJoin(
                            eventId = action.event.id,
                            userId = currentUser.id,
                            join = !actualmenteUnido
                        )

                        // 2. Si MySQL actualizó la tabla intermedia correctamente, cambiamos el estado local
                        if (exito) {
                            Log.d(TAG, "Toggle Join exitoso en servidor")
                            EventRepository.toggleJoinEvent(action.event.id)
                        } else {
                            Log.e(TAG, "Error en Toggle Join en servidor")
                        }

                        _uiState.update { it.copy(selectedEventForDetails = null) }
                    }
                }
            }

            is MapUiAction.OnNewDateChanged -> {
                _uiState.update { it.copy(newEventDate = action.isoDateTime) }
            }

            is MapUiAction.OnBanUser -> {
                val currentUser = SessionManager.currentUser
                Log.d(TAG, "Intento de baneo. Admin actual: ${currentUser?.name}, EsAdmin: ${currentUser?.isAdmin}")

                // Solo si estamos seguros de que es Admin
                if (currentUser != null && currentUser.isAdmin) {
                    viewModelScope.launch {
                        // 1. Llamamos al servidor
                        Log.d(TAG, "Llamando a persistirBanUser para ID: ${action.userId}")
                        val exito = EventRepository.persistirBanUser(
                            userIdToBan = action.userId, // El ID del organizador que vamos a banear
                            adminId = currentUser.id      // Nuestro ID para validar permisos en Java
                        )

                        if (exito) {
                            Log.d(TAG, "Baneo exitoso")
                            // 2. Opcional: Si el baneo es exitoso, podríamos limpiar la UI
                            _uiState.update { it.copy(selectedEventForDetails = null) }

                            // 3. (Opcional) Recargar eventos para que los eventos de ese usuario desaparezcan
                            EventRepository.cargarEventosDelServidor()
                        } else {
                            Log.e(TAG, "Error al banear en servidor")
                        }
                    }
                } else {
                    Log.w(TAG, "Acción de baneo rechazada: usuario no es admin o es nulo")
                }
            }
        }
    }

    private fun formatearFechaParaUI(fechaIso: String): String {
        return try {
            val partes = fechaIso.split("T")
            val fechaComponentes = partes[0].split("-")
            val horaComponentes = partes[1].split(":")

            val dia = fechaComponentes[2]
            val mes = fechaComponentes[1]
            val año = fechaComponentes[0]
            val hora = horaComponentes[0]
            val minuto = horaComponentes[1]

            "$dia/$mes/$año a las $hora:${minuto}h"
        } catch (e: Exception) {
            e.printStackTrace()
            fechaIso
        }
    }
}