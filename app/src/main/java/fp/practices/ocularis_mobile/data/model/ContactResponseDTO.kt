package fp.practices.ocularis_mobile.data.model

/**
 * Respuesta del servidor tras enviar un mensaje de contacto.
 */
data class ContactResponseDTO(
    val status: String?,
    val message: String?,
    val id: Long? = null
)

