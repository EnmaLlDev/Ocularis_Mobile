package fp.practices.ocularis_mobile.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fp.practices.ocularis_mobile.data.model.PatientDTO
import fp.practices.ocularis_mobile.data.repository.PatientsRepository
import kotlinx.coroutines.launch

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

    init {
        loadPatients()
    }

    fun loadPatients() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _message.value = null
            try {
                _patients.value = repository.getPatients()
            } catch (e: Exception) {
                _error.value = e.message ?: "Error desconocido"
            } finally {
                _isLoading.value = false
            }
        }
    }

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

    fun createPatient(patient: PatientDTO) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _message.value = null
            try {
                repository.create(patient)
                _message.value = "Paciente creado"
                _patients.value = repository.getPatients()
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al crear"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updatePatient(id: Int, patient: PatientDTO) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _message.value = null
            try {
                val ok = repository.update(id, patient)
                if (ok) {
                    _message.value = "Paciente actualizado"
                    _patients.value = repository.getPatients()
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

    fun deletePatient(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _message.value = null
            try {
                val ok = repository.delete(id)
                if (ok) {
                    _message.value = "Paciente eliminado"
                    _patients.value = repository.getPatients()
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

