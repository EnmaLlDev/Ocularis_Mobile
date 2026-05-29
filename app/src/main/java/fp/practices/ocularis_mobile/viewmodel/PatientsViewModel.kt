package fp.practices.ocularis_mobile.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fp.practices.ocularis_mobile.data.model.PatientDTO
import fp.practices.ocularis_mobile.data.repository.PatientsRepository
import kotlinx.coroutines.launch

/**
 * ViewModel para la gestión de pacientes.
 */
class PatientsViewModel(
    private val repository: PatientsRepository = PatientsRepository()
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

    /**
     * Carga la lista de pacientes o los datos propios según el rol.
     * @param isPatient true para cargar solo el perfil propio
     */
    fun loadPatients(isPatient: Boolean = false) {
        isPatientMode = isPatient
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
                _error.value = e.message ?: "Error desconocido"
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
            } catch (e: Exception) {
                _error.value = e.message ?: "Error desconocido"
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
                _message.value = "Paciente creado"
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al crear"
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
                _message.value = "Paciente actualizado"
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al actualizar"
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
                    _message.value = "Paciente eliminado"
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

    private suspend fun reloadPatients() {
        _patients.value = if (isPatientMode) {
            listOfNotNull(repository.getMyData())
        } else {
            repository.getPatients()
        }
    }
}

