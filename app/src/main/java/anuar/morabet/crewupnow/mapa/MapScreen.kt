package anuar.morabet.crewupnow.mapa

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import anuar.morabet.crewupnow.BottomBar4Tabs
import anuar.morabet.crewupnow.evento.CreateEventDialog
import anuar.morabet.crewupnow.evento.EventDetailsDialog
import anuar.morabet.crewupnow.evento.FloatingAddButton
import anuar.morabet.crewupnow.evento.TopBannerHeader
import anuar.morabet.crewupnow.mapa.data.MapUiState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

@Composable
fun MapScreen(
    state: MapUiState,
    onAction: (MapUiAction) -> Unit
) {

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(40.4168, -3.7038), 12f)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {

            MapViewContent(
                state = state,
                cameraPositionState = cameraPositionState,
                onAction = onAction
            )

            TopBannerHeader(
                isSelectionModeActive = state.isSelectionModeActive,
                onAction = onAction
            )

            FloatingAddButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding( bottom = 20.dp),
                isSelectionModeActive = state.isSelectionModeActive,
                onAction = onAction
            )
        }

        BottomBar4Tabs(
            selectedTab = state.selectedTab,
            onAction = onAction
        )
    }

    if (state.showCreateDialog) {
        CreateEventDialog(
            state = state,
            onAction = onAction
        )
    }

    state.selectedEventForDetails?.let { event ->
        EventDetailsDialog(
            event = event,
            onAction = onAction
        )
    }
}


//mostrar mapa y todos los eventos
@Composable
fun MapViewContent(
    state: MapUiState,
    cameraPositionState: CameraPositionState,
    onAction: (MapUiAction) -> Unit
) {
    val context = LocalContext.current

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onMapClick = { onAction(MapUiAction.OnMapClicked(it)) }
    ) {
        // Pintar Eventos Guardados
        state.eventsList.forEach { event ->
            val customTagIcon = remember(event.title, event.isUserJoined) {
                createEventTagBitmap(context, event.title, event.isUserJoined)
            }

            Marker(
                state = rememberMarkerState(position = event.coordinates),
                icon = customTagIcon,
                title = event.title,
                onClick = {
                    onAction(MapUiAction.OnEventTagClicked(event))
                    true
                }
            )
        }

        // Pintar Marcador Temporal de Creación
        state.pendingEventCoords?.let { coords ->
            val pendingIcon = remember {
                createEventTagBitmap(context, "Nuevo Evento...", false, isPending = true)
            }
            Marker(
                state = rememberMarkerState(position = coords),
                icon = pendingIcon,
                alpha = 0.9f
            )
        }
    }
}