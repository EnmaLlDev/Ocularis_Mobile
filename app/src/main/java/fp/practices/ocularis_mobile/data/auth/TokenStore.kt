package fp.practices.ocularis_mobile.data.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import fp.practices.ocularis_mobile.data.model.auth.AuthUserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "auth_store"
private const val ENCRYPTED_PREFS_NAME = "secure_auth_prefs"
private const val REFRESH_TOKEN_KEY = "refresh_token"

private val Context.authDataStore by preferencesDataStore(name = DATASTORE_NAME)

/**
 * Almacena de forma segura los tokens de sesión y la información del usuario.
 */
class TokenStore(private val context: Context) {

    private val gson = Gson()
    private val refreshPrefs by lazy {
        fun buildPrefs(): android.content.SharedPreferences {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            return EncryptedSharedPreferences.create(
                context,
                ENCRYPTED_PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
        // Si el archivo de prefs cifrado está corrupto (p. ej. tras reinstalar la app en debug),
        // lo borramos y empezamos de cero — el usuario deberá volver a hacer login.
        try {
            buildPrefs()
        } catch (e: Exception) {
            context.deleteSharedPreferences(ENCRYPTED_PREFS_NAME)
            buildPrefs()
        }
    }

    private object Keys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val USER_INFO = stringPreferencesKey("user_info")
    }

    val accessTokenFlow: Flow<String?> = context.authDataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> prefs[Keys.ACCESS_TOKEN] }

    val userInfoFlow: Flow<AuthUserInfo?> = context.authDataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            prefs[Keys.USER_INFO]?.let { json ->
                runCatching { gson.fromJson(json, AuthUserInfo::class.java) }.getOrNull()
            }
        }

    /**
     * Devuelve el access token actual.
     */
    suspend fun getAccessToken(): String? = accessTokenFlow.first()

    /**
     * Guarda el access token en DataStore.
     */
    suspend fun saveAccessToken(token: String) {
        context.authDataStore.edit { prefs ->
            prefs[Keys.ACCESS_TOKEN] = token
        }
    }

    /**
     * Elimina el access token almacenado.
     */
    suspend fun clearAccessToken() {
        context.authDataStore.edit { prefs ->
            prefs.remove(Keys.ACCESS_TOKEN)
        }
    }

    /**
     * Devuelve el refresh token desde EncryptedSharedPreferences.
     */
    fun getRefreshToken(): String? = refreshPrefs.getString(REFRESH_TOKEN_KEY, null)

    /**
     * Guarda el refresh token de forma cifrada.
     */
    fun saveRefreshToken(token: String) {
        refreshPrefs.edit().putString(REFRESH_TOKEN_KEY, token).apply()
    }

    /**
     * Elimina el refresh token almacenado.
     */
    fun clearRefreshToken() {
        refreshPrefs.edit().remove(REFRESH_TOKEN_KEY).apply()
    }

    /**
     * Serializa y guarda la información del usuario.
     */
    suspend fun saveUserInfo(userInfo: AuthUserInfo) {
        context.authDataStore.edit { prefs ->
            prefs[Keys.USER_INFO] = gson.toJson(userInfo)
        }
    }

    /**
     * Recupera la información del usuario almacenada.
     */
    suspend fun getUserInfo(): AuthUserInfo? = userInfoFlow.first()

    /**
     * Elimina la información del usuario almacenada.
     */
    suspend fun clearUserInfo() {
        context.authDataStore.edit { prefs ->
            prefs.remove(Keys.USER_INFO)
        }
    }

    /**
     * Borra todos los datos de sesión (tokens e información de usuario).
     */
    suspend fun clearAll() {
        clearAccessToken()
        clearUserInfo()
        clearRefreshToken()
    }
}

