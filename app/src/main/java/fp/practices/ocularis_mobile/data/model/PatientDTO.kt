package fp.practices.ocularis_mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Representa un paciente de la clínica.
 */
data class PatientDTO(
    @SerializedName("id") val id: Int?,
    @SerializedName("dni") val dni: String?,
    @SerializedName("firstName") val firstName: String,
    @SerializedName("secondName") val secondName: String?,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("secondLastName") val secondLastName: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("birthDate") val birthDate: String?,
    @SerializedName("address") val address: String?,
    @SerializedName("doctorIds") val doctorIds: List<Int>? = null
)

