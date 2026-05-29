package fp.practices.ocularis_mobile.data.auth

import fp.practices.ocularis_mobile.util.Logger
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor que añade el token de acceso al header Authorization de cada petición.
 */
class AuthHeaderInterceptor(
    private val tokenStore: TokenStore
) : Interceptor {

    private val TAG = "HttpClient"

    /**
     * Añade el header Bearer si existe un token válido.
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = runBlocking { tokenStore.getAccessToken() }
        val hasToken = !token.isNullOrBlank()
        Logger.d(TAG, "--> ${original.method} ${original.url} (token=${if (hasToken) "present" else "absent"})")
        val request = if (hasToken) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }

        val response = chain.proceed(request)
        Logger.d(TAG, "<-- ${response.code} ${original.method} ${original.url}")

        // No borrar tokens aquí: un 401 puede significar token expirado.
        // El Authenticator debe manejar la renovación vía refresh token.
        if (!original.url.encodedPath.startsWith("/auth/") && response.code == 401) {
            Logger.e(TAG, "401 detectado en ${original.url.encodedPath} -> permitiendo que TokenAuthenticator maneje la sesión")
        }

        return response
    }
}

