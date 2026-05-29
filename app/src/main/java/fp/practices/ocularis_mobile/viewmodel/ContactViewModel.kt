package fp.practices.ocularis_mobile.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fp.practices.ocularis_mobile.data.model.ContactMessageDTO
import fp.practices.ocularis_mobile.data.repository.ContactRepository
import kotlinx.coroutines.launch

/**
 * ViewModel para enviar mensajes de contacto a la clínica.
 */
class ContactViewModel(
    private val repository: ContactRepository = ContactRepository()
) : ViewModel() {
    private val _isSending = MutableLiveData(false)
    val isSending: LiveData<Boolean> = _isSending

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _message = MutableLiveData<String?>(null)
    val message: LiveData<String?> = _message

    /**
     * Envía un mensaje de contacto al backend.
     * @param message datos del mensaje
     */
    fun sendMessage(message: ContactMessageDTO) {
        viewModelScope.launch {
            _isSending.value = true
            _error.value = null
            _message.value = null
            try {
                val response = repository.sendMessage(message)
                if (response.status.equals("success", ignoreCase = true)) {
                    _message.value = response.message ?: "Mensaje enviado"
                } else {
                    _error.value = response.message ?: "No se pudo enviar el mensaje"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al enviar el mensaje"
            } finally {
                _isSending.value = false
            }
        }
    }

    /**
     * Limpia el mensaje de éxito.
     */
    fun clearMessage() {
        _message.value = null
    }

    /**
     * Limpia el mensaje de error.
     */
    fun clearError() {
        _error.value = null
    }
}
