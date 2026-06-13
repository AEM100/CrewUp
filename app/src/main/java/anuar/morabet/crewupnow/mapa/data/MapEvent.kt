package anuar.morabet.crewupnow.mapa.data

import com.google.android.gms.maps.model.LatLng

data class MapEvent(
    val id: String,
    val title: String,
    val description: String,
    val date: String,
    val organizer: String,
    val coordinates: LatLng,
    val creatorId: Int,
    val participantsCount: Int,
    val isUserJoined: Boolean = false
)