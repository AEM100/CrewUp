package anuar.morabet.crewupnow

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔥 Inicialización obligatoria de Google Maps
        MapsInitializer.initialize(applicationContext)

        setContent {
            MapScreen()
        }
    }
}

@Composable
fun MapScreen() {

    var selectedTab by remember { mutableStateOf(0) }

    val context = LocalContext.current

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(40.4168, -3.7038),
            12f
        )
    }

    var mapLoaded by remember { mutableStateOf(false) }

    val markerIcon: BitmapDescriptor? = remember {
        createBitmapDescriptor(context)
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // 🗺️ MAPA (ocupa todo el espacio disponible)
        GoogleMap(
            modifier = Modifier.weight(1f),
            cameraPositionState = cameraPositionState,
            onMapLoaded = {
                mapLoaded = true
            }
        ) {

            if (mapLoaded && markerIcon != null) {
                Marker(
                    state = MarkerState(
                        position = LatLng(40.4168, -3.7038)
                    ),
                    title = "Evento",
                    icon = markerIcon
                )
            }
        }

        // 🔻 BARRA INFERIOR
        BottomBar4Tabs(
            selected = selectedTab,
            onSelected = { selectedTab = it }
        )
    }
}

@Composable
fun BottomBar4Tabs(
    selected: Int,
    onSelected: (Int) -> Unit
) {
    NavigationBar {

        NavigationBarItem(
            selected = selected == 0,
            onClick = { onSelected(0) },
            icon = { Text("🏠") },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = selected == 1,
            onClick = { onSelected(1) },
            icon = { Text("🗺️") },
            label = { Text("Mapa") }
        )

        NavigationBarItem(
            selected = selected == 2,
            onClick = { onSelected(2) },
            icon = { Text("📅") },
            label = { Text("Eventos") }
        )

        NavigationBarItem(
            selected = selected == 3,
            onClick = { onSelected(3) },
            icon = { Text("⚙️") },
            label = { Text("Ajustes") }
        )
    }
}

/**
 * 🔥 CREA ICONO SEGURO PARA MARKERS
 */
fun createBitmapDescriptor(context: android.content.Context): BitmapDescriptor? {
    return try {

        val drawable = ContextCompat.getDrawable(
            context,
            android.R.drawable.ic_menu_mylocation
        ) ?: return null

        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth.coerceAtLeast(1),
            drawable.intrinsicHeight.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        BitmapDescriptorFactory.fromBitmap(bitmap)

    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}