package fp.practices.ocularis_mobile.viewmodel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fp.practices.ocularis_mobile.data.model.DetailsDTO
import fp.practices.ocularis_mobile.data.repository.DetailsRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
class DetailsViewModel(
    private val repository: DetailsRepository = DetailsRepository()
) : ViewModel() {
    private val _details = MutableLiveData<List<DetailsDTO>>(emptyList())
    val details: LiveData<List<DetailsDTO>> = _details
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error
    private val _message = MutableLiveData<String?>(null)
    val message: LiveData<String?> = _message
    init {
        loadDetails()
    }
    fun loadDetails(isPatient: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _message.value = null
            try {
                _details.value = if (isPatient) {
                    repository.getMyDetails()
                } else {
                    repository.getDetails()
                }
            } catch (e: HttpException) {
                _error.value = "HTTP ${e.code()}"
            } catch (e: Exception) {
                _error.value = e.message ?: "Error desconocido"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadByAppointment(appointmentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _message.value = null
            try {
                _details.value = repository.getByAppointment(appointmentId)
            } catch (e: HttpException) {
                _error.value = "HTTP ${e.code()}"
            } catch (e: Exception) {
                _error.value = e.message ?: "Error desconocido"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createDetail(detail: DetailsDTO) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _message.value = null
            try {
                repository.create(detail)
                _message.value = "Detalle creado"
                _details.value = repository.getDetails()
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al crear"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateDetail(id: Int, detail: DetailsDTO) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _message.value = null
            try {
                val ok = repository.update(id, detail)
                if (ok) {
                    _message.value = "Detalle actualizado"
                    _details.value = repository.getDetails()
                } else {
                    _error.value = "No se pudo actualizar"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al actualizar"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteDetail(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _message.value = null
            try {
                val ok = repository.delete(id)
                if (ok) {
                    _message.value = "Detalle eliminado"
                    _details.value = repository.getDetails()
                } else {
                    _error.value = "No se pudo eliminar"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al eliminar"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
