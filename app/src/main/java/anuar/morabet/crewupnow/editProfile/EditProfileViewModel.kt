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
import org.json.JSONObject


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
                    email = usuarioActual.email,
                    bio = usuarioActual.bio,
                    fotoBase64 = usuarioActual.fotoBase64
                )
            }
        }
    }

    fun onAction(action: EditProfileDataAction) {
        when (action) {
            is EditProfileDataAction.OnNameChanged -> {
                _uiState.update { it.copy(name = action.newName) }
            }
            is EditProfileDataAction.OnEmailChanged -> {
                _uiState.update { it.copy(email = action.newEmail) }
            }
            is EditProfileDataAction.OnBioChanged -> {
                _uiState.update { it.copy(bio = action.newBio) }
            }
            is EditProfileDataAction.OnPasswordChanged -> {
                _uiState.update { it.copy(password = action.newPassword) }
            }
            is EditProfileDataAction.OnAvatarSelected -> {
                _uiState.update { it.copy(fotoBase64 = action.base64) }
            }
            EditProfileDataAction.OnSaveClicked -> {
                guardarCambiosEnServidor()
            }
            EditProfileDataAction.OnChangeAvatarClicked -> {
                // Esta acción se maneja desde la UI con el launcher de fotos
            }
        }
    }

    private fun guardarCambiosEnServidor() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val usuarioActual = SessionManager.currentUser
                    ?: throw Exception("Error: No se encontró la sesión del usuario.")

                // 1. Construimos el JSON con todos los campos
                val requestJson = JSONObject().apply {
                    put("action", "UPDATE_PROFILE")
                    put("userId", usuarioActual.id)
                    put("name", _uiState.value.name)
                    put("email", _uiState.value.email)
                    put("bio", _uiState.value.bio)
                    // Solo enviamos password si el usuario escribió algo
                    if (_uiState.value.password.isNotBlank()) {
                        put("password", _uiState.value.password)
                    }
                    put("foto_base64", _uiState.value.fotoBase64)
                }

                // 2. Enviamos al servidor
                val responseString = Locator.socketClient.sendRequest(requestJson.toString())
                    ?: throw Exception("No se pudo conectar con el servidor.")

                // 3. Parseamos la respuesta
                val responseJson = JSONObject(responseString)
                val status = responseJson.optString("status", "ERROR")

                if (status == "SUCCESS") {
                    // 4. Actualizamos la sesión local con TODOS los campos
                    SessionManager.currentUser = usuarioActual.copy(
                        name = _uiState.value.name,
                        email = _uiState.value.email,
                        bio = _uiState.value.bio,
                        fotoBase64 = _uiState.value.fotoBase64
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