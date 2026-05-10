package fp.practices.ocularis_mobile.data.network

import android.content.Context
import fp.practices.ocularis_mobile.data.auth.AuthHeaderInterceptor
import fp.practices.ocularis_mobile.data.auth.TokenAuthenticator
import fp.practices.ocularis_mobile.data.auth.TokenStore
import fp.practices.ocularis_mobile.util.Logger
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val TAG = "RetrofitClient"
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
        Logger.d(TAG, "Inicializando RetrofitClient...")
        synchronized(this) {
            if (initialized) return
            try {
                tokenStore = TokenStore(context.applicationContext)

                val baseRetrofitBuilder = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())

                val plainClient = OkHttpClient.Builder().build()
                authApiService = baseRetrofitBuilder
                    .client(plainClient)
                    .build()
                    .create(ApiService::class.java)
                Logger.d(TAG, "authApiService creado correctamente")

                val authenticatedClient = OkHttpClient.Builder()
                    .addInterceptor(AuthHeaderInterceptor(tokenStore))
                    .authenticator(TokenAuthenticator(tokenStore, authApiService))
                    .build()

                apiService = baseRetrofitBuilder
                    .client(authenticatedClient)
                    .build()
                    .create(ApiService::class.java)
                Logger.d(TAG, "apiService creado correctamente")

                initialized = true
                Logger.d(TAG, "RetrofitClient inicializado correctamente")
            } catch (e: Exception) {
                Logger.e(TAG, "Error al inicializar RetrofitClient", e)
                throw e
            }
        }
    }

    fun requireApiService(): ApiService {
        if (!initialized) {
            Logger.e(TAG, "RetrofitClient no esta inicializado al requerir apiService. Llama a RetrofitClient.initialize(context) en MainActivity.")
            throw IllegalStateException("RetrofitClient no esta inicializado. Llama a RetrofitClient.initialize(context) en MainActivity.")
        }
        Logger.d(TAG, "apiService retornado correctamente")
        return apiService
    }

    fun requireAuthApiService(): ApiService {
        if (!initialized) {
            Logger.e(TAG, "RetrofitClient no esta inicializado al requerir authApiService. Llama a RetrofitClient.initialize(context) en MainActivity.")
            throw IllegalStateException("RetrofitClient no esta inicializado. Llama a RetrofitClient.initialize(context) en MainActivity.")
        }
        Logger.d(TAG, "authApiService retornado correctamente")
        return authApiService
    }
}
