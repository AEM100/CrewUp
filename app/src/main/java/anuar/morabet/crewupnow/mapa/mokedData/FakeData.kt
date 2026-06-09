package anuar.morabet.crewupnow.mapa.mokedData

import anuar.morabet.crewupnow.mapa.data.MapEvent
import com.google.android.gms.maps.model.LatLng

fun fakeData () {
    listOf(
        MapEvent(
            id = "1",
            title = "Torneo Basket 3x3",
            description = "Torneo relámpago en las canchas del parque.",
            date = "Sábado 15 de Junio - 17:00h",
            organizer = "Carlos R.",
            coordinates = LatLng(40.4218, -3.6938),
            participantsCount = 12
        ),
        MapEvent(
            id = "2",
            title = "Clase de Yoga Aire Libre",
            description = "Trae tu esterilla y disfruta de una sesión relajante al atardecer.",
            date = "Domingo 16 de Junio - 19:30h",
            organizer = "Marta S.",
            coordinates = LatLng(40.4110, -3.7120),
            participantsCount = 8,
            isUserJoined = true
        )
    )
}