package fp.practices.ocularis_mobile.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fp.practices.ocularis_mobile.data.model.AppointmentDTO
import fp.practices.ocularis_mobile.data.repository.AppointmentsRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * ViewModel para la gestión de citas médicas.
 */
class AppointmentsViewModel(
    private val repository: AppointmentsRepository = AppointmentsRepository()
) : ViewModel() {

    private val _appointments = MutableLiveData<List<AppointmentDTO>>(emptyList())
    val appointments: LiveData<List<AppointmentDTO>> = _appointments

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _message = MutableLiveData<String?>(null)
    val message: LiveData<String?> = _message

    init {
        loadAppointments()
    }

    /**
     * Carga las citas del paciente autenticado o todas las citas.
     * @param isPatient true para cargar solo las citas propias
     */
    fun loadAppointments(isPatient: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _message.value = null
            try {
                _appointments.value = if (isPatient) {
                    repository.getMyAppointments()
                } else {
                    repository.getAppointments()
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

    /**
     * Crea una nueva cita médica.
     * @param appointment datos de la cita
     */
    fun createAppointment(appointment: AppointmentDTO) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _message.value = null
            try {
                repository.create(appointment)
                _message.value = "Cita creada"
                _appointments.value = repository.getAppointments()
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al crear"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Actualiza una cita existente.
     * @param id identificador de la cita
     * @param appointment datos actualizados
     */
    fun updateAppointment(id: Int, appointment: AppointmentDTO) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _message.value = null
            try {
                val ok = repository.update(id, appointment)
                if (ok) {
                    _message.value = "Cita actualizada"
                    _appointments.value = repository.getAppointments()
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

    /**
     * Elimina una cita por su identificador.
     * @param id identificador de la cita
     */
    fun deleteAppointment(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _message.value = null
            try {
                val ok = repository.delete(id)
                if (ok) {
                    _message.value = "Cita eliminada"
                    _appointments.value = repository.getAppointments()
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
