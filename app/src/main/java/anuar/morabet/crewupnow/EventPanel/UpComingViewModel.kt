package anuar.morabet.crewupnow.EventPanel

import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import anuar.morabet.crewupnow.data.repository.EventRepository
import anuar.morabet.crewupnow.paneleUsuario.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class UpcomingEventsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<UpcomingEventsUiState>(UpcomingEventsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        obtenerProximosEventos()
    }

    fun obtenerProximosEventos() {
        val currentUser = SessionManager.currentUser
        if (currentUser == null) {
            _uiState.update { it.copy(errorMessage = "No has iniciado sesión.", isLoading = false) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val eventos = EventRepository.cargarMisProximosEventos(currentUser.id)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    eventsList = eventos
                )
            }
        }
    }

    // 🗺️ Traduce coordenadas a dirección real usando el Geocoder de Android
    fun buscarDireccionPostal(context: Context, eventId: String, lat: Double, lng: Double) {
        val listaActual = _uiState.value.eventsList
        val evento = listaActual.find { it.id == eventId }

        // Si ya calculamos la dirección de este evento, ignoramos para no repetir trabajo
        if (evento == null || evento.address != "Cargando ubicación...") return

        viewModelScope.launch {
            val direccionObtenida = withContext(Dispatchers.IO) {
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val direcciones = geocoder.getFromLocation(lat, lng, 1)
                    if (!direcciones.isNullOrEmpty()) {
                        direcciones[0].getAddressLine(0) ?: "$lat, $lng"
                    } else {
                        "$lat, $lng"
                    }
                } catch (e: Exception) {
                    "$lat, $lng"
                }
            }

            // Buscamos el evento en la lista y le encasquetamos la dirección real
            _uiState.update { estado ->
                estado.copy(
                    eventsList = estado.eventsList.map { item ->
                        if (item.id == eventId) item.copy(address = direccionObtenida) else item
                    }
                )
            }
        }
    }
}