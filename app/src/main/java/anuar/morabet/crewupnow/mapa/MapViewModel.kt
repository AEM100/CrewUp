package anuar.morabet.crewupnow.mapa

import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import anuar.morabet.crewupnow.mapa.data.MapEvent
import anuar.morabet.crewupnow.mapa.data.MapUiState
import anuar.morabet.crewupnow.network.SocketClient
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState = _uiState.asStateFlow()
    private val socketClient = SocketClient()

    init {
        Log.d("ffffffffff", "iniciado")

        connectToServer()
    }

    private fun connectToServer() {

        viewModelScope.launch(Dispatchers.IO) {
            val connected =
                socketClient.connect(
                    host = "192.168.1.48",
                    port = 9000
                )

            Log.d("ffffffffff", connected.toString())
        }
    }

    fun onAction(action: MapUiAction) {
        when (action) {
            is MapUiAction.OnTabSelected -> {
                _uiState.value = _uiState.value.copy(selectedTab = action.tabIndex)
            }
            is MapUiAction.OnMapClicked -> {
                val state = _uiState.value
                if (state.isSelectionModeActive) {
                    _uiState.value = state.copy(
                        pendingEventCoords = action.coordinates,
                        showCreateDialog = true,
                        isSelectionModeActive = false
                    )
                }
            }
            MapUiAction.OnToggleSelectionMode -> {
                val currentMode = _uiState.value.isSelectionModeActive
                _uiState.value = _uiState.value.copy(isSelectionModeActive = !currentMode)
            }
            MapUiAction.OnCancelSelectionMode -> {
                _uiState.value = _uiState.value.copy(isSelectionModeActive = false)
            }
            is MapUiAction.OnEventTagClicked -> {
                _uiState.value = _uiState.value.copy(selectedEventForDetails = action.event)
            }
            is MapUiAction.OnNewTitleChanged -> {
                _uiState.value = _uiState.value.copy(newEventTitle = action.title)
            }
            is MapUiAction.OnNewDescChanged -> {
                _uiState.value = _uiState.value.copy(newEventDesc = action.description)
            }
            MapUiAction.OnCancelCreateEvent -> {
                _uiState.value = _uiState.value.copy(
                    showCreateDialog = false,
                    pendingEventCoords = null,
                    newEventTitle = "",
                    newEventDesc = ""
                )
            }
            MapUiAction.OnConfirmCreateEvent -> {
                val state = _uiState.value
                val coords = state.pendingEventCoords
                if (coords != null && state.newEventTitle.isNotBlank()) {
                    val newEvent = MapEvent(
                        id = (state.eventsList.size + 1).toString(),
                        title = state.newEventTitle,
                        description = state.newEventDesc,
                        date = "Hoy - 19:00h",
                        organizer = "Yo",
                        coordinates = coords,
                        participantsCount = 1
                    )
                    _uiState.value = state.copy(
                        eventsList = state.eventsList + newEvent,
                        showCreateDialog = false,
                        pendingEventCoords = null,
                        newEventTitle = "",
                        newEventDesc = ""
                    )
                }
            }
            MapUiAction.OnDismissDetails -> {
                _uiState.value = _uiState.value.copy(selectedEventForDetails = null)
            }
            is MapUiAction.OnToggleJoinEvent -> {
                val state = _uiState.value
                val updatedList = state.eventsList.map { event ->
                    if (event.id == action.event.id) {
                        event.copy(
                            isUserJoined = !event.isUserJoined,
                            participantsCount = if (event.isUserJoined) event.participantsCount - 1 else event.participantsCount + 1
                        )
                    } else event
                }
                _uiState.value = state.copy(
                    eventsList = updatedList,
                    selectedEventForDetails = null
                )
            }
        }
    }
}


// ============================================================================
// DIBUJADO DE MAP CHIPS DINÁMICOS
// ============================================================================
fun createEventTagBitmap(
    context: android.content.Context,
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

        val textPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = fontSize
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }

        val emoji = when {
            isPending -> "📍 "
            title.contains("Basket", ignoreCase = true) || title.contains("Baloncesto", ignoreCase = true) -> "🏀 "
            title.contains("Yoga", ignoreCase = true) || title.contains("Meditación", ignoreCase = true) -> "🧘 "
            title.contains("Vóley", ignoreCase = true) || title.contains("Volley", ignoreCase = true) -> "🏐 "
            title.contains("Clase", ignoreCase = true) || title.contains("Curso", ignoreCase = true) -> "📚 "
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

        val shadowPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(45, 0, 0, 0)
            style = android.graphics.Paint.Style.FILL
        }

        val bgPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = when {
                isPending -> android.graphics.Color.parseColor("#F57F17")
                isJoined -> android.graphics.Color.parseColor("#2E7D32")
                else -> android.graphics.Color.parseColor("#6200EE")
            }
            style = android.graphics.Paint.Style.FILL
        }

        val borderPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 2f * density
        }

        val offset = 2f * density

        val shadowRect = android.graphics.RectF(offset, offset, bubbleWidth + offset, bubbleHeight + offset)
        canvas.drawRoundRect(shadowRect, cornerRadius, cornerRadius, shadowPaint)
        val shadowPath = android.graphics.Path().apply {
            moveTo((bubbleWidth / 2f) - (6f * density) + offset, bubbleHeight + offset)
            lineTo((bubbleWidth / 2f) + (6f * density) + offset, bubbleHeight + offset)
            lineTo((bubbleWidth / 2f) + offset, bubbleHeight + triangleHeight + offset)
            close()
        }
        canvas.drawPath(shadowPath, shadowPaint)

        val rect = android.graphics.RectF(0f, 0f, bubbleWidth, bubbleHeight)
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint)
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint)

        val trianglePath = android.graphics.Path().apply {
            moveTo((bubbleWidth / 2f) - (6f * density), bubbleHeight - 1f)
            lineTo((bubbleWidth / 2f) + (6f * density), bubbleHeight - 1f)
            lineTo((bubbleWidth / 2f), bubbleHeight + triangleHeight)
            close()
        }
        canvas.drawPath(trianglePath, bgPaint)
        canvas.drawPath(trianglePath, borderPaint)

        val textX = horizontalPadding
        val textY = (bubbleHeight / 2f) - (fontMetrics.descent + fontMetrics.ascent) / 2f
        canvas.drawText(fullText, textX, textY, textPaint)

        BitmapDescriptorFactory.fromBitmap(bitmap)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}