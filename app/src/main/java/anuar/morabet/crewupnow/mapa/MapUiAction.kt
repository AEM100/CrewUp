package anuar.morabet.crewupnow.mapa

import anuar.morabet.crewupnow.mapa.data.MapEvent
import com.google.android.gms.maps.model.LatLng

sealed interface MapUiAction {
    data class OnTabSelected(val tabIndex: Int) : MapUiAction
    data class OnMapClicked(val coordinates: LatLng) : MapUiAction
    object OnToggleSelectionMode : MapUiAction
    object OnCancelSelectionMode : MapUiAction
    data class OnEventTagClicked(val event: MapEvent) : MapUiAction
    data class OnNewTitleChanged(val title: String) : MapUiAction
    data class OnNewDescChanged(val description: String) : MapUiAction
    object OnCancelCreateEvent : MapUiAction
    object OnConfirmCreateEvent : MapUiAction
    object OnDismissDetails : MapUiAction
    data class OnToggleJoinEvent(val event: MapEvent) : MapUiAction
}