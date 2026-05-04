package fp.practices.ocularis_mobile.data.repository

import fp.practices.ocularis_mobile.data.auth.TokenStore
import fp.practices.ocularis_mobile.data.model.auth.AuthUserInfo
import fp.practices.ocularis_mobile.data.model.auth.LoginRequest
import fp.practices.ocularis_mobile.data.model.auth.MeResponse
import fp.practices.ocularis_mobile.data.model.auth.RefreshRequest
import fp.practices.ocularis_mobile.data.network.ApiService
import fp.practices.ocularis_mobile.data.network.RetrofitClient

class AuthRepository(
    api: ApiService? = null,
    authApi: ApiService? = null,
    tokenStore: TokenStore? = null
) {
    private val api: ApiService = api ?: RetrofitClient.requireApiService()
    private val authApi: ApiService = authApi ?: RetrofitClient.requireAuthApiService()
    private val tokenStore: TokenStore = tokenStore ?: RetrofitClient.tokenStore

    suspend fun login(username: String, password: String): AuthUserInfo {
        val auth = authApi.login(LoginRequest(username = username, password = password))
        tokenStore.saveAccessToken(auth.accessToken)
        auth.refreshToken?.let { tokenStore.saveRefreshToken(it) }

        val me = api.me()
        val userInfo = me.toUserInfo()
        tokenStore.saveUserInfo(userInfo)
        return userInfo
    }

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

    suspend fun restoreSession(): Pair<String?, AuthUserInfo?> {
        val token = tokenStore.getAccessToken()
        val userInfo = tokenStore.getUserInfo()
        return token to userInfo
    }

    suspend fun fetchMe(): AuthUserInfo {
        val me = api.me()
        val userInfo = me.toUserInfo()
        tokenStore.saveUserInfo(userInfo)
        return userInfo
    }

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

