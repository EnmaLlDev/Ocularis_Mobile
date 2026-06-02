package fp.practices.ocularis_mobile.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fp.practices.ocularis_mobile.data.model.AppointmentDTO
import fp.practices.ocularis_mobile.data.model.DetailsDTO
import fp.practices.ocularis_mobile.data.model.DoctorDTO
import fp.practices.ocularis_mobile.data.model.PatientDTO
import fp.practices.ocularis_mobile.data.repository.AppointmentsRepository
import fp.practices.ocularis_mobile.data.repository.DetailsRepository
import fp.practices.ocularis_mobile.data.repository.DoctorsRepository
import fp.practices.ocularis_mobile.data.repository.PatientsRepository
import kotlinx.coroutines.launch

/**
 * ViewModel para la gestión de médicos.
 */
class DoctorsViewModel(
    private val repository: DoctorsRepository = DoctorsRepository(),
    private val appointmentsRepository: AppointmentsRepository = AppointmentsRepository(),
    private val detailsRepository: DetailsRepository = DetailsRepository()
) : ViewModel() {

    private val _doctors = MutableLiveData<List<DoctorDTO>>(emptyList())
    val doctors: LiveData<List<DoctorDTO>> = _doctors

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _message = MutableLiveData<String?>(null)
    val message: LiveData<String?> = _message

    private val _appointmentsLoading = MutableLiveData(false)
    val appointmentsLoading: LiveData<Boolean> = _appointmentsLoading

    private val _doctorAppointments = MutableLiveData<List<AppointmentDTO>>(emptyList())
    val doctorAppointments: LiveData<List<AppointmentDTO>> = _doctorAppointments

    private val _appointmentDetails = MutableLiveData<Map<Int, List<DetailsDTO>>>(emptyMap())
    val appointmentDetails: LiveData<Map<Int, List<DetailsDTO>>> = _appointmentDetails

    init {
        loadDoctors()
    }

    /**
     * Carga la lista completa de médicos.
     */
    fun loadDoctors() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _message.value = null
            try {
                _doctors.value = repository.getDoctors()
            } catch (e: Exception) {
                _error.value = e.message ?: "Error desconocido"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Busca médicos por número de licencia.
     * @param license número de licencia a buscar
     */
    fun searchByLicense(license: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _message.value = null
            try {
                _doctors.value = repository.searchByLicense(license)
            } catch (e: Exception) {
                _error.value = e.message ?: "Error desconocido"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Busca médicos por especialidad.
     * @param terms término de búsqueda
     */
    fun searchBySpecialty(terms: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _message.value = null
            try {
                _doctors.value = repository.searchBySpecialty(terms)
            } catch (e: Exception) {
                _error.value = e.message ?: "Error desconocido"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Crea un nuevo médico.
     * @param doctor datos del médico
     */
    fun createDoctor(doctor: DoctorDTO) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _message.value = null
            try {
                repository.create(doctor)
                _message.value = "Doctor creado"
                _doctors.value = repository.getDoctors()
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al crear"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Actualiza los datos de un médico.
     * @param id identificador del médico
     * @param doctor datos actualizados
     */
    fun updateDoctor(id: Int, doctor: DoctorDTO) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _message.value = null
            try {
                val ok = repository.update(id, doctor)
                if (ok) {
                    _message.value = "Doctor actualizado"
                    _doctors.value = repository.getDoctors()
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
     * Elimina un médico por su identificador.
     * @param id identificador del médico
     */
    fun deleteDoctor(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _message.value = null
            try {
                val ok = repository.delete(id)
                if (ok) {
                    _message.value = "Doctor eliminado"
                    _doctors.value = repository.getDoctors()
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

    fun loadAppointmentDetails(doctorId: Int) {
        viewModelScope.launch {
            _appointmentsLoading.value = true
            _doctorAppointments.value = emptyList()
            _appointmentDetails.value = emptyMap()
            try {
                val appointments = appointmentsRepository.getAppointmentsByDoctor(doctorId.toLong())
                _doctorAppointments.value = appointments
                val map = mutableMapOf<Int, List<DetailsDTO>>()
                appointments.forEach { appt ->
                    val appId = appt.id ?: return@forEach
                    runCatching { detailsRepository.getByAppointment(appId) }
                        .onSuccess { map[appId] = it }
                        .onFailure { map[appId] = emptyList() }
                }
                _appointmentDetails.value = map
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al cargar detalles de citas"
            } finally {
                _appointmentsLoading.value = false
            }
        }
    }
}

