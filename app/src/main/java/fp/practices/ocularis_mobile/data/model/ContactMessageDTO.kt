package fp.practices.ocularis_mobile.data.model

data class ContactMessageDTO(
    val nombre: String,
    val apellido: String,
    val email: String,
    val telefono: String,
    val mensaje: String,
    val revisado: Boolean
)
