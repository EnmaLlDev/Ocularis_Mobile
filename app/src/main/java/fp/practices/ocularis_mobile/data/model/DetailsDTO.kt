package fp.practices.ocularis_mobile.data.model

data class DetailsDTO(
    val id: Int?,
    val appointment: AppointmentDTO?,
    val diagnosis: String?,
    val prescription: String?,
    val notes: String?,
    val treatment: String?,
    val followup: String?
)

typealias AppointmentDetailDTO = DetailsDTO

