package fp.practices.ocularis_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fp.practices.ocularis_mobile.data.model.DoctorDTO
import fp.practices.ocularis_mobile.data.model.PatientDTO
import fp.practices.ocularis_mobile.data.repository.DoctorsRepository
import fp.practices.ocularis_mobile.data.repository.PatientsRepository
import fp.practices.ocularis_mobile.util.Logger
import retrofit2.HttpException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado UI del registro de usuarios.
 */
data class RegisterUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel para el registro de pacientes y doctores.
 */
class RegisterViewModel(
    private val patientsRepository: PatientsRepository = PatientsRepository(),
    private val doctorsRepository: DoctorsRepository = DoctorsRepository()
) : ViewModel() {

    private val TAG = "RegisterViewModel"

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    /**
     * Registra un nuevo paciente.
     * @param patient datos del paciente
     */
    fun registerPatient(patient: PatientDTO) {
        viewModelScope.launch {
            Logger.d(TAG, "registerPatient() iniciado. Payload: $patient")
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            runCatching { patientsRepository.create(patient) }
                .onSuccess { created ->
                    Logger.d(TAG, "registerPatient() OK. Respuesta backend: $created")
                    _uiState.update { it.copy(isLoading = false, successMessage = "Paciente registrado") }
                }
                .onFailure { ex ->
                    val msg = extractErrorMessage(ex)
                    Logger.e(TAG, "registerPatient() FALLO. ${ex.javaClass.simpleName}: $msg", ex)
                    _uiState.update {
                        it.copy(isLoading = false, error = msg)
                    }
                }
        }
    }

    /**
     * Registra un nuevo doctor.
     * @param doctor datos del doctor
     */
    fun registerDoctor(doctor: DoctorDTO) {
        viewModelScope.launch {
            Logger.d(TAG, "registerDoctor() iniciado. Payload: $doctor")
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            runCatching { doctorsRepository.create(doctor) }
                .onSuccess { created ->
                    Logger.d(TAG, "registerDoctor() OK. Respuesta backend: $created")
                    _uiState.update { it.copy(isLoading = false, successMessage = "Doctor registrado") }
                }
                .onFailure { ex ->
                    val msg = extractErrorMessage(ex)
                    Logger.e(TAG, "registerDoctor() FALLO. ${ex.javaClass.simpleName}: $msg", ex)
                    _uiState.update {
                        it.copy(isLoading = false, error = msg)
                    }
                }
        }
    }

    /**
     * Extrae un mensaje legible de una excepción, incluyendo el cuerpo del error HTTP.
     * @param ex excepción ocurrida
     * @return mensaje descriptivo del error
     */
    private fun extractErrorMessage(ex: Throwable): String {
        return when (ex) {
            is HttpException -> {
                val code = ex.code()
                val body = runCatching { ex.response()?.errorBody()?.string() }.getOrNull()
                Logger.e(TAG, "HTTP $code - errorBody: $body")
                "HTTP $code: ${body ?: ex.message()}"
            }
            else -> ex.message ?: "No se pudo registrar"
        }
    }

    /**
     * Limpia el estado de éxito y error.
     */
    fun clearStatus() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}

