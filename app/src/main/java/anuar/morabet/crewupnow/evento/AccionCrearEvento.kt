package anuar.morabet.crewupnow.evento

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import anuar.morabet.crewupnow.mapa.MapUiAction

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