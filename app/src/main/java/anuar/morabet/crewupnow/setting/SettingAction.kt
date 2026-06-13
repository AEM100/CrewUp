package anuar.morabet.crewupnow.setting


sealed interface SettingsAction {
    data class OnToggleDarkMode(val enabled: Boolean) : SettingsAction
    object OnClickLogout : SettingsAction
    object OnConfirmLogout : SettingsAction
    object OnDismissLogout : SettingsAction
    object OnClickDeleteAccount : SettingsAction
    object OnConfirmDeleteAccount : SettingsAction
    object OnDismissDeleteAccount : SettingsAction

    object OnClickInfo : SettingsAction
    object OnDismissInfo : SettingsAction
}