package fp.practices.ocularis_mobile.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fp.practices.ocularis_mobile.data.model.DoctorDTO
import fp.practices.ocularis_mobile.data.model.PatientDTO
import fp.practices.ocularis_mobile.data.repository.DoctorsRepository
import fp.practices.ocularis_mobile.data.repository.PatientsRepository
import kotlinx.coroutines.launch

class DoctorsViewModel(
    private val repository: DoctorsRepository = DoctorsRepository()
) : ViewModel() {

    private val _doctors = MutableLiveData<List<DoctorDTO>>(emptyList())
    val patients: LiveData<List<DoctorDTO>> = _doctors

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    init {
        loadDoctors()
    }

    fun loadDoctors() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _doctors.value = repository.getDoctors()
            } catch (e: Exception) {
                _error.value = e.message ?: "Error desconocido"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

