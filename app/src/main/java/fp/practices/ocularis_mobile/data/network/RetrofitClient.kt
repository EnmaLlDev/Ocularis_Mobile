package fp.practices.ocularis_mobile.data.network

import android.content.Context
import fp.practices.ocularis_mobile.data.auth.AuthHeaderInterceptor
import fp.practices.ocularis_mobile.data.auth.TokenAuthenticator
import fp.practices.ocularis_mobile.data.auth.TokenStore
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Usa la IP local de tu PC (obtenida con ipconfig)
    // Ejemplo: private const val BASE_URL = "http://192.168.1.50:8080/"
    private const val BASE_URL = "http://192.168.1.128:8080/"

    @Volatile
    private var initialized = false

    lateinit var tokenStore: TokenStore
        private set

    lateinit var apiService: ApiService
        private set

    private lateinit var authApiService: ApiService

    fun initialize(context: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return

            tokenStore = TokenStore(context.applicationContext)

            val baseRetrofitBuilder = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())

            val plainClient = OkHttpClient.Builder().build()
            authApiService = baseRetrofitBuilder
                .client(plainClient)
                .build()
                .create(ApiService::class.java)

            val authenticatedClient = OkHttpClient.Builder()
                .addInterceptor(AuthHeaderInterceptor(tokenStore))
                .authenticator(TokenAuthenticator(tokenStore, authApiService))
                .build()

            apiService = baseRetrofitBuilder
                .client(authenticatedClient)
                .build()
                .create(ApiService::class.java)

            initialized = true
        }
    }

    fun requireApiService(): ApiService {
        check(initialized) {
            "RetrofitClient no esta inicializado. Llama a RetrofitClient.initialize(context) en MainActivity."
        }
        return apiService
    }

    fun requireAuthApiService(): ApiService {
        check(initialized) {
            "RetrofitClient no esta inicializado. Llama a RetrofitClient.initialize(context) en MainActivity."
        }
        return authApiService
    }
}
