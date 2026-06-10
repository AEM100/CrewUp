package anuar.morabet.crewupnow.evento

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import anuar.morabet.crewupnow.mapa.MapUiAction
import anuar.morabet.crewupnow.mapa.data.MapUiState

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