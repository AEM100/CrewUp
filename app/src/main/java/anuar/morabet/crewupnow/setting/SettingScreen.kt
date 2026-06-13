package anuar.morabet.crewupnow.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // --- SECCIÓN: PREFERENCIAS ---
            Text("Preferencias", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

            Card(shape = RoundedCornerShape(12.dp)) {
                Column {
                    SettingSwitchItem(
                        icon = Icons.Default.Menu, // Representa tema / UI
                        title = "Modo Oscuro",
                        checked = state.isDarkModeEnabled,
                        onCheckedChange = { onAction(SettingsAction.OnToggleDarkMode(it)) }
                    )

                }
            }

            // --- SECCIÓN: SOPORTE E INFO ---
            Text("Aplicación", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

            Card(shape = RoundedCornerShape(12.dp)) {
                Column {
                    SettingClickableItem(
                        icon = Icons.Default.Info,
                        title = "Información de la App",
                        subtitle = state.appVersion,
                        onClick =  { onAction(SettingsAction.OnClickInfo) }
                    )

                }
            }

            // --- SECCIÓN: CUENTA (ACCIONES DESTRUCTORAS) ---
            Text("Cuenta", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error)

            Card(shape = RoundedCornerShape(12.dp)) {
                Column {
                    SettingClickableItem(
                        icon = Icons.Default.ExitToApp,
                        title = "Cerrar Sesión",
                        titleColor = MaterialTheme.colorScheme.error,
                        onClick = { onAction(SettingsAction.OnClickLogout) }
                    )
                    HorizontalDivider()
                    SettingClickableItem(
                        icon = Icons.Default.Delete,
                        title = "Eliminar Cuenta permanentemente",
                        titleColor = MaterialTheme.colorScheme.error,
                        onClick = { onAction(SettingsAction.OnClickDeleteAccount) }
                    )
                }
            }
            if (state.showInfoDialog) {
                AlertDialog(
                    onDismissRequest = { onAction(SettingsAction.OnDismissInfo) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    },
                    title = {
                        Text(
                            text = "CrewUp",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = state.appVersion,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            Text(
                                text = "Plataforma colaborativa para la creación y gestión de eventos deportivos y sociales en tiempo real.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Desarrollado con Android Jetpack Compose y Backend en Java Sockets con Spring Data JPA.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { onAction(SettingsAction.OnDismissInfo) }) {
                            Text("Entendido")
                        }
                    }
                )
            }
        }
    }

    // --- DIÁLOGOS DE CONFIRMACIÓN ---
    if (state.showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { onAction(SettingsAction.OnDismissLogout) },
            title = { Text("¿Cerrar Sesión?") },
            text = { Text("Tendrás que volver a introducir tus credenciales para acceder.") },
            confirmButton = {
                TextButton(onClick = { onAction(SettingsAction.OnConfirmLogout) }) { Text("Salir") }
            },
            dismissButton = {
                TextButton(onClick = { onAction(SettingsAction.OnDismissLogout) }) { Text("Cancelar") }
            }
        )
    }

    if (state.showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { onAction(SettingsAction.OnDismissDeleteAccount) },
            title = { Text("¿Eliminar Cuenta?") },
            text = { Text("Esta acción es irreversible. Se borrarán todos tus eventos y datos de la base de datos.") },
            confirmButton = {
                Button(
                    onClick = { onAction(SettingsAction.OnConfirmDeleteAccount) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { onAction(SettingsAction.OnDismissDeleteAccount) }) { Text("Cancelar") }
            }
        )
    }
}

// --- SUBCOMPONENTES REUTILIZABLES ---

@Composable
fun SettingSwitchItem(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingClickableItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = if(titleColor == MaterialTheme.colorScheme.error) titleColor else MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = titleColor)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
    }
}