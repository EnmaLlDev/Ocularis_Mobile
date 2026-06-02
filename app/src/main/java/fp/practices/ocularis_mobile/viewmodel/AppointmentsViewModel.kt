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
 *
 * doctorId proviene de AuthUserInfo.id (campo id devuelto por /auth/me),
 * que identifica al doctor autenticado en el backend.
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

    // true cuando el usuario activo es PATIENT (usa /api/appointment/my)
    private var useMyEndpoint = false
    private var currentDoctorId: Long? = null

    /**
     * Carga las citas del paciente autenticado o todas las citas.
     * @param isPatient true para cargar solo las citas propias (modo paciente)
     */
    fun loadAppointments(isPatient: Boolean = false) {
        useMyEndpoint = isPatient
        currentDoctorId = null
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
     * Carga las citas del doctor autenticado usando /api/appointment/doctor/{doctorId}.
     */
    fun loadForDoctor(doctorId: Long?) {
        useMyEndpoint = false
        currentDoctorId = doctorId
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _message.value = null
            try {
                val safeDoctorId = doctorId ?: run {
                    _error.value = "Id de doctor no disponible"
                    _appointments.value = emptyList()
                    return@launch
                }
                _appointments.value = repository.getAppointmentsByDoctor(safeDoctorId)
            } catch (e: HttpException) {
                _error.value = "HTTP ${e.code()}"
            } catch (e: Exception) {
                _error.value = e.message ?: "Error desconocido"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun reloadAppointments(): List<AppointmentDTO> {
        val safeDoctorId = currentDoctorId
        return when {
            safeDoctorId != null -> repository.getAppointmentsByDoctor(safeDoctorId)
            useMyEndpoint -> repository.getMyAppointments()
            else -> repository.getAppointments()
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
                _appointments.value = reloadAppointments()
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
                    _appointments.value = reloadAppointments()
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
                    _appointments.value = reloadAppointments()
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
