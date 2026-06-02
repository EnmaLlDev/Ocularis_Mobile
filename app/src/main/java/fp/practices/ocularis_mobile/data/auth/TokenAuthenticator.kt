package fp.practices.ocularis_mobile.data.auth

import fp.practices.ocularis_mobile.data.model.auth.RefreshRequest
import fp.practices.ocularis_mobile.data.network.ApiService
import fp.practices.ocularis_mobile.util.Logger
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * Authenticator que renueva el access token automáticamente ante un 401.
 */
class TokenAuthenticator(
    private val tokenStore: TokenStore,
    private val authApi: ApiService
) : Authenticator {

    private val TAG = "TokenAuthenticator"

    /**
     * Intenta refrescar el token y reintenta la petición original.
     */
    override fun authenticate(route: Route?, response: Response): Request? {
        val path = response.request.url.encodedPath
        Logger.d(TAG, "authenticate() called for path=$path, responseCode=${response.code}")

        if (path.startsWith("/auth/")) {
            Logger.d(TAG, "Skipping auth endpoint, returning null")
            return null
        }

        // Solo manejar 401 (no autenticado), NO 403 (no autorizado - es permiso, no token)
        if (response.code != 401) {
            Logger.d(TAG, "Response code is ${response.code}, not 401, returning null")
            return null
        }

        val count = responseCount(response)
        if (count >= 2) {
            Logger.e(TAG, "Response count >= 2 ($count), preventing infinite loop, returning null")
            return null
        }

        val currentRefresh = tokenStore.getRefreshToken()
        if (currentRefresh.isNullOrBlank()) {
            Logger.e(TAG, "No refresh token available in TokenStore, clearing session")
            runBlocking { tokenStore.clearAll() }
            return null
        }

        synchronized(this) {
            val requestAccessToken = response.request.header("Authorization")
                ?.removePrefix("Bearer ")
                ?.trim()
            val latestAccessToken = runBlocking { tokenStore.getAccessToken() }
            
            Logger.d(TAG, "Request token: ${requestAccessToken?.take(10)}...")
            Logger.d(TAG, "Latest stored token: ${latestAccessToken?.take(10)}...")

            if (!latestAccessToken.isNullOrBlank() && latestAccessToken != requestAccessToken) {
                Logger.d(TAG, "✅ Token already renewed by another thread, retrying with latest")
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $latestAccessToken")
                    .build()
            }

            Logger.d(TAG, "🔄 Attempting to refresh token...")
            val refreshed = runCatching {
                runBlocking { authApi.refresh(RefreshRequest(currentRefresh)) }
            }.onSuccess {
                Logger.d(TAG, "✅ Refresh success.")
                // Decodificar el payload del JWT (es la parte del medio separada por puntos)
                val payloadB64 = it.accessToken.split(".")[1]
                val decodedPayload = String(android.util.Base64.decode(payloadB64, android.util.Base64.DEFAULT))
                Logger.d(TAG, "🔍 JWT Payload del nuevo token: $decodedPayload")
                Logger.d(TAG, "✅ Refresh success. New token: ${it.accessToken.take(10)}...")
            }.onFailure { ex ->
                Logger.e(TAG, "❌ Refresh failed: ${ex.message}")
            }.getOrNull()

            if (refreshed == null) {
                Logger.e(TAG, "❌ Refresh returned null. Clearing session.")
                runBlocking { tokenStore.clearAll() }
                return null
            }

            if (refreshed.accessToken == requestAccessToken) {
                Logger.e(TAG, "❌ Server returned the SAME expired token. Avoiding infinite loop.")
                runBlocking { tokenStore.clearAll() }
                return null
            }

            Logger.d(TAG, "💾 Saving new tokens...")
            runBlocking {
                tokenStore.saveAccessToken(refreshed.accessToken)
                refreshed.refreshToken?.let { tokenStore.saveRefreshToken(it) }
            }

            Logger.d(TAG, "🚀 Retrying original request with NEW token")
            
            // Construimos el reintento asegurándonos de mantener el body original
            val newRequest = response.request.newBuilder()
                .header("Authorization", "Bearer ${refreshed.accessToken}")
                .build()
                
            return newRequest
        }
    }

    /**
     * Cuenta cuántas respuestas previas existen para evitar bucles infinitos.
     */
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

