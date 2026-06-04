package com.joel.proyecto2026.ui.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joel.proyecto2026.models.LoginCredentials
import com.joel.proyecto2026.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val rememberMe: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false
)

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        LoginUiState(email = authRepository.getRememberedEmail().orEmpty())
    )
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email, error = null)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    fun onRememberChange(remember: Boolean) {
        _uiState.value = _uiState.value.copy(rememberMe = remember)
    }

    fun login() {
        val state = _uiState.value
        if (!isValidEmail(state.email)) {
            _uiState.value = state.copy(error = "Correo inválido")
            return
        }
        if (state.password.length < 6) {
            _uiState.value = state.copy(error = "Contraseña muy corta")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            val loggedIn = authRepository.login(
                LoginCredentials(
                    email = state.email,
                    password = state.password,
                    rememberMe = state.rememberMe
                )
            )

            _uiState.value = if (loggedIn) {
                state.copy(
                    password = "",
                    isLoading = false,
                    error = null,
                    isLoggedIn = true
                )
            } else {
                state.copy(
                    isLoading = false,
                    error = "Correo o contraseña incorrectos"
                )
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.value = LoginUiState(
            email = authRepository.getRememberedEmail().orEmpty()
        )
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
