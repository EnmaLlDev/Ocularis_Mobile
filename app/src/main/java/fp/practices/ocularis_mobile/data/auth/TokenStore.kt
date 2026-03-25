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

class TokenStore(private val context: Context) {

    private val gson = Gson()
    private val refreshPrefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
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

    suspend fun getAccessToken(): String? = accessTokenFlow.first()

    suspend fun saveAccessToken(token: String) {
        context.authDataStore.edit { prefs ->
            prefs[Keys.ACCESS_TOKEN] = token
        }
    }

    suspend fun clearAccessToken() {
        context.authDataStore.edit { prefs ->
            prefs.remove(Keys.ACCESS_TOKEN)
        }
    }

    fun getRefreshToken(): String? = refreshPrefs.getString(REFRESH_TOKEN_KEY, null)

    fun saveRefreshToken(token: String) {
        refreshPrefs.edit().putString(REFRESH_TOKEN_KEY, token).apply()
    }

    fun clearRefreshToken() {
        refreshPrefs.edit().remove(REFRESH_TOKEN_KEY).apply()
    }

    suspend fun saveUserInfo(userInfo: AuthUserInfo) {
        context.authDataStore.edit { prefs ->
            prefs[Keys.USER_INFO] = gson.toJson(userInfo)
        }
    }

    suspend fun getUserInfo(): AuthUserInfo? = userInfoFlow.first()

    suspend fun clearUserInfo() {
        context.authDataStore.edit { prefs ->
            prefs.remove(Keys.USER_INFO)
        }
    }

    suspend fun clearAll() {
        clearAccessToken()
        clearUserInfo()
        clearRefreshToken()
    }
}

