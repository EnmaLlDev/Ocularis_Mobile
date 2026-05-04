
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
import kotlinx.coroutines.supervisorScope

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
			supervisorScope {
				// Se obtienen las listas en paralelo sin propagar fallos de red al scope padre.
				val patientsDeferred = async { runCatching { patientsRepository.getPatients().size } }
				val doctorsDeferred = async { runCatching { doctorsRepository.getDoctors().size } }
				val appointmentsDeferred = async { runCatching { appointmentsRepository.getAppointments().size } }

				val patientsResult = patientsDeferred.await()
				val doctorsResult = doctorsDeferred.await()
				val appointmentsResult = appointmentsDeferred.await()

				val firstFailure = listOfNotNull(
					patientsResult.exceptionOrNull(),
					doctorsResult.exceptionOrNull(),
					appointmentsResult.exceptionOrNull()
				).firstOrNull()

				if (firstFailure != null) {
					_error.value = firstFailure.message ?: "Error al cargar el dashboard"
				} else {
					_stats.value = DashboardStats(
						patients = patientsResult.getOrThrow(),
						doctors = doctorsResult.getOrThrow(),
						appointments = appointmentsResult.getOrThrow()
					)
				}
				_isLoading.value = false
			}
		}
	}
}

