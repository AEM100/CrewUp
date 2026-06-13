package anuar.morabet.crewupnow.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import anuar.morabet.crewupnow.network.Locator
import anuar.morabet.crewupnow.paneleUsuario.SessionManager
import anuar.morabet.crewupnow.themes.ThemeManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val onLogoutSuccess: () -> Unit
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(isDarkModeEnabled = ThemeManager.isDarkMode.value)
    )
    val uiState = _uiState.asStateFlow()

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.OnToggleDarkMode -> {
                _uiState.update { it.copy(isDarkModeEnabled = action.enabled) }

                ThemeManager.setDarkMode(action.enabled)
            }

            SettingsAction.OnClickLogout -> {
                _uiState.update { it.copy(showLogoutDialog = true) }
            }
            SettingsAction.OnDismissLogout -> {
                _uiState.update { it.copy(showLogoutDialog = false) }
            }
            SettingsAction.OnConfirmLogout -> {
                _uiState.update { it.copy(showLogoutDialog = false) }
                SessionManager.currentUser = null
                onLogoutSuccess()
            }
            SettingsAction.OnClickDeleteAccount -> {
                _uiState.update { it.copy(showDeleteAccountDialog = true) }
            }
            SettingsAction.OnDismissDeleteAccount -> {
                _uiState.update { it.copy(showDeleteAccountDialog = false) }
            }
            SettingsAction.OnConfirmDeleteAccount -> {
                _uiState.update { it.copy(showDeleteAccountDialog = false) }

                val currentUser = SessionManager.currentUser
                if (currentUser != null) {
                    eliminarCuentaDelServidor(userId = currentUser.id) {
                        SessionManager.currentUser = null
                        onLogoutSuccess()
                    }
                }
            }
            SettingsAction.OnClickInfo -> {
                _uiState.update { it.copy(showInfoDialog = true) }
            }
            SettingsAction.OnDismissInfo -> {
                _uiState.update { it.copy(showInfoDialog = false) }
            }

        }
    }

    private fun eliminarCuentaDelServidor(userId: Int, onExito: () -> Unit) {
        viewModelScope.launch {
            try {
                // 1. Creamos el JSON para el servidor Java
                val json = org.json.JSONObject().apply {
                    put("action", "DELETE_ACCOUNT")
                    put("userId", userId)
                }

                // 2. Enviamos por el socket utilizando tu cliente global
                // (Asegúrate de cambiar 'Locator.socketClient' por cómo se llame tu clase del socket)
                val response = Locator.socketClient.sendRequest(json.toString())
                val resJson = org.json.JSONObject(response ?: "")

                // 3. Si MySQL borró el registro correctamente, ejecutamos la salida
                if (resJson.getString("status") == "SUCCESS") {
                    onExito()
                } else {
                    println("El servidor denegó el borrado de la cuenta")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // En caso de que el servidor esté caído, puedes forzar la salida igualmente quitando el comentario de abajo:
                // onExito()
            }
        }
    }
}