package fp.practices.ocularis_mobile.data.auth

import fp.practices.ocularis_mobile.data.model.auth.RefreshRequest
import fp.practices.ocularis_mobile.data.network.ApiService
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val tokenStore: TokenStore,
    private val authApi: ApiService
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null

        val currentRefresh = tokenStore.getRefreshToken() ?: return null

        synchronized(this) {
            val requestAccessToken = response.request.header("Authorization")
                ?.removePrefix("Bearer ")
                ?.trim()
            val latestAccessToken = runBlocking { tokenStore.getAccessToken() }

            if (!latestAccessToken.isNullOrBlank() && latestAccessToken != requestAccessToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $latestAccessToken")
                    .build()
            }

            val refreshed = runCatching {
                runBlocking { authApi.refresh(RefreshRequest(currentRefresh)) }
            }.getOrNull() ?: return null

            runBlocking {
                tokenStore.saveAccessToken(refreshed.accessToken)
            }
            refreshed.refreshToken?.let { tokenStore.saveRefreshToken(it) }

            return response.request.newBuilder()
                .header("Authorization", "Bearer ${refreshed.accessToken}")
                .build()
        }
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var current = response.priorResponse
        while (current != null) {
            result++
            current = current.priorResponse
        }
        return result
    }
}

