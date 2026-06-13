package anuar.morabet.crewupnow.EventPanel

data class UpcomingEventItem(
    val id: String,
    val title: String,
    val description: String,
    val rawDate: String,         // Guardamos "2026-06-15T18:30:00"
    val formattedDate: String,   // Guardamos "15 JUN" o "Sábado, 15 de Junio"
    val formattedTime: String,   // Guardamos "18:30h"
    val organizer: String,
    val lat: Double,
    val lng: Double,
    val address: String = "Cargando ubicación..." // Aquí se guardará la calle real
)