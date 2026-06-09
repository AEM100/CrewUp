package anuar.morabet.crewupnow.paneleUsuario

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview



data class UserProfile(
    val name: String,
    val age: Int,
    val bio: String,
    val avatarUrl: String,
    val followersCount: Int,
    val followingCount: Int,
    val organizedEventsCount: Int
)

data class Event(
    val id: String,
    val title: String,
    val date: String,
    val location: String,
    val imageUrl: String = ""
)

// ==========================================
// COMPONENTE PRINCIPAL DE LA PANTALLA
// ==========================================

@Composable
fun UserProfileScreen(
    user: UserProfile,
    isOwnProfile: Boolean, // Define si es el perfil del usuario logueado
    isFollowing: Boolean,  // Estado de seguimiento (relevante si isOwnProfile = false)
    createdEvents: List<Event>,
    participatedEvents: List<Event>,
    onEventClick: (Event) -> Unit,
    onEditProfileClick: () -> Unit,
    onFollowClick: () -> Unit,      // Callback para seguir/dejar de seguir
    onMessageClick: () -> Unit,     // Callback para chatear con el usuario
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Creados", "Participando")

    // Usamos LazyColumn como contenedor raíz para evitar problemas de scroll anidado
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Cabecera de Perfil (Avatar Izquierda, Info Derecha)
        item {
            ProfileHeader(user = user)
        }

        // 2. Contador de Estadísticas (Seguidos, Seguidores, Eventos Org.)
        item {
            ProfileStatsRow(user = user)
        }

        // 3. Botones de Acción Dinámicos (Editar o Seguir/Mensaje)
        item {
            ProfileActionsRow(
                isOwnProfile = isOwnProfile,
                isFollowing = isFollowing,
                onEditProfileClick = onEditProfileClick,
                onFollowClick = onFollowClick,
                onMessageClick = onMessageClick
            )
        }

        // 4. Pestañas de Eventos (Creados vs Participando)
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }

        // 5. Listado dinámico de Eventos según la pestaña activa
        val activeEvents = if (selectedTab == 0) createdEvents else participatedEvents

        if (activeEvents.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay eventos registrados en esta categoría.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(activeEvents, key = { it.id }) { event ->
                EventListItem(
                    event = event,
                    onClick = { onEventClick(event) }
                )
            }
        }
    }
}

// ==========================================
// SUB-COMPONENTES AUXILIARES
// ==========================================

@Composable
fun ProfileHeader(
    user: UserProfile,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Avatar circular a la izquierda
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Detalles a la derecha (Nombre, edad, bio)
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = ", ${user.age}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = user.bio,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ProfileStatsRow(
    user: UserProfile,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileStatItem(
                count = user.followingCount.toString(),
                label = "Seguidos",
                modifier = Modifier.weight(1f)
            )
            // Divisor vertical
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
            ProfileStatItem(
                count = user.followersCount.toString(),
                label = "Seguidores",
                modifier = Modifier.weight(1f)
            )
            // Divisor vertical
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
            ProfileStatItem(
                count = user.organizedEventsCount.toString(),
                label = "Eventos Org.",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ProfileStatItem(
    count: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = count,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Gestiona inteligentemente las acciones disponibles dependiendo de si es
 * el perfil propio o el de un tercero.
 */
@Composable
fun ProfileActionsRow(
    isOwnProfile: Boolean,
    isFollowing: Boolean,
    onEditProfileClick: () -> Unit,
    onFollowClick: () -> Unit,
    onMessageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isOwnProfile) {
        // Opción A: Botón de Editar Perfil ocupando todo el ancho
        OutlinedButton(
            onClick = onEditProfileClick,
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Editar Perfil")
        }
    } else {
        // Opción B: Botón de "Seguir" y "Mensaje" lado a lado
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Botón de Seguir (Cambia dinámicamente de estilo según si ya le sigue)
            Button(
                onClick = onFollowClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = if (isFollowing) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
            ) {
                Text(
                    text = if (isFollowing) "Siguiendo" else "Seguir",
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Botón de Mensaje rápido
            OutlinedButton(
                onClick = onMessageClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Mensaje")
            }
        }
    }
}

@Composable
fun EventListItem(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${event.date} • ${event.location}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Detalles",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ==========================================
// VISTAS PREVIAS (PREVIEWS)
// ==========================================

@Preview(showBackground = true, name = "Mi Perfil (Own Profile)")
@Composable
fun OwnUserProfileScreenPreview() {
    val sampleUser = UserProfile(
        name = "Sofía Martínez",
        age = 28,
        bio = "Amante de la tecnología, organizadora de hackatones locales y deportista los fines de semana. ¡Nos vemos en el próximo evento!",
        avatarUrl = "",
        followersCount = 452,
        followingCount = 312,
        organizedEventsCount = 12
    )

    val createdEvents = listOf(
        Event("1", "Hackathon Android 2026", "15 Jun", "Madrid, ES"),
        Event("2", "Taller de Jetpack Compose", "02 Jul", "Online")
    )

    MaterialTheme {
        UserProfileScreen(
            user = sampleUser,
            isOwnProfile = true, // <--- Perfil Propio
            isFollowing = false,
            createdEvents = createdEvents,
            participatedEvents = emptyList(),
            onEventClick = {},
            onEditProfileClick = {},
            onFollowClick = {},
            onMessageClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Perfil Ajeno (Other User Profile)")
@Composable
fun OtherUserProfileScreenPreview() {
    val sampleUser = UserProfile(
        name = "Carlos Ruiz",
        age = 31,
        bio = "Creador de contenido sobre desarrollo de software. Me encanta asistir a conferencias y conocer gente del sector.",
        avatarUrl = "",
        followersCount = 1205,
        followingCount = 490,
        organizedEventsCount = 3
    )

    val createdEvents = listOf(
        Event("1", "Kotlin Meetup #4", "10 Abr", "Valencia, ES")
    )

    MaterialTheme {
        UserProfileScreen(
            user = sampleUser,
            isOwnProfile = false, // <--- Perfil de Otra Persona
            isFollowing = true,   // Lo estamos siguiendo
            createdEvents = createdEvents,
            participatedEvents = emptyList(),
            onEventClick = {},
            onEditProfileClick = {},
            onFollowClick = {}, // Lógica para cambiar estado de seguidor
            onMessageClick = {}  // Lógica para abrir chat
        )
    }
}