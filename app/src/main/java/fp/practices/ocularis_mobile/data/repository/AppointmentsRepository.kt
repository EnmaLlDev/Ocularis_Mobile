package fp.practices.ocularis_mobile.data.repository

import fp.practices.ocularis_mobile.data.model.AppointmentDTO
import fp.practices.ocularis_mobile.data.network.ApiService
import fp.practices.ocularis_mobile.data.network.RetrofitClient

class AppointmentsRepository(private val api: ApiService = RetrofitClient.requireApiService()) {
    suspend fun getAppointments(): List<AppointmentDTO> = api.getAppointments()
    suspend fun getAppointment(id: Int): AppointmentDTO = api.getAppointment(id)
    suspend fun create(appointment: AppointmentDTO): AppointmentDTO = api.createAppointment(appointment)
    suspend fun update(id: Int, appointment: AppointmentDTO): Boolean = api.updateAppointment(id, appointment).isSuccessful
    suspend fun delete(id: Int): Boolean = api.deleteAppointment(id).isSuccessful
}

