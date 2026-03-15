package fp.practices.ocularis_mobile.data.model

import java.time.LocalDate

data class PatientDTO(
    val id: Int?,
    val dni: String?,
    val firstName: String,
    val secondName: String?,
    val lastName: String,
    val secondLastName: String?,
    val email: String?,
    val phone: String?,
    val birthDate: LocalDate?,
    val address: String?
)

