package fp.practices.ocularis_mobile.data.model.auth

import com.google.gson.annotations.SerializedName

/**
 * Credenciales para iniciar sesión.
 */
data class LoginRequest(
    val username: String,
    val password: String
)

/**
 * Solicitud de renovación de tokens con el refresh token.
 */
data class RefreshRequest(
    val refreshToken: String
)

/**
 * Respuesta de autenticación con los tokens de sesión.
 */
data class AuthResponse(
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String? = null,
    @SerializedName("tokenType")
    val tokenType: String? = "Bearer",
    @SerializedName("username")
    val username: String? = null,
    @SerializedName("roles")
    val roles: List<String>? = null
)

/**
 * Información del usuario autenticado devuelta por el servidor.
 */
data class MeResponse(
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName("username")
    val username: String = "",
    @SerializedName("roles")
    val roles: List<String> = emptyList()
)

/**
 * Información local del usuario autenticado.
 */
data class AuthUserInfo(
    val id: Long? = null,
    val username: String = "",
    val roles: List<String> = emptyList()
)

