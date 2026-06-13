package anuar.morabet.crewupnow.setting

data class SettingsUiState(
    val isDarkModeEnabled: Boolean = false,
    val showLogoutDialog: Boolean = false,
    val showDeleteAccountDialog: Boolean = false,
    val showInfoDialog: Boolean = false,
    val appVersion: String = "v1.4.2 (2026)"
)
