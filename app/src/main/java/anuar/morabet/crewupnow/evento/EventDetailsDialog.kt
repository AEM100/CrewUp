package anuar.morabet.crewupnow.evento

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import anuar.morabet.crewupnow.mapa.MapUiAction
import anuar.morabet.crewupnow.mapa.data.MapEvent
import anuar.morabet.crewupnow.paneleUsuario.SessionManager
@Composable
fun EventDetailsDialog(
    event: MapEvent,
    onAction: (MapUiAction) -> Unit
) {

    val currentUser = SessionManager.currentUser
    val isOwner = event.creatorId.toString() == currentUser?.id?.toString()
    val isAdmin = currentUser?.isAdmin == true

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
                // 1. Cabecera del evento
                EventHeader(title = event.title, organizer = event.organizer)

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Descripción
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 2. Información de Fecha, Hora y Participantes
                EventDateTimeAndParticipants(date = event.date, count = event.participantsCount)

                // 3. Fila de botones principales (Control dinámico de roles)
                EventAdminActions(
                    event = event,
                    isAdmin = isAdmin,
                    isOwner = isOwner,
                    onAction = onAction
                )

                // 4. Botón de baneo (Solo si es Admin y no es su propio evento)
                if (isAdmin && !isOwner) {
                    BanUserButton(
                        organizerName = event.organizer,
                        onClick = { onAction(MapUiAction.OnBanUser(event.creatorId)) }
                    )
                }
            }
        }
    }
}

// ==========================================
// 🧱 SUB-COMPOSABLES COMPONENTIZADOS
// ==========================================

@Composable
fun EventHeader(title: String, organizer: String) {
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
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Organizado por $organizer",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EventDateTimeAndParticipants(date: String, count: Int) {
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
            Text(text = date, style = MaterialTheme.typography.bodySmall)
        }
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Text(
                text = "$count apuntados",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun EventAdminActions(
    event: MapEvent,
    isAdmin: Boolean,
    isOwner: Boolean,
    onAction: (MapUiAction) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Botón cerrar común para todos
        OutlinedButton(
            onClick = { onAction(MapUiAction.OnDismissDetails) },
            modifier = Modifier.weight(1f)
        ) {
            Text("Cerrar")
        }

        when {
            // Caso 1: Es Admin pero el evento es ajeno (Pinta Apuntarse + Eliminar)
            isAdmin && !isOwner -> {
                JoinToggleButton(event = event, onAction = onAction, modifier = Modifier.weight(1.2f))
                DeleteButton(onClick = { onAction(MapUiAction.OnDeleteEvent(event.id)) }, text = "Eliminar", modifier = Modifier.weight(1.2f))
            }
            // Caso 2: Es el dueño del evento (Solo puede eliminarlo)
            isOwner -> {
                DeleteButton(onClick = { onAction(MapUiAction.OnDeleteEvent(event.id)) }, text = "Eliminar Evento", modifier = Modifier.weight(1.5f))
            }
            // Caso 3: Es un usuario normal en un evento ajeno (Solo puede unirse/desapuntarse)
            else -> {
                JoinToggleButton(event = event, onAction = onAction, modifier = Modifier.weight(1.5f))
            }
        }
    }
}

@Composable
fun JoinToggleButton(
    event: MapEvent,
    onAction: (MapUiAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = { onAction(MapUiAction.OnToggleJoinEvent(event)) },
        modifier = modifier,
        colors = if (event.isUserJoined) {
            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        } else {
            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        }
    ) {
        Text(if (event.isUserJoined) "Desapuntarse" else "Apuntarse")
    }
}

@Composable
fun DeleteButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
    ) {
        Text(text, color = Color.White)
    }
}

@Composable
fun BanUserButton(
    organizerName: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Banear a $organizerName")
    }
}