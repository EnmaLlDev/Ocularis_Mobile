package fp.practices.ocularis_mobile.data.repository

import fp.practices.ocularis_mobile.data.model.PatientDTO
import fp.practices.ocularis_mobile.data.network.ApiService
import fp.practices.ocularis_mobile.data.network.RetrofitClient

class PatientsRepository(private val api: ApiService = RetrofitClient.apiService) {
    suspend fun getPatients(): List<PatientDTO> = api.getPatients()
}

