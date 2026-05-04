package fp.practices.ocularis_mobile.data.repository

import fp.practices.ocularis_mobile.data.model.DoctorDTO
import fp.practices.ocularis_mobile.data.network.ApiService
import fp.practices.ocularis_mobile.data.network.RetrofitClient

class DoctorsRepository(api: ApiService? = null) {
    private val api: ApiService = api ?: RetrofitClient.requireApiService()

    suspend fun getDoctors(): List<DoctorDTO> = api.getDoctors()
    suspend fun getDoctor(id: Int): DoctorDTO = api.getDoctor(id)
    suspend fun create(doctor: DoctorDTO): DoctorDTO = api.createDoctor(doctor)
    suspend fun update(id: Int, doctor: DoctorDTO): Boolean = api.updateDoctor(id, doctor).isSuccessful
    suspend fun delete(id: Int): Boolean = api.deleteDoctor(id).isSuccessful
    suspend fun searchByLicense(license: String): List<DoctorDTO> = api.searchDoctorsByLicense(license)
    suspend fun searchBySpecialty(terms: String): List<DoctorDTO> = api.searchDoctorsBySpecialty(terms)
}

