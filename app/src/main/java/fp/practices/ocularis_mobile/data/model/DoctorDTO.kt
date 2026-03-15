package fp.practices.ocularis_mobile.data.model

data class DoctorDTO(
    val id: Int?,
    val firstName: String,
    val secondName: String?,
    val lastName: String,
    val secondLastName: String?,
    val dni: String?,
    val email: String?,
    val phone: String?,
    val licenseNumber: String?,
    val specialty: String?
)

