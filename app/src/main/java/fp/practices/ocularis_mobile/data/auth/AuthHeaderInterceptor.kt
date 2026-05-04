package fp.practices.ocularis_mobile.data.auth

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthHeaderInterceptor(
    private val tokenStore: TokenStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = runBlocking { tokenStore.getAccessToken() }
        val request = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }

        val response = chain.proceed(request)

        // Si el backend invalida credenciales en endpoints protegidos, se limpia sesión para forzar login.
        val isAuthEndpoint = original.url.encodedPath.startsWith("/auth/")
        if (!isAuthEndpoint && (response.code == 401 || response.code == 403)) {
            runBlocking { tokenStore.clearAll() }
        }

        return response
    }
}

