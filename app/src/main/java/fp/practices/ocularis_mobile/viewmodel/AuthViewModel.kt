package fp.practices.ocularis_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fp.practices.ocularis_mobile.data.network.RetrofitClient
import fp.practices.ocularis_mobile.data.model.auth.AuthUserInfo
import fp.practices.ocularis_mobile.data.repository.AuthRepository
import fp.practices.ocularis_mobile.util.Logger
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isCheckingSession: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val accessToken: String? = null,
    val userInfo: AuthUserInfo? = null,
    val roles: Set<String> = emptySet()
) {
    val isAuthenticated: Boolean
        get() = !accessToken.isNullOrBlank()
}

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        observeExternalSessionInvalidation()
        restoreSession()
    }

    private fun observeExternalSessionInvalidation() {
        viewModelScope.launch {
            RetrofitClient.tokenStore.accessTokenFlow.collectLatest { token ->
                if (token.isNullOrBlank()) {
                    _uiState.update {
                        it.copy(
                            isCheckingSession = false,
                            isLoading = false,
                            accessToken = null,
                            userInfo = null,
                            roles = emptySet(),
                            error = null
                        )
                    }
                }
            }
        }
    }

    fun restoreSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingSession = true, error = null) }
            val (token, cachedUser) = repository.restoreSession()

            if (token.isNullOrBlank()) {
                _uiState.update {
                    it.copy(
                        isCheckingSession = false,
                        accessToken = null,
                        userInfo = null,
                        roles = emptySet(),
                        error = null
                    )
                }
                return@launch
            }

            val refreshed = repository.refreshSession()
            val finalUser = if (refreshed) {
                runCatching { repository.fetchMe() }.getOrNull()
            } else {
                cachedUser
            }

            if (refreshed) {
                val (newToken, _) = repository.restoreSession()
                _uiState.update {
                    it.copy(
                        isCheckingSession = false,
                        accessToken = newToken,
                        userInfo = finalUser,
                        roles = normalizeRoles(finalUser?.roles),
                        error = null
                    )
                }
            } else {
                repository.logout()
                _uiState.update {
                    it.copy(
                        isCheckingSession = false,
                        accessToken = null,
                        userInfo = null,
                        roles = emptySet(),
                        error = null
                    )
                }
            }
        }
    }

    fun login(username: String, password: String) {
        Logger.d("AuthViewModel", "Intentando login para usuario: $username")
        if (username.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "Usuario y contraseña son obligatorios") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                val user = repository.login(username.trim(), password)
                val (token, _) = repository.restoreSession()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        accessToken = token,
                        userInfo = user,
                        roles = normalizeRoles(user.roles),
                        error = null
                    )
                }
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = ex.message ?: "No se pudo iniciar sesión"
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.logout()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    accessToken = null,
                    userInfo = null,
                    roles = emptySet(),
                    error = null
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun normalizeRoles(roles: List<String>?): Set<String> {
        return roles.orEmpty()
            .map { raw -> raw.trim().uppercase().removePrefix("ROLE_") }
            .filter { it.isNotBlank() }
            .toSet()
    }
}
