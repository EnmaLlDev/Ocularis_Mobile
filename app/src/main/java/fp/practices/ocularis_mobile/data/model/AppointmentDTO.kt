package fp.practices.ocularis_mobile.data.model

/**
 * Representa una cita médica.
 */
data class AppointmentDTO(
    val id: Int?,
    val dateTime: String?,
    val patient: PatientDTO?,
    val doctor: DoctorDTO?,
    val reason: String?,
    val status: StateAppoinment?
)