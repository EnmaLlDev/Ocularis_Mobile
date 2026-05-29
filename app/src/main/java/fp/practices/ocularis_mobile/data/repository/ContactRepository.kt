package fp.practices.ocularis_mobile.data.repository

import fp.practices.ocularis_mobile.data.model.ContactMessageDTO
import fp.practices.ocularis_mobile.data.model.ContactResponseDTO
import fp.practices.ocularis_mobile.data.network.ApiService
import fp.practices.ocularis_mobile.data.network.RetrofitClient

/**
 * Repositorio para enviar mensajes de contacto a la clínica.
 */
class ContactRepository(api: ApiService? = null) {
    private val api: ApiService = api ?: RetrofitClient.requireAuthApiService()

    suspend fun sendMessage(message: ContactMessageDTO): ContactResponseDTO {
        return api.createContactMessage(message)
    }
}
