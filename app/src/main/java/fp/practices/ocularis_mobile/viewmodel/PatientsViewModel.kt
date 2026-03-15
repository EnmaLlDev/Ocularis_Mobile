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

    init {
        loadPatients()
    }

    fun loadPatients() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _patients.value = repository.getPatients()
            } catch (e: Exception) {
                _error.value = e.message ?: "Error desconocido"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

