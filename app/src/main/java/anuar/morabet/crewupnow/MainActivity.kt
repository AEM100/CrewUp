package anuar.morabet.crewupnow

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import anuar.morabet.crewupnow.mapa.MapUiAction
import anuar.morabet.crewupnow.mapa.MapViewModel
import anuar.morabet.crewupnow.mapa.createEventTagBitmap
import anuar.morabet.crewupnow.mapa.data.MapEvent
import anuar.morabet.crewupnow.mapa.data.MapUiState
import anuar.morabet.crewupnow.panelPrincipal.ViewModelPrincipal
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*



// ============================================================================
// VISTA CONTROLADORA DE ENTRADA (ACTIVITY / VIEWMODEL BINDING)
// ============================================================================
class MainActivity : ComponentActivity() {

    // El ViewModel se instancia a nivel de Activity, NUNCA dentro de la vista Compose.
    private val viewModel: MapViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapsInitializer.initialize(applicationContext)

        setContent {
            MaterialTheme {
                val state by viewModel.uiState.collectAsState()

                // Pasamos el estado inmutable y el gestor de acciones directamente.
                // Cumple con el patrón UDF (Unidirectional Data Flow).
                MapScreen(
                    state = state,
                    onAction = { action -> viewModel.onAction(action) }
                )
            }
        }
    }
}

// ============================================================================
// VISTA PRINCIPAL (MAP SCREEN - STATELESS)
// ============================================================================
@Composable
fun MapScreen(
    state: MapUiState,
    onAction: (MapUiAction) -> Unit
) {

    // Estado de la cámara sobre Madrid (mantenido localmente en la UI por rendimiento de renderizado del SDK de mapas)
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

    // 📝 COMPONENTE: DIÁLOGO DE CREACIÓN
    if (state.showCreateDialog) {
        CreateEventDialog(
            state = state,
            onAction = onAction
        )
    }

    // ℹ️ COMPONENTE: DIÁLOGO DE DETALLES DEL EVENTO
    state.selectedEventForDetails?.let { event ->
        EventDetailsDialog(
            event = event,
            onAction = onAction
        )
    }
}

// ============================================================================
// COMPONENTES DE VISTA SEPARADOS Y MODULARES
// ============================================================================

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

@Composable
fun TopBannerHeader(
    isSelectionModeActive: Boolean,
    onAction: (MapUiAction) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .statusBarsPadding(),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelectionModeActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        tonalElevation = 4.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = if (isSelectionModeActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isSelectionModeActive) {
                    "Toca el mapa para marcar la ubicación del evento"
                } else {
                    "Pulsa en las etiquetas para unirte al evento"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (isSelectionModeActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            if (isSelectionModeActive) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { onAction(MapUiAction.OnCancelSelectionMode) }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cancelar")
                }
            }
        }
    }
}

@Composable
fun FloatingAddButton(
    modifier: Modifier = Modifier,
    isSelectionModeActive: Boolean,
    onAction: (MapUiAction) -> Unit
) {
    FloatingActionButton(
        onClick = { onAction(MapUiAction.OnToggleSelectionMode) },
        modifier = modifier,
        containerColor = if (isSelectionModeActive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
        contentColor = if (isSelectionModeActive) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onPrimary
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Nuevo")
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isSelectionModeActive) "Modo Selección..." else "Nuevo Evento",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CreateEventDialog(
    state: MapUiState,
    onAction: (MapUiAction) -> Unit
) {
    Dialog(onDismissRequest = { onAction(MapUiAction.OnCancelCreateEvent) }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Crear nuevo evento",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = state.newEventTitle,
                    onValueChange = { onAction(MapUiAction.OnNewTitleChanged(it)) },
                    label = { Text("Nombre del Evento") },
                    placeholder = { Text("Ej. Torneo Vóley") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = state.newEventDesc,
                    onValueChange = { onAction(MapUiAction.OnNewDescChanged(it)) },
                    label = { Text("Breve descripción") },
                    placeholder = { Text("¿Qué vais a hacer?") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onAction(MapUiAction.OnCancelCreateEvent) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = { onAction(MapUiAction.OnConfirmCreateEvent) },
                        enabled = state.newEventTitle.isNotBlank(),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text("Crear Evento")
                    }
                }
            }
        }
    }
}

@Composable
fun EventDetailsDialog(
    event: MapEvent,
    onAction: (MapUiAction) -> Unit
) {
    Dialog(onDismissRequest = { onAction(MapUiAction.OnDismissDetails) }) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column {
                        Text(
                            text = event.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Organizado por ${event.organizer}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Fecha y hora:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = event.date, style = MaterialTheme.typography.bodySmall)
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "${event.participantsCount} apuntados",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onAction(MapUiAction.OnDismissDetails) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cerrar")
                    }

                    Button(
                        onClick = { onAction(MapUiAction.OnToggleJoinEvent(event)) },
                        modifier = Modifier.weight(1.5f),
                        colors = if (event.isUserJoined) {
                            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        } else {
                            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        }
                    ) {
                        if (event.isUserJoined) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Salir del evento")
                        } else {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Apuntarme")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomBar4Tabs(
    selectedTab: Int,
    onAction: (MapUiAction) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onAction(MapUiAction.OnTabSelected(0)) },
            icon = { Text("🏠") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onAction(MapUiAction.OnTabSelected(1)) },
            icon = { Text("🗺️") },
            label = { Text("Mapa") }
        )
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onAction(MapUiAction.OnTabSelected(2)) },
            icon = { Text("📅") },
            label = { Text("Eventos") }
        )
        NavigationBarItem(
            selected = selectedTab == 3,
            onClick = { onAction(MapUiAction.OnTabSelected(3)) },
            icon = { Text("⚙️") },
            label = { Text("Ajustes") }
        )
    }
}

