package anuar.morabet.crewupnow.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay

class LoginViewModel : ViewModel() {

    private val _uiState =
        MutableStateFlow<LoginUiState>(
            LoginUiState.Idle
        )

    val uiState = _uiState.asStateFlow()

    fun onAction(action: LoginAction) {
        when (action) {

            is LoginAction.Login -> {
                login(
                    action.email,
                    action.password
                )
            }

            is LoginAction.Register -> {
                register(
                    action.email,
                    action.password
                )
            }
        }
    }

    private fun login(
        email: String,
        password: String
    ) {
        viewModelScope.launch {

            _uiState.value = LoginUiState.Loading

            delay(1500)

            if (
                email.isNotBlank() &&
                password.isNotBlank()
            ) {
                _uiState.value =
                    LoginUiState.Success("123")
            } else {
                _uiState.value =
                    LoginUiState.Error(
                        "Credenciales inválidas"
                    )
            }
        }
    }

    private fun register(
        email: String,
        password: String
    ) {
        viewModelScope.launch {

            _uiState.value = LoginUiState.Loading

            delay(1500)

            if (
                email.isNotBlank() &&
                password.isNotBlank()
            ) {
                _uiState.value =
                    LoginUiState.Success("456")
            } else {
                _uiState.value =
                    LoginUiState.Error(
                        "Datos inválidos"
                    )
            }
        }
    }
}