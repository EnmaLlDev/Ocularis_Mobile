package fp.practices.ocularis_mobile.data.model

import java.time.LocalDate

// Nota: uso LocalDate según lo solicitado; si necesitas LocalDateTime, avísame.
data class AppointmentDTO(
    val id: Int?,
    val dateTime: LocalDate?,
    val patient: PatientDTO?,
    val doctor: DoctorDTO?,
    val reason: String?,
    val status: StateAppoinment?
)

