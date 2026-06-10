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
import anuar.morabet.crewupnow.evento.CreateEventDialog
import anuar.morabet.crewupnow.evento.EventDetailsDialog
import anuar.morabet.crewupnow.evento.FloatingAddButton
import anuar.morabet.crewupnow.evento.TopBannerHeader
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










