package anuar.morabet.crewupnow.mapa.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import anuar.morabet.crewupnow.evento.CreateEventDialog
import anuar.morabet.crewupnow.evento.EventDetailsDialog
import anuar.morabet.crewupnow.evento.FloatingAddButton
import anuar.morabet.crewupnow.evento.TopBannerHeader
import anuar.morabet.crewupnow.mapa.MapUiAction
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
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
        position = CameraPosition.fromLatLngZoom(
            LatLng(40.4168, -3.7038),
            12f
        )
    }

    Box(
        modifier = Modifier.fillMaxSize()
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
                .padding(20.dp),
            isSelectionModeActive = state.isSelectionModeActive,
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

@SuppressLint("RememberReturnType")
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

/**
 * Genera un BitmapDescriptor para los marcadores del mapa con estilo de "chip"
 */
fun createEventTagBitmap(
    context: Context,
    title: String,
    isJoined: Boolean,
    isPending: Boolean = false
): BitmapDescriptor? {
    return try {
        val density = context.resources.displayMetrics.density

        val fontSize = 13f * density
        val horizontalPadding = 12f * density
        val verticalPadding = 8f * density
        val cornerRadius = 16f * density
        val triangleHeight = 6f * density

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = fontSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val emoji = when {
            isPending -> "📍 "
            title.contains("Basket", ignoreCase = true) || title.contains("Baloncesto", ignoreCase = true) -> "🏀 "
            title.contains("Vóley", ignoreCase = true) || title.contains("Volley", ignoreCase = true) -> "🏐 "
            isJoined -> "✅ "
            else -> "✨ "
        }
        val fullText = "$emoji$title"

        val textWidth = textPaint.measureText(fullText)
        val fontMetrics = textPaint.fontMetrics
        val textHeight = fontMetrics.descent - fontMetrics.ascent

        val bubbleWidth = textWidth + (horizontalPadding * 2)
        val bubbleHeight = textHeight + (verticalPadding * 2)
        val bitmapWidth = (bubbleWidth + (4f * density)).toInt()
        val bitmapHeight = (bubbleHeight + triangleHeight + (4f * density)).toInt()

        val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = when {
                isPending -> android.graphics.Color.parseColor("#F57F17") // Naranja
                isJoined -> android.graphics.Color.parseColor("#2E7D32")  // Verde
                else -> android.graphics.Color.parseColor("#6200EE")      // Púrpura
            }
            style = Paint.Style.FILL
        }

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 2f * density
        }

        // Dibujar burbuja redondeada
        val rect = RectF(0f, 0f, bubbleWidth, bubbleHeight)
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint)
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint)

        // Dibujar el triángulo indicador inferior
        val trianglePath = Path().apply {
            moveTo((bubbleWidth / 2f) - (6f * density), bubbleHeight - 1f)
            lineTo((bubbleWidth / 2f) + (6f * density), bubbleHeight - 1f)
            lineTo((bubbleWidth / 2f), bubbleHeight + triangleHeight)
            close()
        }
        canvas.drawPath(trianglePath, bgPaint)
        canvas.drawPath(trianglePath, borderPaint)

        // Dibujar el texto centrado
        val textX = horizontalPadding
        val textY = (bubbleHeight / 2f) - (fontMetrics.descent + fontMetrics.ascent) / 2f
        canvas.drawText(fullText, textX, textY, textPaint)

        BitmapDescriptorFactory.fromBitmap(bitmap)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
