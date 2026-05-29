package fp.practices.ocularis_mobile.data.repository

import fp.practices.ocularis_mobile.data.model.DoctorDTO
import fp.practices.ocularis_mobile.data.network.ApiService
import fp.practices.ocularis_mobile.data.network.RetrofitClient
import fp.practices.ocularis_mobile.util.Logger

/**
 * Repositorio para operaciones CRUD de médicos.
 */
class DoctorsRepository(api: ApiService? = null) {
    private val TAG = "DoctorsRepository"
    private val api: ApiService = api ?: RetrofitClient.requireApiService()

    suspend fun getDoctors(): List<DoctorDTO> = api.getDoctors()
    suspend fun getDoctor(id: Int): DoctorDTO = api.getDoctor(id)
    suspend fun create(doctor: DoctorDTO): DoctorDTO {
        Logger.d(TAG, "create() -> POST /api/doctor/create body=$doctor")
        return try {
            val result = api.createDoctor(doctor)
            Logger.d(TAG, "create() <- 2xx response=$result")
            result
        } catch (e: Exception) {
            Logger.e(TAG, "create() exception ${e.javaClass.simpleName}: ${e.message}", e)
            throw e
        }
    }
    suspend fun update(id: Int, doctor: DoctorDTO): Boolean = api.updateDoctor(id, doctor).isSuccessful
    suspend fun delete(id: Int): Boolean = api.deleteDoctor(id).isSuccessful
    suspend fun searchByLicense(license: String): List<DoctorDTO> = api.searchDoctorsByLicense(license)
    suspend fun searchBySpecialty(terms: String): List<DoctorDTO> = api.searchDoctorsBySpecialty(terms)
}

