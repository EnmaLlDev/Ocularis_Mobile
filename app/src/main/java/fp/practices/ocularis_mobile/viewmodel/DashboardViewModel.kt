package fp.practices.ocularis_mobile.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fp.practices.ocularis_mobile.data.model.Dashboard
import fp.practices.ocularis_mobile.data.model.PatientDTO
import fp.practices.ocularis_mobile.data.repository.DashboardVisualRepository
import fp.practices.ocularis_mobile.data.repository.AppointmentsRepository
import fp.practices.ocularis_mobile.data.repository.DoctorsRepository
import fp.practices.ocularis_mobile.data.repository.PatientsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

/**
 * ViewModel que carga las estadísticas y el contenido visual del dashboard según el rol.
 */
class DashboardViewModel(
	private val patientsRepository: PatientsRepository = PatientsRepository(),
	private val doctorsRepository: DoctorsRepository = DoctorsRepository(),
	private val appointmentsRepository: AppointmentsRepository = AppointmentsRepository(),
	private val visualsRepository: DashboardVisualRepository = DashboardVisualRepository()
) : ViewModel() {

	private val _dashboard = MutableLiveData<Dashboard?>(null)
	val dashboard: LiveData<Dashboard?> = _dashboard

	private val _isLoading = MutableLiveData(false)
	val isLoading: LiveData<Boolean> = _isLoading

	private val _error = MutableLiveData<String?>(null)
	val error: LiveData<String?> = _error

	private val _patientData = MutableLiveData<PatientDTO?>(null)
	val patientData: LiveData<PatientDTO?> = _patientData

	private val _visualsLoading = MutableLiveData(false)
	val visualsLoading: LiveData<Boolean> = _visualsLoading

	private val _visualsError = MutableLiveData<String?>(null)
	val visualsError: LiveData<String?> = _visualsError

	/**
	 * Carga las estadísticas del dashboard filtradas por rol.
	 * @param roles conjunto de roles del usuario
	 */
	fun loadStatsForRoles(roles: Set<String>) {
		when {
			roles.contains("ADMIN") -> loadAdminStats()
			roles.contains("DOCTOR") -> loadDoctorStats()
			roles.contains("PATIENT") -> loadPatientStats()
			else -> {
				_dashboard.value = Dashboard.default()
				_error.value = null
				_isLoading.value = false
			}
		}
	}

	private fun loadAdminStats() {
		viewModelScope.launch {
			_isLoading.value = true
			_error.value = null
			supervisorScope {
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
					val current = _dashboard.value ?: Dashboard.default()
					_dashboard.value = current.copy(
						patients = patientsResult.getOrThrow(),
						doctors = doctorsResult.getOrThrow(),
						appointments = appointmentsResult.getOrThrow()
					)
				}
				_isLoading.value = false
			}
		}
	}

	private fun loadDoctorStats() {
		viewModelScope.launch {
			_isLoading.value = true
			_error.value = null
			supervisorScope {
				val patientsDeferred = async { runCatching { patientsRepository.getPatients().size } }
				val appointmentsDeferred = async { runCatching { appointmentsRepository.getAppointments().size } }

				val patientsResult = patientsDeferred.await()
				val appointmentsResult = appointmentsDeferred.await()

				val firstFailure = listOfNotNull(
					patientsResult.exceptionOrNull(),
					appointmentsResult.exceptionOrNull()
				).firstOrNull()

				if (firstFailure != null) {
					_error.value = firstFailure.message ?: "Error al cargar el dashboard"
				} else {
					val current = _dashboard.value ?: Dashboard.default()
					_dashboard.value = current.copy(
						patients = patientsResult.getOrThrow(),
						doctors = current.doctors,
						appointments = appointmentsResult.getOrThrow()
					)
				}
				_isLoading.value = false
			}
		}
	}

	private fun loadPatientStats() {
		viewModelScope.launch {
			_isLoading.value = true
			_error.value = null
			supervisorScope {
				val patientDeferred = async { runCatching { patientsRepository.getMyData() } }
				val appointmentsDeferred = async { runCatching { appointmentsRepository.getMyAppointments().size } }

				val patientResult = patientDeferred.await()
				val appointmentsResult = appointmentsDeferred.await()

				val firstFailure = listOfNotNull(
					patientResult.exceptionOrNull(),
					appointmentsResult.exceptionOrNull()
				).firstOrNull()

				if (firstFailure != null) {
					_error.value = firstFailure.message ?: "Error al cargar el dashboard"
				} else {
					val patient = patientResult.getOrThrow()
					_patientData.value = patient
					val current = _dashboard.value ?: Dashboard.default()
					val patientCount = if (patient != null) 1 else 0
					_dashboard.value = current.copy(
						patients = patientCount,
						doctors = current.doctors,
						appointments = appointmentsResult.getOrThrow()
					)
				}
				_isLoading.value = false
			}
		}
	}

	/**
	 * Carga el contenido visual del dashboard desde el servidor.
	 */
	fun loadVisualContent() {
		viewModelScope.launch {
			_visualsLoading.value = true
			_visualsError.value = null
			runCatching { visualsRepository.getVisualContent() }
				.onSuccess { fetched ->
					val current = _dashboard.value
					_dashboard.value = if (current != null) {
						fetched.copy(
							patients = current.patients,
							doctors = current.doctors,
							appointments = current.appointments
						)
					} else fetched
				}
				.onFailure { _visualsError.value = it.message ?: "Error al cargar las secciones visuales" }
			_visualsLoading.value = false
		}
	}
}
