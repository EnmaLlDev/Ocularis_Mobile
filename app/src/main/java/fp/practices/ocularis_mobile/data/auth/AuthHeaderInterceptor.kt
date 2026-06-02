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
        
        // Si ya trae Authorization (puesto por el Authenticator), no hacemos nada
        if (original.header("Authorization") != null) {
            Logger.d(TAG, "--> ${original.method} ${original.url} (usando header existente)")
            return chain.proceed(original)
        }

        val token = runBlocking { tokenStore.getAccessToken() }
        val request = if (!token.isNullOrBlank()) {
            Logger.d(TAG, "--> ${original.method} ${original.url} (añadiendo token de Store)")
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            Logger.d(TAG, "--> ${original.method} ${original.url} (sin token disponible)")
            original
        }

        val response = chain.proceed(request)
        
        if (response.code == 401 && !original.url.encodedPath.startsWith("/auth/")) {
            Logger.e(TAG, "<-- 401 detectado en ${original.url.encodedPath}")
        }

        return response
    }
}

