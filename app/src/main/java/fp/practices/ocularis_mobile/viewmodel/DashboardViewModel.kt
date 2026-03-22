
package fp.practices.ocularis_mobile.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fp.practices.ocularis_mobile.data.model.DashboardStats
import fp.practices.ocularis_mobile.data.repository.AppointmentsRepository
import fp.practices.ocularis_mobile.data.repository.DoctorsRepository
import fp.practices.ocularis_mobile.data.repository.PatientsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class DashboardViewModel(
	private val patientsRepository: PatientsRepository = PatientsRepository(),
	private val doctorsRepository: DoctorsRepository = DoctorsRepository(),
	private val appointmentsRepository: AppointmentsRepository = AppointmentsRepository()
) : ViewModel() {

	private val _stats = MutableLiveData<DashboardStats?>(null)
	val stats: LiveData<DashboardStats?> = _stats

	private val _isLoading = MutableLiveData(false)
	val isLoading: LiveData<Boolean> = _isLoading

	private val _error = MutableLiveData<String?>(null)
	val error: LiveData<String?> = _error

	init {
		loadStats()
	}

	fun loadStats() {
		viewModelScope.launch {
			_isLoading.value = true
			_error.value = null
			try {
				// Se obtienen las listas en paralelo y sólo se usan los tamaños como KPI.
				val patientsDeferred = async { patientsRepository.getPatients().size }
				val doctorsDeferred = async { doctorsRepository.getDoctors().size }
				val appointmentsDeferred = async { appointmentsRepository.getAppointments().size }

				_stats.value = DashboardStats(
					patients = patientsDeferred.await(),
					doctors = doctorsDeferred.await(),
					appointments = appointmentsDeferred.await()
				)
			} catch (e: Exception) {
				_error.value = e.message ?: "Error al cargar el dashboard"
			} finally {
				_isLoading.value = false
			}
		}
	}
}

