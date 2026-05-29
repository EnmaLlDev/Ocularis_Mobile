package fp.practices.ocularis_mobile.data.model

/**
 * Datos de un mensaje de contacto enviado a la clínica.
 */
data class ContactMessageDTO(
    val nombre: String,
    val apellido: String,
    val email: String,
    val telefono: String,
    val mensaje: String,
    val revisado: Boolean
)
