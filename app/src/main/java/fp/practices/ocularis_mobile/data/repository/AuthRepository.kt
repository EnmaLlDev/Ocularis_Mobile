package fp.practices.ocularis_mobile.data.repository

import fp.practices.ocularis_mobile.data.auth.TokenStore
import fp.practices.ocularis_mobile.data.model.auth.AuthUserInfo
import fp.practices.ocularis_mobile.data.model.auth.LoginRequest
import fp.practices.ocularis_mobile.data.model.auth.MeResponse
import fp.practices.ocularis_mobile.data.model.auth.RefreshRequest
import fp.practices.ocularis_mobile.data.network.ApiService
import fp.practices.ocularis_mobile.data.network.RetrofitClient

/**
 * Repositorio que gestiona la autenticación, sesión y tokens del usuario.
 */
class AuthRepository(
    api: ApiService? = null,
    authApi: ApiService? = null,
    tokenStore: TokenStore? = null
) {
    private val api: ApiService = api ?: RetrofitClient.requireApiService()
    private val authApi: ApiService = authApi ?: RetrofitClient.requireAuthApiService()
    private val tokenStore: TokenStore = tokenStore ?: RetrofitClient.tokenStore

    /**
     * Inicia sesión y almacena los tokens y la información del usuario.
     * Usa los campos username/roles del login response directamente para evitar
     * una condición de carrera en DataStore al llamar api.me() inmediatamente después.
     */
    suspend fun login(username: String, password: String): AuthUserInfo {
        val auth = authApi.login(LoginRequest(username = username, password = password))
        tokenStore.saveAccessToken(auth.accessToken)
        auth.refreshToken?.let { tokenStore.saveRefreshToken(it) }

        val userInfo = AuthUserInfo(
            id = null,
            username = auth.username ?: username,
            roles = auth.roles.orEmpty().map { it.removePrefix("ROLE_") }
        )
        tokenStore.saveUserInfo(userInfo)
        return userInfo
    }

    /**
     * Renueva la sesión usando el refresh token almacenado.
     * @return true si la renovación fue exitosa
     */
    suspend fun refreshSession(): Boolean {
        val refreshToken = tokenStore.getRefreshToken() ?: return false
        val auth = runCatching { authApi.refresh(RefreshRequest(refreshToken)) }.getOrNull() ?: return false
        tokenStore.saveAccessToken(auth.accessToken)
        auth.refreshToken?.let { tokenStore.saveRefreshToken(it) }

        return runCatching {
            val me = api.me()
            tokenStore.saveUserInfo(me.toUserInfo())
            true
        }.getOrElse { false }
    }

    /**
     * Recupera el token y la información de usuario guardados localmente.
     */
    suspend fun restoreSession(): Pair<String?, AuthUserInfo?> {
        val token = tokenStore.getAccessToken()
        val userInfo = tokenStore.getUserInfo()
        return token to userInfo
    }

    /**
     * Obtiene la información actual del usuario desde el servidor y la guarda.
     */
    suspend fun fetchMe(): AuthUserInfo {
        val me = api.me()
        val userInfo = me.toUserInfo()
        tokenStore.saveUserInfo(userInfo)
        return userInfo
    }

    /**
     * Cierra sesión en el servidor y borra todos los datos locales.
     */
    suspend fun logout() {
        val refreshToken = tokenStore.getRefreshToken()
        runCatching {
            authApi.logout(refreshToken?.let { RefreshRequest(it) })
        }
        tokenStore.clearAll()
    }

    private fun MeResponse.toUserInfo(): AuthUserInfo {
        return AuthUserInfo(
            id = id,
            username = username,
            roles = roles
        )
    }
}

