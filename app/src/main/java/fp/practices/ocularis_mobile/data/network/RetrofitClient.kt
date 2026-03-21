package fp.practices.ocularis_mobile.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Emulador Android accede al host local de la máquina con 10.0.2.2
    private const val BASE_URL = "http://10.0.2.2:8080/" // barra final requerida por Retrofit

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
