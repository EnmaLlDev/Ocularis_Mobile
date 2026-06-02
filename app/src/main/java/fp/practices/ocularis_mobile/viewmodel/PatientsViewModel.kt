package fp.practices.ocularis_mobile.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fp.practices.ocularis_mobile.data.model.PatientDTO
import fp.practices.ocularis_mobile.data.repository.AppointmentsRepository
import fp.practices.ocularis_mobile.data.repository.PatientsRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * ViewModel para la gestión de pacientes.
 *
 * En modo doctor, los pacientes se derivan de las citas del doctor autenticado
 * (AuthUserInfo.id) usando /api/appointment/doctor/{doctorId}.
 */
class PatientsViewModel(
    private val repository: PatientsRepository = PatientsRepository(),
    private val appointmentsRepository: AppointmentsRepository = AppointmentsRepository()
) : ViewModel() {

    private val _patients = MutableLiveData<List<PatientDTO>>(emptyList())
    val patients: LiveData<List<PatientDTO>> = _patients

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _message = MutableLiveData<String?>(null)
    val message: LiveData<String?> = _message

    private var isPatientMode = false
    private var isDoctorMode = false
    private var currentDoctorId: Long? = null

    /**
     * Carga la lista de pacientes o los datos propios según el rol.
     * @param isPatient true para cargar solo el perfil propio (modo paciente)
     */
    fun loadPatients(isPatient: Boolean = false) {
        isPatientMode = isPatient
        isDoctorMode = false
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _message.value = null
            try {
                _patients.value = if (isPatient) {
                    listOfNotNull(repository.getMyData())
                } else {
                    repository.getPatients()
                }
            } catch (e: Exception) {
                val errorMsg = handleApiError(e, "cargar pacientes")
                _error.value = errorMsg
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Carga los pacientes que tienen citas con el doctor autenticado.
     * Usa /api/appointment/doctor/{doctorId}.
     * Los pacientes únicos se extraen del listado de citas devuelto.
     */
    fun loadForDoctor(doctorId: Long?) {
        isDoctorMode = true
        isPatientMode = false
        currentDoctorId = doctorId
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _message.value = null
            try {
                val safeDoctorId = doctorId ?: run {
                    _error.value = "Id de doctor no disponible"
                    _patients.value = emptyList()
                    return@launch
                }
                val appointments = appointmentsRepository.getAppointmentsByDoctor(safeDoctorId)
                _patients.value = appointments.mapNotNull { it.patient }.distinctBy { it.id }
            } catch (e: Exception) {
                val errorMsg = handleApiError(e, "cargar pacientes del doctor")
                _error.value = errorMsg
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Busca pacientes por dirección.
     * @param address dirección a buscar
     */
    fun searchByAddress(address: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _message.value = null
            try {
                _patients.value = repository.searchByAddress(address)
                if (_patients.value?.isEmpty() == true) {
                    _message.value = "No se encontraron pacientes con esa dirección"
                }
            } catch (e: Exception) {
                val errorMsg = handleApiError(e, "buscar pacientes")
                _error.value = errorMsg
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Crea un nuevo paciente.
     * @param patient datos del paciente
     */
    fun createPatient(patient: PatientDTO) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _message.value = null
            try {
                repository.create(patient)
                reloadPatients()
                _message.value = "Paciente creado exitosamente"
            } catch (e: Exception) {
                val errorMsg = handleApiError(e, "crear paciente")
                _error.value = errorMsg
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Actualiza los datos de un paciente.
     * @param id identificador del paciente
     * @param patient datos actualizados
     */
    fun updatePatient(id: Int, patient: PatientDTO) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _message.value = null
            try {
                repository.update(id, patient)
                reloadPatients()
                _message.value = "Paciente actualizado exitosamente"
            } catch (e: Exception) {
                val errorMsg = handleApiError(e, "actualizar paciente")
                _error.value = errorMsg
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Elimina un paciente por su identificador.
     * @param id identificador del paciente
     */
    fun deletePatient(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _message.value = null
            try {
                val ok = repository.delete(id)
                if (ok) {
                    reloadPatients()
                    _message.value = "Paciente eliminado exitosamente"
                } else {
                    _error.value = "No se pudo eliminar el paciente"
                }
            } catch (e: Exception) {
                val errorMsg = handleApiError(e, "eliminar paciente")
                _error.value = errorMsg
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun reloadPatients() {
        _patients.value = when {
            isDoctorMode -> {
                val safeDoctorId = currentDoctorId
                if (safeDoctorId == null) {
                    _error.postValue("Id de doctor no disponible")
                    emptyList()
                } else {
                    val appointments = appointmentsRepository.getAppointmentsByDoctor(safeDoctorId)
                    appointments.mapNotNull { it.patient }.distinctBy { it.id }
                }
            }
            isPatientMode -> listOfNotNull(repository.getMyData())
            else -> repository.getPatients()
        }
    }

    /**
     * Interpreta los errores de la API y proporciona mensajes útiles.
     */
    private fun handleApiError(exception: Exception, operacion: String): String {
        return when (exception) {
            is HttpException -> {
                when (exception.code()) {
                    401 -> "⚠️ Sesión expirada. Por favor, vuelve a iniciar sesión en la pantalla de inicio."
                    403 -> "❌ No tienes permisos para $operacion"
                    404 -> "❌ Recurso no encontrado"
                    400 -> "❌ Datos inválidos: ${exception.message}"
                    500 -> "❌ Error del servidor. Intenta de nuevo más tarde."
                    else -> "❌ Error ${exception.code()} al $operacion"
                }
            }
            is java.net.SocketTimeoutException -> "⏳ El servidor tarda demasiado en responder (Timeout). Revisa tu conexión."
            is java.net.ConnectException -> "📡 No se pudo conectar con el servidor. Asegúrate de que el backend esté corriendo y la IP sea correcta."
            is java.io.IOException -> "🌐 Error de red: ${exception.message}"
            else -> "❌ Error al $operacion: ${exception.message ?: "desconocido"}"
        }
    }
}
