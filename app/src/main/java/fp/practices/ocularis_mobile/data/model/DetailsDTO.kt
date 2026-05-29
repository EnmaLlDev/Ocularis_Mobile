package fp.practices.ocularis_mobile.data.model

/**
 * Detalle clínico asociado a una cita médica.
 */
data class DetailsDTO(
    val id: Int?,
    val appointmentId: Int?,
    val diagnosis: String?,
    val prescription: String?,
    val notes: String?,
    val treatment: String?,
    val followUp: String?
)

typealias AppointmentDetailDTO = DetailsDTO

