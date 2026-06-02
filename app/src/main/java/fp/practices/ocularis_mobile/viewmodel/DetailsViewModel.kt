package fp.practices.ocularis_mobile.viewmodel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fp.practices.ocularis_mobile.data.model.AppointmentDTO
import fp.practices.ocularis_mobile.data.model.DetailsDTO
import fp.practices.ocularis_mobile.data.repository.AppointmentsRepository
import fp.practices.ocularis_mobile.data.repository.DetailsRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
/**
 * ViewModel para la gestión de detalles clínicos.
 *
 * doctorId proviene de AuthUserInfo.id (campo id devuelto por /auth/me),
 * que identifica al doctor autenticado en el backend.
 */
class DetailsViewModel(
    private val repository: DetailsRepository = DetailsRepository(),
    private val appointmentsRepository: AppointmentsRepository = AppointmentsRepository()
) : ViewModel() {
    private val _details = MutableLiveData<List<DetailsDTO>>(emptyList())
    val details: LiveData<List<DetailsDTO>> = _details
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error
    private val _message = MutableLiveData<String?>(null)
    val message: LiveData<String?> = _message

    private val _myAppointments = MutableLiveData<List<AppointmentDTO>>(emptyList())
    val myAppointments: LiveData<List<AppointmentDTO>> = _myAppointments

    private val _appointmentDetailsMap = MutableLiveData<Map<Int, List<DetailsDTO>>>(emptyMap())
    val appointmentDetailsMap: LiveData<Map<Int, List<DetailsDTO>>> = _appointmentDetailsMap

    // true cuando el usuario activo es DOCTOR o PATIENT (ambos usan /api/details/my)
    private var useMyEndpoint = false

    /**
     * Carga los detalles clínicos propios o todos según el rol.
     * @param isPatient true para cargar solo los propios (modo paciente)
     */
    fun loadDetails(isPatient: Boolean = false) {
        useMyEndpoint = isPatient
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

    /**
     * Carga los detalles clínicos del doctor autenticado usando /api/details/my.
     * El backend detecta el rol DOCTOR y filtra por doctor.email == username.
     */
    fun loadForDoctor() {
        useMyEndpoint = true
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _message.value = null
            try {
                _details.value = repository.getMyDetails()
            } catch (e: HttpException) {
                _error.value = "HTTP ${e.code()}"
            } catch (e: Exception) {
                _error.value = e.message ?: "Error desconocido"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun reloadDetails(): List<DetailsDTO> =
        if (useMyEndpoint) repository.getMyDetails() else repository.getDetails()

    /**
     * Carga los detalles asociados a una cita específica.
     * @param appointmentId identificador de la cita
     */
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

    /**
     * Crea un nuevo detalle clínico.
     * @param detail datos del detalle
     */
    fun createDetail(detail: DetailsDTO) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _message.value = null
            try {
                repository.create(detail)
                _message.value = "Detalle creado"
                _details.value = reloadDetails()
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al crear"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Actualiza un detalle clínico existente.
     * @param id identificador del detalle
     * @param detail datos actualizados
     */
    fun updateDetail(id: Int, detail: DetailsDTO) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _message.value = null
            try {
                val ok = repository.update(id, detail)
                if (ok) {
                    _message.value = "Detalle actualizado"
                    _details.value = reloadDetails()
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
     * Elimina un detalle clínico por su identificador.
     * @param id identificador del detalle
     */
    fun deleteDetail(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _message.value = null
            try {
                val ok = repository.delete(id)
                if (ok) {
                    _message.value = "Detalle eliminado"
                    _details.value = reloadDetails()
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

    /**
     * Carga las citas del doctor autenticado vía /api/appointment/my y los detalles
     * clínicos de cada una en paralelo.
     */
    fun loadMyAppointmentsWithDetails() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val appts = appointmentsRepository.getMyAppointments()
                _myAppointments.value = appts
                val map = mutableMapOf<Int, List<DetailsDTO>>()
                appts.forEach { appt ->
                    val apptId = appt.id ?: return@forEach
                    runCatching { repository.getByAppointment(apptId) }
                        .onSuccess { map[apptId] = it }
                        .onFailure { map[apptId] = emptyList() }
                }
                _appointmentDetailsMap.value = map
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
     * Recarga los detalles de una cita concreta tras crear/editar/eliminar un detalle.
     * @param appointmentId identificador de la cita
     */
    fun reloadDetailsForAppointment(appointmentId: Int) {
        viewModelScope.launch {
            runCatching { repository.getByAppointment(appointmentId) }
                .onSuccess { details ->
                    val updated = _appointmentDetailsMap.value.orEmpty().toMutableMap()
                    updated[appointmentId] = details
                    _appointmentDetailsMap.value = updated
                }
        }
    }
}
