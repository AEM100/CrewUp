package anuar.morabet.crewupnow.EventPanel

data class UpcomingEventsUiState(
    val isLoading: Boolean = false,
    val eventsList: List<UpcomingEventItem> = emptyList(),
    val errorMessage: String? = null
)
