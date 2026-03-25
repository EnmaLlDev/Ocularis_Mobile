package fp.practices.ocularis_mobile.data.model.auth

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val username: String,
    val password: String
)

data class RefreshRequest(
    val refreshToken: String
)

data class AuthResponse(
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String? = null,
    @SerializedName("tokenType")
    val tokenType: String? = "Bearer"
)

data class MeResponse(
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName("username")
    val username: String = "",
    @SerializedName("roles")
    val roles: List<String> = emptyList()
)

data class AuthUserInfo(
    val id: Long? = null,
    val username: String = "",
    val roles: List<String> = emptyList()
)

