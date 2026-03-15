package fp.practices.ocularis_mobile.data.network

import fp.practices.ocularis_mobile.data.model.AppointmentDTO
import fp.practices.ocularis_mobile.data.model.DoctorDTO
import fp.practices.ocularis_mobile.data.model.PatientDTO
import retrofit2.http.GET

interface ApiService {
    @GET("/api/patient")
    suspend fun getPatients(): List<PatientDTO>

    @GET("/api/doctors")
    suspend fun getDoctors(): List<DoctorDTO>


    @GET ("/api/appointments")
    suspend fun getAppointments(): List<AppointmentDTO>

}

