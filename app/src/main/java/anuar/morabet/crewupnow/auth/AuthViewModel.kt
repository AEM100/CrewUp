package anuar.morabet.crewupnow.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import anuar.morabet.crewupnow.network.Locator
import anuar.morabet.crewupnow.paneleUsuario.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class AuthViewModel : ViewModel() {

    private val TAG = "DEBUG_CREWUP" // Etiqueta unificada
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Login)
    val uiState = _uiState.asStateFlow()

    private val repository = Locator.authRepository

    fun onAction(action: AuthAction) {
        when (action) {
            AuthAction.ShowLogin -> _uiState.value = AuthUiState.Login
            AuthAction.ShowRegister -> _uiState.value = AuthUiState.Register
            is AuthAction.Register -> register(action.name, action.email, action.password, action.confirmPassword)
            is AuthAction.Login -> login(action.email, action.password)
        }
    }

    private fun login(email: String, password: String) {
        viewModelScope.launch {
            Log.d(TAG, "ViewModel - Iniciando login para: $email")
            _uiState.value = AuthUiState.Loading

            if (email.isBlank() || password.isBlank()) {
                Log.w(TAG, "ViewModel - Campos vacíos")
                _uiState.value = AuthUiState.Error("Por favor, rellena todos los campos")
                return@launch
            }

            when (val result = repository.loginUser(email, password)) {
                is Resource.Success<UserSession> -> {
                    Log.d(TAG, "ViewModel - Login Exitoso. Usuario: ${result.data.name}, ID: ${result.data.id}, Admin: ${result.data.isAdmin}")

                    // Asignación al gestor de sesión
                    SessionManager.currentUser = result.data
                    Log.d(TAG, "ViewModel - SessionManager actualizado. Valor actual: ${SessionManager.currentUser}")

                    _uiState.value = AuthUiState.Authenticated(user = result.data)
                }
                is Resource.Error<UserSession> -> {
                    Log.e(TAG, "ViewModel - Login Error: ${result.message}")
                    _uiState.value = AuthUiState.Error(result.message)
                }
            }
        }
    }

    private fun register(name: String, email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            Log.d(TAG, "ViewModel - Iniciando registro para: $email")
            _uiState.value = AuthUiState.Loading

            if (name.isBlank() || email.isBlank() || password.isBlank()) {
                _uiState.value = AuthUiState.Error("Introduce todos los datos")
                return@launch
            }
            if (password != confirmPassword) {
                _uiState.value = AuthUiState.Error("Las contraseñas no coinciden")
                return@launch
            }

            when (val result = repository.registerUser(name, email, password)) {
                is Resource.Success<UserSession> -> {
                    Log.d(TAG, "ViewModel - Registro Exitoso: ${result.data.name}")
                    SessionManager.currentUser = result.data // También guardamos tras registrar
                    _uiState.value = AuthUiState.Authenticated(user = result.data)
                }
                is Resource.Error<UserSession> -> {
                    Log.e(TAG, "ViewModel - Registro Error: ${result.message}")
                    _uiState.value = AuthUiState.Error(result.message)
                }
            }
        }
    }
}