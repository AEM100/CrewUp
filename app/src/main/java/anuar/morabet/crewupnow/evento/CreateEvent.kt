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
import anuar.morabet.crewupnow.mapa.ui.MapUiState

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar

@Composable
fun CreateEventDialog(
    state: MapUiState,
    onAction: (MapUiAction) -> Unit
) {
    val context = LocalContext.current

    // Configuramos el selector de fecha y hora nativo de Android
    val abrirSelectorFechaHora = {
        val calendario = Calendar.getInstance()

        // 1. Abrimos el calendario para elegir el Día
        DatePickerDialog(
            context,
            { _, año, mes, dia ->
                // 2. Al aceptar el día, abrimos el reloj para elegir la Hora
                TimePickerDialog(
                    context,
                    { _, hora, minuto ->
                        // Formateamos los valores para que tengan siempre dos dígitos (Ej: "05" en vez de "5")
                        val mesFormateado = String.format("%02d", mes + 1)
                        val diaFormateado = String.format("%02d", dia)
                        val horaFormateada = String.format("%02d", hora)
                        val minutoFormateado = String.format("%02d", minuto)

                        // Construimos el String ISO estándar que espera Java: "YYYY-MM-DDTHH:mm:ss"
                        val fechaIsoResult = "$año-$mesFormateado-${diaFormateado}T$horaFormateada:$minutoFormateado:00"

                        onAction(MapUiAction.OnNewDateChanged(fechaIsoResult))
                    },
                    calendario.get(Calendar.HOUR_OF_DAY),
                    calendario.get(Calendar.MINUTE),
                    true // Formato de 24 horas
                ).show()
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

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

                // 🗓️ 🔥 NUEVA SECCIÓN DE SELECCIÓN DE FECHA OBLIGATORIA
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Fecha y hora del evento *",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = state.newEventDate.replace("T", "  Hora: "), // Hace que se vea más amigable en pantalla
                            onValueChange = {},
                            readOnly = true, // Evita que el usuario escriba a mano
                            placeholder = { Text("Ninguna fecha seleccionada") },
                            modifier = Modifier.weight(1f),
                            textStyle = MaterialTheme.typography.bodyMedium
                        )

                        IconButton(
                            onClick = { abrirSelectorFechaHora() },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange, // Icono de calendario nativo
                                contentDescription = "Elegir fecha",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

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
                        // 🔥 OBLIGATORIO: Se activa solo si tiene título Y si ha elegido una fecha
                        enabled = state.newEventTitle.isNotBlank() && state.newEventDate.isNotBlank(),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text("Crear Evento")
                    }
                }
            }
        }
    }
}