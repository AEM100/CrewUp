package anuar.morabet.crewupnow.editProfile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import anuar.morabet.crewupnow.network.Locator
import anuar.morabet.crewupnow.paneleUsuario.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import org.json.JSONObject // Usamos la librería nativa de Android para crear JSON de forma segura

class EditProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        cargarDatosUsuarioActual()
    }

    private fun cargarDatosUsuarioActual() {
        val usuarioActual = SessionManager.currentUser
        if (usuarioActual != null) {
            _uiState.update { state ->
                state.copy(
                    name = usuarioActual.name,
                    bio = usuarioActual.bio ?: "" // 🔥 Cargamos la bio real que viene del servidor
                )
            }
        }
    }

    fun onAction(action: EditProfileDataAction) {
        when (action) {
            is EditProfileDataAction.OnNameChanged -> {
                _uiState.update { it.copy(name = action.newName) }
            }
            is EditProfileDataAction.OnBioChanged -> {
                _uiState.update { it.copy(bio = action.newBio) }
            }
            EditProfileDataAction.OnSaveClicked -> {
                guardarCambiosEnServidor()
            }
            EditProfileDataAction.OnChangeAvatarClicked -> {
                // Se implementará más adelante junto con el guardado de la foto
            }
        }
    }

    private fun guardarCambiosEnServidor() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Usamos SessionManager.currentUser que ahora es del tipo UserSession actualizado
                val usuarioActual = SessionManager.currentUser
                    ?: throw Exception("Error: No se encontró la sesión del usuario.")

                // 1. Construimos el JSON exacto usando JSONObject (como haces en tu AuthRepository)
                val requestJson = JSONObject().apply {
                    put("action", "UPDATE_PROFILE")
                    put("userId", usuarioActual.id)
                    put("name", _uiState.value.name)
                    put("bio", _uiState.value.bio)
                }

                // 2. Enviamos al servidor usando tu método real: sendRequest
                val responseString = Locator.socketClient.sendRequest(requestJson.toString())
                    ?: throw Exception("No se pudo conectar con el servidor.")

                // 3. Parseamos la respuesta. ¡Ahora sí te compilará optString!
                val responseJson = JSONObject(responseString)
                val status = responseJson.optString("status", "ERROR")

                if (status == "SUCCESS") {
                    // 4. Actualizamos la sesión local copiando el nuevo nombre y la nueva bio
                    SessionManager.currentUser = usuarioActual.copy(
                        name = _uiState.value.name,
                        bio = _uiState.value.bio // 👈 ¡Ya existe la propiedad!
                    )

                    _uiState.update { it.copy(isLoading = false, isSaveSuccess = true) }
                } else {
                    val errorMsg = responseJson.optString("message", "Error al actualizar en el servidor.")
                    _uiState.update { it.copy(isLoading = false, error = errorMsg) }
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error de conexión") }
            }
        }
    }
}