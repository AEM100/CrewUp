package anuar.morabet.crewupnow.EventPanel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingEventsScreen(
    state: UpcomingEventsUiState,
    onRetryClick: () -> Unit,
    onCardAppear: (UpcomingEventItem) -> Unit
) {
    var selectedEventForAddress by remember { mutableStateOf<UpcomingEventItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Inscripciones", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // El "Despachador" reactivo basado estrictamente en las propiedades del estado actual
            when {
                state.isLoading -> {
                    LoadingView(modifier = Modifier.align(Alignment.Center))
                }
                state.errorMessage != null -> {
                    ErrorView(
                        message = state.errorMessage,
                        onRetry = onRetryClick
                    )
                }
                else -> {
                    SuccessView(
                        events = state.eventsList,
                        onEventAppear = onCardAppear,
                        onEventClick = { selectedEventForAddress = it }
                    )
                }
            }
        }
    }

    // Ventana emergente controlada de forma reactiva por el estado local de selección
    selectedEventForAddress?.let { item ->
        // Sincronizamos con la dirección actualizada que mute en el estado global
        val freshItem = state.eventsList.find { it.id == item.id } ?: item
        AddressDialog(
            item = freshItem,
            onDismiss = { selectedEventForAddress = null }
        )
    }
}

@Composable
private fun LoadingView(modifier: Modifier = Modifier) {
    CircularProgressIndicator(modifier = modifier)
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Reintentar") }
    }
}

@Composable
private fun SuccessView(
    events: List<UpcomingEventItem>,
    onEventAppear: (UpcomingEventItem) -> Unit,
    onEventClick: (UpcomingEventItem) -> Unit
) {
    if (events.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No estás apuntado a ningún próximo evento.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(32.dp),
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(events, key = { it.id }) { evento ->
                // Notificamos reactivamente al contenedor superior que este item requiere Geocodificación
                LaunchedEffect(evento.id) {
                    onEventAppear(evento)
                }

                UpcomingEventCard(
                    item = evento,
                    onClick = { onEventClick(evento) }
                )
            }
        }
    }
}

@Composable
private fun UpcomingEventCard(
    item: UpcomingEventItem,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 🗓️ FECHA ENORME DESTACADA (Lateral Izquierdo)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(65.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(vertical = 8.dp, horizontal = 4.dp)
            ) {
                Text(
                    text = item.formattedDate.substringBefore("\n"),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimary,
                    lineHeight = 26.sp
                )
                Text(
                    text = item.formattedDate.substringAfter("\n"),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // INFO DEL EVENTO
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (item.description.isNotBlank()) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                        Text(item.formattedTime, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                        Text(item.organizer, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun AddressDialog(
    item: UpcomingEventItem,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Título principal con el nombre del evento
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // 1. Sección de la Fecha y Hora
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text("Fecha y Hora:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        // Limpiamos el salto de línea interno para mostrarlo horizontal, ej: "15 JUN a las 18:30h"
                        val diaMes = item.formattedDate.replace("\n", " ")
                        Text("$diaMes a las ${item.formattedTime}", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                // 2. Sección de la Ubicación Postal Calculada
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text("Dirección Postal:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Text(item.address, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                // 3. Sección de Asistencia (Participantes)
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text("Asistentes confirmados:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        // Nota: Si no tienes el conteo numérico real mapeado en UpcomingEventItem,
                        // puedes añadir 'val participantsCount: Int' a tu modelo. De momento lo asocia con texto explicativo.
                        Text("Estás inscrito en este evento junto a otros usuarios", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar detalles")
                }
            }
        }
    }
}