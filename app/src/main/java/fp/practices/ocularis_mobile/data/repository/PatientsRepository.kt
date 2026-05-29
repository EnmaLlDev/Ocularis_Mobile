package fp.practices.ocularis_mobile.data.repository

import fp.practices.ocularis_mobile.data.model.PatientDTO
import fp.practices.ocularis_mobile.data.network.ApiService
import fp.practices.ocularis_mobile.data.network.RetrofitClient
import fp.practices.ocularis_mobile.util.Logger

/**
 * Repositorio para operaciones CRUD de pacientes.
 */
class PatientsRepository(api: ApiService? = null) {
    private val TAG = "PatientsRepository"
    // Evalúa el servicio bajo demanda si no fue inyectado; evita excepciones por orden de inicialización
    private val api: ApiService = api ?: RetrofitClient.requireApiService()

    suspend fun getPatients(): List<PatientDTO> = api.getPatients()
    suspend fun getPatient(id: Int): PatientDTO = api.getPatient(id)
    suspend fun create(patient: PatientDTO): PatientDTO {
        Logger.d(TAG, "create() -> POST /api/patient/create body=$patient")
        return try {
            val result = api.createPatient(patient)
            Logger.d(TAG, "create() <- 2xx response=$result")
            result
        } catch (e: Exception) {
            Logger.e(TAG, "create() exception ${e.javaClass.simpleName}: ${e.message}", e)
            throw e
        }
    }
    suspend fun update(id: Int, patient: PatientDTO): PatientDTO = api.updatePatient(id, patient)
    suspend fun getMyData(): PatientDTO? {
        val response = api.getMyPatientData()
        return if (response.isSuccessful) response.body() else null
    }
    suspend fun delete(id: Int): Boolean = api.deletePatient(id).isSuccessful
    suspend fun searchByAddress(address: String): List<PatientDTO> = api.searchPatientsByAddress(address)
}

