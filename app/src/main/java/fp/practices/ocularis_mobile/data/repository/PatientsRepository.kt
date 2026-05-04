package fp.practices.ocularis_mobile.data.repository

import fp.practices.ocularis_mobile.data.model.PatientDTO
import fp.practices.ocularis_mobile.data.network.ApiService
import fp.practices.ocularis_mobile.data.network.RetrofitClient

class PatientsRepository(api: ApiService? = null) {
    // Evalúa el servicio bajo demanda si no fue inyectado; evita excepciones por orden de inicialización
    private val api: ApiService = api ?: RetrofitClient.requireApiService()

    suspend fun getPatients(): List<PatientDTO> = api.getPatients()
    suspend fun getPatient(id: Int): PatientDTO = api.getPatient(id)
    suspend fun create(patient: PatientDTO): PatientDTO = api.createPatient(patient)
    suspend fun update(id: Int, patient: PatientDTO): Boolean = api.updatePatient(id, patient).isSuccessful
    suspend fun delete(id: Int): Boolean = api.deletePatient(id).isSuccessful
    suspend fun searchByAddress(address: String): List<PatientDTO> = api.searchPatientsByAddress(address)
}

