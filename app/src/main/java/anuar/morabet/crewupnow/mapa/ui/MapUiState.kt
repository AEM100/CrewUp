package anuar.morabet.crewupnow.mapa.ui

import anuar.morabet.crewupnow.mapa.data.MapEvent
import com.google.android.gms.maps.model.LatLng

data class MapUiState(
    val eventsList: List<MapEvent> = emptyList(),
    val selectedTab: Int = 1,
    val pendingEventCoords: LatLng? = null,
    val isSelectionModeActive: Boolean = false,
    val showCreateDialog: Boolean = false,
    val newEventTitle: String = "",
    val newEventDesc: String = "",
    val selectedEventForDetails: MapEvent? = null,
    val newEventDate: String = ""
)